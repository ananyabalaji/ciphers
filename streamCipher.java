import java.io.*;

public class streamCipher {
    
    // Method that creates a seed from the password
    public static long sdbm(String password) {
        long seed = 0;
        for (int i = 0; i < password.length(); i++) {
            char charVal = password.charAt(i);
            seed = (charVal + (seed << 6) + (seed << 16) - seed) % (1L << 64);
        }
        return seed;
    }

    // Method that constructs a keystream of length of the message given the seed
    public static long generateKey(long seed) {
        long a = 1103515245;
        long m = 256;
        long c = 12345;
        
        return ((a * seed) + c) % m;
        // return keyStream;
    }

    public static void streamCipher(byte[] plaintext, byte[] key, FileOutputStream ciphertextFile) throws IOException {
        for (int i = 0; i < plaintext.length; i++) {
            if (i < key.length) {
                ciphertextFile.write((byte) (plaintext[i] ^ key[i]));
            } else {
                ciphertextFile.write((byte) (plaintext[i] ^ key[0]));
            }
        }
    }    

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java streamCipher <password> <plaintextFilePath> <ciphertextFilePath>");
            return;
        }
        String password = args[0];
        String plaintextFilePath = args[1];
        String ciphertextFilePath = args[2];

        try (FileInputStream plainTextFile = new FileInputStream(plaintextFilePath);
             FileOutputStream ciphertextFile = new FileOutputStream(ciphertextFilePath)) {
            
            long seed = sdbm(password);
            int dataByte = plainTextFile.read();
            while (dataByte != -1){
                seed = generateKey(seed);
                ciphertextFile.write((int) seed ^ dataByte);
                dataByte = plainTextFile.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
