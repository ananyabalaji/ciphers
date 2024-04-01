import java.util.*;
import java.io.*;

public class blockCipherEncryption{
    
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
                // If key is shorter than plaintext, use key[0] for remaining plaintext bytes
            //     blockCipher[i] = ((byte) (plaintext[i] ^ key[0]));
            // }
        }

        return blockCipher;
        
    }    

    public static void permutations(byte[] blockCipher, byte[] key) throws IOException {
        for (int i = 0; i < blockCipher.length; i++){
            int first = key[i] & 0xF; //lower 4 bits of the keystream
            int second = (key[i] >> 4) & 0xF; // Top 4 bits of the keystream

            //finishing up with the swap
            byte temp = blockCipher[first];
            blockCipher[first] = blockCipher[second];
            blockCipher[second] = temp;
        }

        // return blockCipher;
    }

    public static void printer(byte [] input, FileOutputStream cipherText){
        try {
            for (int i = 0; i < input.length; i++){
            cipherText.write(input[i]);
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
        String plaintextFilePath = args[1];
        String ciphertextFilePath = args[2];

        try (FileInputStream plainTextFile = new FileInputStream(plaintextFilePath);
            FileOutputStream cipherTextFile = new FileOutputStream(ciphertextFilePath)) {
            
            byte[] plaintext_block = new byte[16];

            int bytesRem = plainTextFile.available(); //16 is full size
            int bytesRead = plainTextFile.read(plaintext_block); //num of bytes read for a given block
            if (bytesRead == -1){
                System.err.println("Empty Input File");
                return;
            }

            //plaintext_block = plaintxt file

            //FIRST CASE = 16 Bytes
            //1. Generate IV
            byte[] IV = generateKey(sdbm(password), 16);
            byte [] keyStreamShuffle = generateKey(sdbm(password), 16);

            byte [] temp_blockN, ciphertext_blockN;
            byte [] ciphertext_blockN_1 = IV;

            if (bytesRem < 16){ 
                byte paddingBytes = (byte) (16 - bytesRem);

                for (int i = bytesRem; i < 16; i++){
                    plaintext_block[i] = paddingBytes;
                }

                temp_blockN = XOR_operation(plaintext_block, ciphertext_blockN_1);
                permutations(temp_blockN, keyStreamShuffle);
                ciphertext_blockN = XOR_operation(temp_blockN, keyStreamShuffle);

                // for (byte i : plaintext_block){
                //     System.out.println("Bytes in Curr Plaintext: " + i);
                // }

                printer(ciphertext_blockN, cipherTextFile);

                // for (byte i : ciphertext_blockN){
                //     System.out.println("Bytes in Curr Ciphertext: " + i);
                // }

                ciphertext_blockN_1 = ciphertext_blockN;
            }
            else if (bytesRem == 16){

                //if the number of bytes remaining is 16, then come up with a new block
                //block size overall is 16.
                byte newPad [] = new byte [16];
                for (int i = 0; i < 16; i++){
                    newPad[i] = (byte) 16; //no casting bc just numerical
                }

                temp_blockN = XOR_operation(plaintext_block, ciphertext_blockN_1);
                // keyStreamShuffle = generateKey(sdbm(password), 16);
                permutations(temp_blockN, keyStreamShuffle);
                ciphertext_blockN = XOR_operation(temp_blockN, keyStreamShuffle);
                printer(ciphertext_blockN, cipherTextFile);
                ciphertext_blockN_1 = ciphertext_blockN;

                // for (byte i : plaintext_block){
                //     System.out.println("Bytes in Curr Plaintext: " + i);
                // }

                // for (byte i : ciphertext_blockN){
                //     System.out.println("Bytes in Curr Ciphertext: " + i);
                // }

                //NOW RUN 2nd TIME FOR NEW BLOCK CREATED W PADDING
                
                //2. Apply CBC
                temp_blockN = XOR_operation(newPad, ciphertext_blockN_1);
                // keyStreamShuffle = generateKey(sdbm(password), 16);
                permutations(temp_blockN, keyStreamShuffle);
                ciphertext_blockN = XOR_operation(temp_blockN, keyStreamShuffle);
                printer(ciphertext_blockN, cipherTextFile);
                ciphertext_blockN_1 = ciphertext_blockN;

                // for (byte i : newPad){
                //     System.out.println("Bytes in New Plaintext: " + i);
                // }

                // for (byte i : ciphertext_blockN){
                //     System.out.println("Bytes in Curr Ciphertext: " + i);
                // }

            }
            else {
                temp_blockN = XOR_operation(plaintext_block, ciphertext_blockN_1);
                // keyStreamShuffle = generateKey(sdbm(password), 16);
                permutations(temp_blockN, keyStreamShuffle);
                ciphertext_blockN = XOR_operation(temp_blockN, keyStreamShuffle);
                printer(ciphertext_blockN, cipherTextFile);
                ciphertext_blockN_1 = ciphertext_blockN;

                // for (byte i : plaintext_block){
                //     System.out.println("Bytes in Curr Plaintext: " + i);
                // }

                // for (byte i : ciphertext_blockN){
                //     System.out.println("Bytes in Curr Ciphertext: " + i);
                // }
            }

            //amount of data available right after
            bytesRem = plainTextFile.available();

            //we can perform the remainder of the evaluation given the blockCipher 

            while ((bytesRead = plainTextFile.read(plaintext_block)) != -1) {


                if (bytesRem < 16){ 
                    byte paddingBytes = (byte) (16 - bytesRem);
    
                    for (int i = bytesRem; i < 16; i++){
                        plaintext_block[i] =  paddingBytes;
                    }

                    // for (int i = 0; i < 16; i++){
                    //     System.out.println("Block: " + plaintext_block[i]);
                    // }
    
                    temp_blockN = XOR_operation(plaintext_block, ciphertext_blockN_1);
                    permutations(temp_blockN, keyStreamShuffle);
                    ciphertext_blockN = XOR_operation(temp_blockN, keyStreamShuffle);
                    printer(ciphertext_blockN, cipherTextFile);
    
                    ciphertext_blockN_1 = ciphertext_blockN;

                    // for (byte i : plaintext_block){
                    //     System.out.println("Bytes in Curr Plaintext: " + i);
                    // }

                    // for (byte i : ciphertext_blockN){
                    //     System.out.println("Bytes in Curr Ciphertext: " + i);
                    // }
                }
                else if (bytesRem == 16){
    
                    //if the number of bytes remaining is 16, then come up with a new block
                    //block size overall is 16.
                    byte newPad [] = new byte [16];
                    for (int i = 0; i < 16; i++){
                        newPad[i] = (byte) 16; //no casting bc just numerical
                    }

    
                    temp_blockN = XOR_operation(plaintext_block, ciphertext_blockN_1);
                    // keyStreamShuffle = generateKey(sdbm(password), 16);
                    permutations(temp_blockN, keyStreamShuffle);
                    ciphertext_blockN = XOR_operation(temp_blockN, keyStreamShuffle);
                    printer(ciphertext_blockN, cipherTextFile);
                    ciphertext_blockN_1 = ciphertext_blockN;

                    // for (byte i : plaintext_block){
                    //     System.out.println("Bytes in Curr Plaintext: " + i);
                    // }

                    // for (byte i : ciphertext_blockN){
                    //     System.out.println("Bytes in Curr Ciphertext: " + i);
                    // }
    
                    //NOW RUN 2nd TIME FOR NEW BLOCK CREATED W PADDING
                    
                    //2. Apply CBC
                    temp_blockN = XOR_operation(newPad, ciphertext_blockN_1);
                    // keyStreamShuffle = generateKey(sdbm(password), 16);
                    permutations(temp_blockN, keyStreamShuffle);
                    ciphertext_blockN = XOR_operation(temp_blockN, keyStreamShuffle);
                    printer(ciphertext_blockN, cipherTextFile);
                    ciphertext_blockN_1 = ciphertext_blockN;

                    // for (int i = 0; i < 16; i++){
                    //     System.out.println("Bytes in New Plaintext: " + newPad[i]);
                    // }

                    // for (byte i : ciphertext_blockN){
                    //     System.out.println("Bytes in Curr Ciphertext: " + i);
                    // }
                }
                else {
                    temp_blockN = XOR_operation(plaintext_block, ciphertext_blockN_1);
                    // keyStreamShuffle = generateKey(sdbm(password), 16);
                    permutations(temp_blockN, keyStreamShuffle);
                    ciphertext_blockN = XOR_operation(temp_blockN, keyStreamShuffle);
                    printer(ciphertext_blockN, cipherTextFile);
                    ciphertext_blockN_1 = ciphertext_blockN;

                    // for (byte i : plaintext_block){
                    //     System.out.println("Bytes in Curr Plaintext: " + i);
                    // }

                    // for (byte i : ciphertext_blockN){
                    //     System.out.println("Bytes in Curr Ciphertext: " + i);
                    // }
                }

                bytesRem = plainTextFile.available();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
