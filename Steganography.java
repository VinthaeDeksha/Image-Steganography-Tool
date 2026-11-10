
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

public class Steganography {

    private static final byte[] MAGIC = {
        'S', 'T', 'G', '1'
    };

    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATIONS = 65536;

    private static void writeBit(
            BufferedImage image, int bitIndex, int bit) {

        int pixelIndex = bitIndex / 3;
        int channel = bitIndex % 3;

        int x = pixelIndex % image.getWidth();
        int y = pixelIndex / image.getWidth();

        int pixel = image.getRGB(x, y);

        int r = (pixel >> 16) & 255;
        int g = (pixel >> 8) & 255;
        int b = pixel & 255;

        if (channel == 0) r = (r & 254) | bit;
        else if (channel == 1) g = (g & 254) | bit;
        else b = (b & 254) | bit;

        int newPixel = (pixel & 0xFF000000)
                | (r << 16) | (g << 8) | b;

        image.setRGB(x, y, newPixel);
    }

    private static int readBit(
            BufferedImage image, int bitIndex) {

        int pixelIndex = bitIndex / 3;
        int channel = bitIndex % 3;

        int x = pixelIndex % image.getWidth();
        int y = pixelIndex / image.getWidth();

        int pixel = image.getRGB(x, y);

        if (channel == 0) return (pixel >> 16) & 1;
        else if (channel == 1) return (pixel >> 8) & 1;
        else return pixel & 1;
    }

    private static void writeBytes(
            BufferedImage image, byte[] data, int startBit) {

        for (byte value : data) {
            for (int j = 7; j >= 0; j--) {
                int bit = (value >> j) & 1;
                writeBit(image, startBit++, bit);
            }
        }
    }

    private static byte[] readBytes(
            BufferedImage image, int length, int startBit) {

        byte[] data = new byte[length];

        for (int i = 0; i < length; i++) {
            int value = 0;

            for (int j = 0; j < 8; j++) {
                value = (value << 1)
                        | readBit(image, startBit++);
            }

            data[i] = (byte) value;
        }

        return data;
    }

    private static SecretKey generateKey(
            char[] password, byte[] salt) throws Exception {

        PBEKeySpec spec = new PBEKeySpec(
                password, salt, ITERATIONS, KEY_LENGTH);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

            byte[] keyBytes = factory
                    .generateSecret(spec).getEncoded();

            return new SecretKeySpec(keyBytes, "AES");

        } finally {
            spec.clearPassword();
        }
    }

    public static void encode(
            File inputFile,
            String message,
            char[] password,
            File outputFile) throws Exception {

        BufferedImage image = ImageIO.read(inputFile);

        if (image == null) {
            throw new IOException("Invalid image file!");
        }

        if (password == null || password.length == 0) {
            throw new IOException("Password cannot be empty!");
        }

        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[IV_LENGTH];

        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);

        SecretKey key = generateKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                key,
                new GCMParameterSpec(TAG_LENGTH, iv));

        byte[] encryptedMessage = cipher.doFinal(
                message.getBytes(StandardCharsets.UTF_8));

        // Store: MAGIC + SALT + IV + ENCRYPTED MESSAGE
        byte[] payload = new byte[
                MAGIC.length + salt.length
                + iv.length + encryptedMessage.length];

        int position = 0;

        System.arraycopy(MAGIC, 0, payload, position, MAGIC.length);
        position += MAGIC.length;

        System.arraycopy(salt, 0, payload, position, salt.length);
        position += salt.length;

        System.arraycopy(iv, 0, payload, position, iv.length);
        position += iv.length;

        System.arraycopy(
                encryptedMessage, 0,
                payload, position, encryptedMessage.length);

        long capacity =
                (long) image.getWidth() * image.getHeight() * 3 / 8;

        if ((long) payload.length + 4 > capacity) {
            throw new IOException(
                    "Message is too large for this image!");
        }

        int length = payload.length;

        byte[] lengthBytes = {
            (byte) (length >> 24),
            (byte) (length >> 16),
            (byte) (length >> 8),
            (byte) length
        };

        writeBytes(image, lengthBytes, 0);
        writeBytes(image, payload, 32);

        if (!ImageIO.write(image, "png", outputFile)) {
            throw new IOException("Could not save encoded image!");
        }
    }

    public static String decode(
            File inputFile, char[] password) throws Exception {

        BufferedImage image = ImageIO.read(inputFile);

        if (image == null) {
            throw new IOException("Invalid image file!");
        }

        if (password == null || password.length == 0) {
            throw new IOException("Password cannot be empty!");
        }

        long capacity =
                (long) image.getWidth() * image.getHeight() * 3 / 8;

        if (capacity < 4) {
            throw new IOException("Image is too small!");
        }

        byte[] lengthBytes = readBytes(image, 4, 0);

        int length =
                ((lengthBytes[0] & 255) << 24)
                | ((lengthBytes[1] & 255) << 16)
                | ((lengthBytes[2] & 255) << 8)
                | (lengthBytes[3] & 255);

        if (length < MAGIC.length + SALT_LENGTH
                + IV_LENGTH + 16
                || (long) length > capacity - 4) {

            throw new IOException(
                    "No valid encrypted message found!");
        }

        byte[] payload = readBytes(image, length, 32);

        int position = 0;

        for (byte value : MAGIC) {
            if (payload[position++] != value) {
                throw new IOException(
                        "No valid encrypted message found!");
            }
        }

        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(payload, position, salt, 0, salt.length);
        position += salt.length;

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(payload, position, iv, 0, iv.length);
        position += iv.length;

        int encryptedLength = payload.length - position;

        byte[] encryptedMessage = new byte[encryptedLength];

        System.arraycopy(
                payload, position,
                encryptedMessage, 0, encryptedLength);

        SecretKey key = generateKey(password, salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                new GCMParameterSpec(TAG_LENGTH, iv));

        try {
            byte[] messageBytes = cipher.doFinal(encryptedMessage);

            return new String(
                    messageBytes, StandardCharsets.UTF_8);

        } catch (AEADBadTagException ex) {
            throw new IOException(
                    "Incorrect password or corrupted image!");
        }
    }
}
