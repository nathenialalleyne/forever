package dev.forever.tools.audit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/** ZIP central-directory inspection and bounded, non-extracting reading. */
public final class ZipSafety {
    public static final long MAX_ARCHIVE_BYTES = 128L * 1024L * 1024L;
    public static final long MAX_FILE_BYTES = 32L * 1024L * 1024L;
    public static final long MAX_TOTAL_UNCOMPRESSED_BYTES = 512L * 1024L * 1024L;
    public static final int MAX_ENTRIES = 100_000;
    public static final long MAX_COMPRESSION_RATIO = 1_000L;

    private static final int END_OF_CENTRAL_DIRECTORY = 0x06054b50;
    private static final int CENTRAL_DIRECTORY_ENTRY = 0x02014b50;
    private static final int MAX_END_RECORD_SEARCH = 22 + 65_535;

    private ZipSafety() {
    }

    public static Set<String> symlinkNames(byte[] archive) throws AuditException {
        int endOffset = findEndOfCentralDirectory(archive);
        long centralDirectorySize = unsignedInt(archive, endOffset + 12);
        long centralDirectoryOffset = unsignedInt(archive, endOffset + 16);
        int entryCount = unsignedShort(archive, endOffset + 10);
        if (centralDirectorySize == 0xffff_ffffL || centralDirectoryOffset == 0xffff_ffffL || entryCount == 0xffff) {
            throw unsafe("ZIP64 archives are not accepted by the bounded reader.");
        }
        if (entryCount > MAX_ENTRIES) {
            throw unsafe("ZIP archive has more than " + MAX_ENTRIES + " central-directory entries.");
        }
        if (centralDirectoryOffset + centralDirectorySize > archive.length || centralDirectoryOffset < 0) {
            throw unsafe("ZIP central directory lies outside the archive.");
        }

        Set<String> result = new HashSet<>();
        int cursor = (int) centralDirectoryOffset;
        for (int index = 0; index < entryCount; index++) {
            requireBytes(archive, cursor, 46, "ZIP central-directory entry");
            if (intAt(archive, cursor) != CENTRAL_DIRECTORY_ENTRY) {
                throw unsafe("ZIP central directory is malformed at entry " + index + ".");
            }
            int madeBy = unsignedShort(archive, cursor + 4);
            int flags = unsignedShort(archive, cursor + 8);
            int nameLength = unsignedShort(archive, cursor + 28);
            int extraLength = unsignedShort(archive, cursor + 30);
            int commentLength = unsignedShort(archive, cursor + 32);
            long externalAttributes = unsignedInt(archive, cursor + 38);
            int recordLength = 46 + nameLength + extraLength + commentLength;
            requireBytes(archive, cursor, recordLength, "ZIP central-directory record");

            byte[] nameBytes = new byte[nameLength];
            System.arraycopy(archive, cursor + 46, nameBytes, 0, nameLength);
            Charset charset = (flags & 0x0800) == 0 ? Charset.forName("IBM437") : StandardCharsets.UTF_8;
            String name = new String(nameBytes, charset);
            String logicalName = normaliseEntryName(name);
            int operatingSystem = (madeBy >>> 8) & 0xff;
            int unixMode = (int) ((externalAttributes >>> 16) & 0xffff);
            boolean unixSymlink = operatingSystem == 3 && (unixMode & 0170000) == 0120000;
            boolean windowsReparsePoint = (externalAttributes & 0x400L) != 0;
            if (unixSymlink || windowsReparsePoint) {
                result.add(logicalName);
            }
            cursor += recordLength;
        }
        if (cursor != centralDirectoryOffset + centralDirectorySize) {
            throw unsafe("ZIP central directory record lengths do not match its declared size.");
        }
        return result;
    }

    public static int centralDirectoryEntryCount(byte[] archive) throws AuditException {
        int endOffset = findEndOfCentralDirectory(archive);
        int entryCount = unsignedShort(archive, endOffset + 10);
        if (entryCount == 0xffff) {
            throw unsafe("ZIP64 archives are not accepted by the bounded reader.");
        }
        return entryCount;
    }

    public static InputSnapshot read(byte[] archive, java.nio.file.Path source) throws AuditException {
        if (archive.length > MAX_ARCHIVE_BYTES) {
            throw unsafe("ZIP archive is " + archive.length + " bytes, over the " + MAX_ARCHIVE_BYTES + " byte limit.");
        }
        Set<String> symlinks = symlinkNames(archive);
        int centralEntryCount = centralDirectoryEntryCount(archive);
        Set<String> seen = new HashSet<>();
        java.util.List<InputFile> files = new java.util.ArrayList<>();
        long totalBytes = 0L;
        int entryCount = 0;

        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
            java.util.zip.ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                entryCount++;
                if (entryCount > MAX_ENTRIES) {
                    throw unsafe("ZIP archive has more than " + MAX_ENTRIES + " entries.");
                }
                String path = normaliseEntryName(entry.getName());
                if (!seen.add(path)) {
                    throw unsafe("ZIP archive contains duplicate logical path '" + path + "'.");
                }
                if (symlinks.contains(path)) {
                    throw unsafe("ZIP archive contains a symlink entry at '" + path + "'.");
                }
                if (entry.isDirectory()) {
                    input.closeEntry();
                    continue;
                }
                long declaredSize = entry.getSize();
                long compressedSize = entry.getCompressedSize();
                if (declaredSize > MAX_FILE_BYTES) {
                    throw unsafe("ZIP entry '" + path + "' declares " + declaredSize + " bytes, over the " + MAX_FILE_BYTES + " byte limit.");
                }
                if (declaredSize >= 0 && compressedSize > 0 && declaredSize / compressedSize > MAX_COMPRESSION_RATIO) {
                    throw unsafe("ZIP entry '" + path + "' exceeds the " + MAX_COMPRESSION_RATIO + ":1 compression-ratio limit.");
                }

                ByteArrayOutputStream bytes = new ByteArrayOutputStream((int) Math.min(Math.max(declaredSize, 0), 8192));
                byte[] buffer = new byte[8192];
                int read;
                long entryBytes = 0L;
                while ((read = input.read(buffer)) != -1) {
                    entryBytes += read;
                    totalBytes += read;
                    if (entryBytes > MAX_FILE_BYTES) {
                        throw unsafe("ZIP entry '" + path + "' expands beyond the " + MAX_FILE_BYTES + " byte limit.");
                    }
                    if (totalBytes > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                        throw unsafe("ZIP archive expands beyond the " + MAX_TOTAL_UNCOMPRESSED_BYTES + " byte total limit.");
                    }
                    if (compressedSize > 0 && entryBytes / compressedSize > MAX_COMPRESSION_RATIO) {
                        throw unsafe("ZIP entry '" + path + "' exceeds the " + MAX_COMPRESSION_RATIO + ":1 compression-ratio limit.");
                    }
                    bytes.write(buffer, 0, read);
                }
                if (declaredSize >= 0 && declaredSize != entryBytes) {
                    throw unsafe("ZIP entry '" + path + "' declared " + declaredSize + " bytes but yielded " + entryBytes + ".");
                }
                byte[] content = bytes.toByteArray();
                files.add(new InputFile(path, content, Hashing.sha256(content)));
                input.closeEntry();
            }
        } catch (ZipException exception) {
            throw unsafe("ZIP archive is malformed or could not be safely read: " + exception.getMessage(), exception);
        } catch (IOException exception) {
            throw unsafe("ZIP archive could not be read: " + exception.getMessage(), exception);
        }

        if (entryCount != centralEntryCount) {
            throw unsafe("ZIP local entry count (" + entryCount + ") does not match central-directory count (" + centralEntryCount + ").");
        }

        files.sort(java.util.Comparator.comparing(InputFile::path));
        return new InputSnapshot(InputSnapshot.InputKind.ZIP, source, files, Hashing.sha256(archive));
    }

    public static String normaliseEntryName(String rawName) throws AuditException {
        if (rawName == null || rawName.isBlank() || rawName.indexOf('\0') >= 0) {
            throw unsafe("ZIP entry has an empty or NUL-containing path.");
        }
        String name = rawName.replace('\\', '/');
        if (name.startsWith("/") || name.startsWith("//") || name.matches("^[A-Za-z]:($|/).*") || name.matches("^[A-Za-z]:.*")) {
            throw unsafe("ZIP entry path is absolute or drive-qualified: '" + rawName + "'.");
        }
        java.nio.file.Path path;
        try {
            path = java.nio.file.Path.of(name).normalize();
        } catch (RuntimeException exception) {
            throw unsafe("ZIP entry path is invalid: '" + rawName + "'.", exception);
        }
        if (path.isAbsolute() || path.toString().equals("..") || path.startsWith("..")) {
            throw unsafe("ZIP entry path escapes the archive root: '" + rawName + "'.");
        }
        String normalised = path.toString().replace(java.io.File.separatorChar, '/');
        while (normalised.endsWith("/") && normalised.length() > 1) {
            normalised = normalised.substring(0, normalised.length() - 1);
        }
        if (normalised.isBlank() || normalised.equals(".")) {
            throw unsafe("ZIP entry path resolves to the archive root: '" + rawName + "'.");
        }
        return normalised;
    }

    private static int findEndOfCentralDirectory(byte[] archive) throws AuditException {
        int first = Math.max(0, archive.length - MAX_END_RECORD_SEARCH);
        for (int offset = archive.length - 22; offset >= first; offset--) {
            if (offset >= 0 && intAt(archive, offset) == END_OF_CENTRAL_DIRECTORY) {
                requireBytes(archive, offset, 22, "ZIP end-of-central-directory record");
                return offset;
            }
        }
        throw unsafe("ZIP archive has no valid end-of-central-directory record.");
    }

    private static void requireBytes(byte[] bytes, int offset, int length, String what) throws AuditException {
        if (offset < 0 || length < 0 || offset > bytes.length - length) {
            throw unsafe("" + what + " lies outside the archive.");
        }
    }

    private static int intAt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff)
                | ((bytes[offset + 1] & 0xff) << 8)
                | ((bytes[offset + 2] & 0xff) << 16)
                | ((bytes[offset + 3] & 0xff) << 24);
    }

    private static int unsignedShort(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }

    private static long unsignedInt(byte[] bytes, int offset) {
        return Integer.toUnsignedLong(intAt(bytes, offset));
    }

    private static AuditException unsafe(String message) {
        return new AuditException(ExitCodes.UNSAFE_ARCHIVE, message);
    }

    private static AuditException unsafe(String message, Throwable cause) {
        return new AuditException(ExitCodes.UNSAFE_ARCHIVE, message, cause);
    }
}
