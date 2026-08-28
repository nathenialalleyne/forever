package dev.forever.tools.audit;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Loads supported inputs without writing or extracting anything beside the output. */
public final class InputLoader {
    private InputLoader() {
    }

    public static InputSnapshot load(Path input) throws AuditException {
        if (input == null) {
            throw inputError("Input path cannot be null.");
        }
        Path absolute = input.toAbsolutePath().normalize();
        if (!Files.exists(absolute, LinkOption.NOFOLLOW_LINKS)) {
            throw inputError("Input path does not exist: " + input);
        }
        if (Files.isSymbolicLink(absolute)) {
            throw inputError("Input path must not be a symbolic link: " + input);
        }
        try {
            if (Files.isDirectory(absolute, LinkOption.NOFOLLOW_LINKS)) {
                return readDirectory(absolute);
            }
            if (Files.isRegularFile(absolute, LinkOption.NOFOLLOW_LINKS)) {
                String lowerName = absolute.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
                long size = Files.size(absolute);
                if (size > ZipSafety.MAX_ARCHIVE_BYTES) {
                    throw new AuditException(ExitCodes.UNSAFE_ARCHIVE, "ZIP archive is " + size + " bytes, over the " + ZipSafety.MAX_ARCHIVE_BYTES + " byte limit.");
                }
                byte[] archive = Files.readAllBytes(absolute);
                if (!lowerName.endsWith(".zip") && !hasZipSignature(archive)) {
                    throw inputError("Input file is not a ZIP archive: " + input);
                }
                return ZipSafety.read(archive, absolute);
            }
            throw inputError("Input path is neither a directory nor a regular ZIP file: " + input);
        } catch (AuditException exception) {
            throw exception;
        } catch (IOException exception) {
            throw inputError("Input could not be read: " + input + " (" + exception.getMessage() + ")", exception);
        } catch (SecurityException exception) {
            throw inputError("Input could not be accessed: " + input + " (" + exception.getMessage() + ")", exception);
        }
    }

    private static InputSnapshot readDirectory(Path root) throws AuditException {
        List<InputFile> files = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        long totalBytes = 0L;
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : paths.sorted().toList()) {
                if (Files.isSymbolicLink(path)) {
                    throw inputError("Directory input contains a symbolic link: " + root.relativize(path));
                }
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    continue;
                }
                if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                    throw inputError("Directory input contains a non-regular file: " + root.relativize(path));
                }
                Path relative = root.relativize(path).normalize();
                String logicalPath = relative.toString().replace(java.io.File.separatorChar, '/');
                if (logicalPath.isBlank() || !seen.add(logicalPath)) {
                    throw inputError("Directory input contains a duplicate logical path: " + logicalPath);
                }
                long size = Files.size(path);
                if (size > ZipSafety.MAX_FILE_BYTES) {
                    throw inputError("Directory file '" + logicalPath + "' is " + size + " bytes, over the " + ZipSafety.MAX_FILE_BYTES + " byte limit.");
                }
                totalBytes += size;
                if (totalBytes > ZipSafety.MAX_TOTAL_UNCOMPRESSED_BYTES) {
                    throw inputError("Directory input exceeds the " + ZipSafety.MAX_TOTAL_UNCOMPRESSED_BYTES + " byte total limit.");
                }
                byte[] content = Files.readAllBytes(path);
                files.add(new InputFile(logicalPath, content, Hashing.sha256(content)));
            }
        } catch (AuditException exception) {
            throw exception;
        } catch (IOException exception) {
            throw inputError("Directory input could not be read: " + root + " (" + exception.getMessage() + ")", exception);
        }
        files.sort(Comparator.comparing(InputFile::path));
        return new InputSnapshot(InputSnapshot.InputKind.DIRECTORY, root, files, Hashing.directoryManifestSha256(files));
    }

    private static AuditException inputError(String message) {
        return new AuditException(ExitCodes.INPUT, message);
    }

    private static AuditException inputError(String message, Throwable cause) {
        return new AuditException(ExitCodes.INPUT, message, cause);
    }

    private static boolean hasZipSignature(byte[] bytes) {
        return bytes.length >= 4 && bytes[0] == 'P' && bytes[1] == 'K'
                && ((bytes[2] == 3 && bytes[3] == 4) || (bytes[2] == 5 && bytes[3] == 6) || (bytes[2] == 7 && bytes[3] == 8));
    }
}
