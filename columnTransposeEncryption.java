import java.io.*;
import java.util.*;
import javax.swing.*;
import java.nio.*;
import java.nio.channels.FileChannel;

public class ctencrypt {

    public static void main(String [] args){

    //parsing the command line input stuff
    String[] commandLine = args;

    boolean encrypt = false;
    int blockSize = 16; // default block size
    String key = "";
    String file = "";

    for (int i = 0; i < commandLine.length; i++) {
        //checking for the component with blocksize
        if (commandLine[i].equals("-b")){
            //in the case we were given blocksize
            blockSize = Integer.parseInt(commandLine[i+1]);
            // i++; //increment so we don't relook at the blocksize number

            if (blockSize < 1){ //blocksize should be min 1.
                System.err.println("ERROR: Blocksize should be min length 1.");
                return; // exit program if blocksize is invalid
            }
        }

        //checking for component with key
        if (commandLine[i].equals("-k")) {
            key = commandLine[i+1];
            // i++; //increment to not consider it again

            if (key.length() < 1){//keysize should be atleast 1 char long
                System.err.println("ERROR: Key should be min length 1.");
                return; // exit program if keysize is invalid
            }
        }
    }


    int rows;
    if (blockSize%key.length() == 0){rows = blockSize/key.length();}
    else {rows = (blockSize/key.length()) + 1;}

    InputStream inputStream;

    try {
        //determine the file to read 
        if (commandLine.length == 0) {
            System.err.println("ERROR: Nothing to print.");
            return;
        }
        
        if (commandLine[commandLine.length-1].equals(key)){
            //if it is the same thing as the key, then need to read standard input
            inputStream = System.in; //file remains "";
            file = "Standard Input";
        }
        else {
            file = commandLine[commandLine.length-1];
            inputStream = new FileInputStream(file);
        }
        
        encrypt(inputStream, System.out, key, rows);

        inputStream.close();
    } catch (IOException e) {
        System.err.println("Error Reading Input: " + e.getMessage());
        System.exit(1);
    }


    OutputStream outputStream = System.out;

    // seeing the parsed values
    // for (String i : args){
    //     System.out.println(i);
    // }

    // System.out.println("Encrypt: " + encrypt);
    System.out.println("\n\nBlockSize: " + blockSize);
    System.out.println("Key: " + key);
    System.out.println("File: " + file);
    System.out.println("Row Length: " + rows);

    //Note: Row Length must be changed at some point partly due to the fact that

    }


    //COLUMN TRAVERSAL CODE
    private static int[] getColumnOrder(String key) {
        char[] keyChars = key.toCharArray();
        int[] colOrder = new int[keyChars.length];

        // create hashmap to store the last occurrence index of each character
        Map<Character, Integer> lastOccurrenceMap = new HashMap<>();
        for (int i = 0; i < keyChars.length; i++) {
            lastOccurrenceMap.put(keyChars[i], i);
        }

        // sort the characters by their byte vals
        Integer[] indices = new Integer[keyChars.length];
        for (int i = 0; i < keyChars.length; i++) {
            indices[i] = i;
        }
        Arrays.sort(indices, (a, b) -> {
            int cmp = Character.compare(keyChars[a], keyChars[b]);
            if (cmp == 0) {
                // if chars  equal, sort by last occurrences
                return Integer.compare(lastOccurrenceMap.get(keyChars[a]), lastOccurrenceMap.get(keyChars[b]));
            }
            return cmp;
        });

        // create a column order array
        for (int i = 0; i < keyChars.length; i++) {
            colOrder[indices[i]] = i;
        }

        return colOrder;
    }


    private static void encrypt(InputStream inputStream, OutputStream outputStream, String key, int row) throws IOException {
        //deal with the overall reading of SYSTEM.IN
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder st = new StringBuilder();

            //key.length() is the column length
            int col = key.length();
            byte [][] arr = new byte[row][col];
            char [][] charArr = new char[row][col];

            if (inputStream != System.in){
            ByteBuffer buffer = ByteBuffer.allocate(row*col);
            FileChannel channel = ((FileInputStream) inputStream).getChannel();

            int bytesRead;
            while ((bytesRead = channel.read(buffer)) != -1) {
                buffer.rewind();

                // int totallyFilled = 0;
                
                for (int i = 0; i < bytesRead; i++) {
                    int r = i / col;
                    int c = i % col;
                    arr[r][c] = buffer.get();
                    // if (arr[r][c] != 0) {totallyFilled++;}
                }

                /* Here I'll implement the column order component */
                byte [][] cipherTable = new byte[row][col];

                int[] order = getColumnOrder(key);
                for (int i = 0; i < row; i++){
                    for (int j = 0; j < col; j++){
                        cipherTable[i][order[j]] = arr[i][j];
                    }
                }

                //now we know total length, how many blocks filled, so print those accordingly
                for (int c = 0; c < col; c++){
                    for (int r = 0; r < row; r++){
                        if (cipherTable[r][c] != 0){
                            System.out.print(cipherTable[r][c]);
                            // System.out.print(" ");
                        }
                        cipherTable[r][c]=0; //after printing out set to zero
                        arr[r][c] = 0;
                    }
                    // System.out.println();
                }

                //to determine the blocks between - TESTING:
                // System.out.println();

                //CLEARING THE BUFFER - SEE IF THIS IS NEEDED OR NOT
                // buffer.clear();
            }
 

        }
    }

}