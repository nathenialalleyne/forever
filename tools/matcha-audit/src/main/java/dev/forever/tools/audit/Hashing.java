package dev.forever.tools.audit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Small SHA-256 helpers used by input identity and inventory reports. */
public final class Hashing {
    private Hashing() {
    }

    public static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return hex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("The JDK does not provide SHA-256.", exception);
        }
    }

    public static String directoryManifestSha256(Iterable<InputFile> files) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (InputFile file : files) {
                byte[] path = file.path().getBytes(StandardCharsets.UTF_8);
                digest.update(path);
                digest.update((byte) 0);
                digest.update(ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN).putLong(file.content().length).array());
                digest.update(file.content());
                digest.update((byte) 0xff);
            }
            return hex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("The JDK does not provide SHA-256.", exception);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            result.append(Character.forDigit((value >>> 4) & 0x0f, 16));
            result.append(Character.forDigit(value & 0x0f, 16));
        }
        return result.toString();
    }
}
