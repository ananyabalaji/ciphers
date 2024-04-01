import java.util.*;
import java.io.*;

public class blockCipherDecryption{
    
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
    public static byte[] generateKey(long seed, int messageLength) {
        byte[] keyStream = new byte[messageLength];
        keyStream[0] = (byte) (seed % 256);
        long a = 1103515245;
        long m = 256;
        long c = 12345;
        for (int i = 1; i < messageLength; i++) {
            keyStream[i] = (byte) ((a * keyStream[i - 1] + c) % m);
        }
        return keyStream;
    }

    // performs the XOR portions and returns blockCipher[]
    public static byte[] XOR_operation(byte[] plaintext, byte[] key) throws IOException {
        byte [] blockCipher = new byte [plaintext.length];

        for (int i = 0; i < plaintext.length; i++) {
            if (i < key.length) {
                blockCipher[i] = ((byte) (plaintext[i] ^ key[i]));
            } 
            // else {
            //     // If key is shorter than plaintext, use key[0] for remaining plaintext bytes
            //     blockCipher[i] = ((byte) (plaintext[i] ^ key[0]));
            // }
        }

        return blockCipher;
        
    }    

    public static void permutations(byte[] blockCipher, byte[] key) throws IOException {
        for (int i = blockCipher.length - 1; i >= 0; i--) {
            int first = key[i] & 0xF; // lower 4 bits of the keystream
            int second = (key[i] >> 4) & 0xF; // Top 4 bits of the keystream
    
            // Reversing the swap
            byte temp = blockCipher[second];
            blockCipher[second] = blockCipher[first];
            blockCipher[first] = temp;
        }
    }

    public static void printer(byte [] input, FileOutputStream plainText){
        try {
            for (int i = 0; i < input.length; i++){
                plainText.write(input[i]);
            }
        } catch (IOException e) {
            // Handle the exception (e.g., print an error message)
            e.printStackTrace();
        }
    }


    public static void printer(byte[] input, FileOutputStream plainText, int paddingBytes) {
        try {
            // If there are exactly 16 padding bytes, print nothing
            if (paddingBytes == 16) {
                return; //nothing is performed
            } else if (paddingBytes > 0 && paddingBytes < 16) {
                // If paddingBytes is > 0 and < 16, print only up to paddingBytes - 1 inclusive
                for (int i = 0; i < input.length-paddingBytes; i++) {
                    plainText.write(input[i]);
                }
            } else {
                // If paddingBytes is 0, print the entire array
                for (byte b : input) {
                    plainText.write(b);
                }
            }
        } catch (IOException e) {
            // Handle the exception (e.g., print an error message)
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java scrypt <password> <plaintextFilePath> <ciphertextFilePath>");
            return;
        }
        String password = args[0];
        String ciphertextFilePath = args[1];
        String plaintextFilePath = args[2];

        try (FileInputStream cipherTextFile = new FileInputStream(ciphertextFilePath);
             FileOutputStream plainTextFile = new FileOutputStream(plaintextFilePath)) {
            
            byte[] ciphertext_blockN = new byte[16];

            int bytesRem = cipherTextFile.available(); //16 is full size
            int bytesRead = cipherTextFile.read(ciphertext_blockN);

            if (bytesRead == -1){
                System.err.println("Empty Input File");
                return;
            }

            //perform the processing            
            //FIRST CASE = 16 Bytes

            //decryption component (same order running these calls could yield same results)
            byte[] IV = generateKey(sdbm(password), 16);

            //need to generate before hand 
            byte [] keyStreamShuffle = generateKey(sdbm(password), 16);

            //1. Find temp_blockN (Step 5)
            byte [] temp_blockN = XOR_operation(ciphertext_blockN, keyStreamShuffle);

            //2. Do Read and Shuffle (Steps 3 + 4)
            permutations(temp_blockN, keyStreamShuffle);

            byte [] ciphertext_blockN_1 = IV;

            //3. Find plaintext_blockN (Step 2)
            byte [] plaintext_blockN = XOR_operation(ciphertext_blockN_1, temp_blockN);

            // System.out.println("Bytes Remaining: " + bytesRem);

            // for (int i = 0; i < plaintext_blockN.length; i++){
            //     System.out.println("Bytes in Plaintext: " + plaintext_blockN[i]);
            // }

            // for (byte i : ciphertext_blockN){
            //     System.out.println("Bytes in Curr Ciphertext: " + i);
            // }


            // System.out.println("DONE");

            if (bytesRem == 16){
                byte paddingByte = plaintext_blockN[plaintext_blockN.length-1];
                // System.out.println((int) paddingByte);

                if (paddingByte == (byte)16){
                    return; //don't do anything
                }

                if (paddingByte > (byte) 0 && paddingByte < (byte) 16){
                        try {
                            for (int i = 0; i < plaintext_blockN.length - ((int)paddingByte); i++){
                            plainTextFile.write(plaintext_blockN[i]); 

                            // System.out.println("hi" + i);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                else {
                    printer(plaintext_blockN, plainTextFile);
                    for (int i = 0; i < plaintext_blockN.length; i++){
                        System.out.println(plaintext_blockN[i]);
                    }

                    // for (byte i : ciphertext_blockN){
                    //     System.out.println("Bytes in Curr Ciphertext: " + i);
                    // }
                }

            }

            if (bytesRem != 16){
                //if there are more remaining, treat as it would have been for the rest of the code
                printer(plaintext_blockN, plainTextFile);
                // for (int i = 0; i < plaintext_blockN.length; i++){
                //     System.out.println(plaintext_blockN[i]);
                // }
            }

            ciphertext_blockN_1 = Arrays.copyOf(ciphertext_blockN, ciphertext_blockN.length);

            bytesRem = cipherTextFile.available();

            // System.out.println("Bytes Remaining before While: " + bytesRem);

            while ((bytesRead = cipherTextFile.read(ciphertext_blockN)) != -1) {

                //need to generate before hand 
                keyStreamShuffle = generateKey(sdbm(password), 16);
    
                //1. Find temp_blockN (Step 5)
                temp_blockN = XOR_operation(ciphertext_blockN, keyStreamShuffle);
    
                //2. Do Read and Shuffle (Steps 3 + 4)
                permutations(temp_blockN, keyStreamShuffle);

                //3. Find plaintext_blockN (Step 2)
                //ciphertext_block_N_1 was prev block_N
                plaintext_blockN = XOR_operation(ciphertext_blockN_1, temp_blockN);
    
                // System.out.println("Bytes Remaining: " + bytesRem);
    
                // for (int i = 0; i < plaintext_blockN.length; i++){
                //     System.out.println("Bytes in Plaintext: " + plaintext_blockN[i]);
                // }
    
                // for (byte i : ciphertext_blockN){
                //     System.out.println("Bytes in Curr Ciphertext: " + i);
                // }
    
    
                System.out.println("DONE");
    
                if (bytesRem == 16){
                    byte paddingByte = plaintext_blockN[plaintext_blockN.length-1];
                    System.out.println((int) paddingByte);
    
                    if (paddingByte == (byte)16){
                        return; //don't do anything
                    }
    
                    if (paddingByte > (byte) 0 && paddingByte < (byte) 16){
                            try {
                                for (int i = 0; i < plaintext_blockN.length - ((int)paddingByte); i++){
                                plainTextFile.write(plaintext_blockN[i]); 
    
                                // System.out.println("hi" + i);
                            }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                }
    
                if (bytesRem != 16){
                    //if there are more remaining, treat as it would have been for the rest of the code
                    printer(plaintext_blockN, plainTextFile);
                    // for (int i = 0; i < plaintext_blockN.length; i++){
                    //     System.out.println(plaintext_blockN[i]);
                    // }
                }
    
                ciphertext_blockN_1 = Arrays.copyOf(ciphertext_blockN, ciphertext_blockN.length);
    
                bytesRem = cipherTextFile.available();
        }
    }
    catch (IOException e) {
        e.printStackTrace();
    }
}
}
