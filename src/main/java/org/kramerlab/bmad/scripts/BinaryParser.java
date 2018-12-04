package org.kramerlab.bmad.scripts;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import java.io.*;
import java.io.FileOutputStream;
import java.util.BitSet;

public class BinaryParser {

    // Import an n * m BooleanMatrix from a binary file.
    public static BooleanMatrix binaryToBooleanMatrix(String relativePathToFile,int n,int m) throws Throwable{
        String basePath = new File("").getAbsolutePath();
        String path = basePath + relativePathToFile;
        File file = new File(path);

        InputStream inputStream = new FileInputStream(file);
        long streamLength = file.length();
        byte[][] C = new byte[n][m];

        int lastBits = (int) (8*streamLength -n*m);
        int i = 0;
        int j =0;

        while (inputStream.available()!=0) {
            int bufferSize = Math.min(inputStream.available(),10240 * 1024);
            byte[] buffer = new byte[bufferSize];
            inputStream.read(buffer);
            for (int k = 0; k <bufferSize ; k++) {
                byte bite = buffer[k];
                String paddedString = String.format("%8s", Integer.toBinaryString(bite
                        & 0xFF)).replace(' ', '0');
                int numBits = i == (n-1) ? 8 - lastBits : 8;
                for (int bit = 0; bit < numBits; bit++) {
                    C[i][j] =  paddedString.charAt(bit) == '1' ? (byte) 3: (byte) 0;

                    if(j != (m-1)){
                        j++;
                    }else{
                        if(i !=(n-1)){
                            i++;
                            j=0;
                        }else{
                            break;
                        }
                    }
                }
            }

        }

        return new BooleanMatrix(C);
    }

    public static void booleanMatrixToBinary(BooleanMatrix C, String relativePathToFile) throws FileNotFoundException {
        String basePath = new File("").getAbsolutePath();
        String path = basePath + relativePathToFile;
        File file = new File(path);
        FileOutputStream fos = new FileOutputStream(file);

        long bitsToGo = (long) C.getHeight()*C.getWidth();// Keeps track of the bits left.
        long totalBits = (long) C.getHeight()*C.getWidth();//
        int i =0;int j=0;
        long counter = 0;
        boolean done = false;

        // Number of bytes to write at a time (set to the closet whole number of bytes)
        int chunkSize = (int) totalBits/8;

        while(true){

            // Write bytes in chunks, if
            int numBytes = bitsToGo/8 > chunkSize ? chunkSize : (int) Math.ceil(bitsToGo/8d);

            // buffer to store all the bytes to be written.
            byte [] buffer = new byte[numBytes];

            // fill the buffer, one byte at a time.
            for (int k = 0; k <numBytes ; k++) {
                String binaryRepresentation = "";
                for (int l = 0; l < 8; l++) {
                    if (!done) {
                        binaryRepresentation += C.apply(i, j) == (byte) 3 ? "1" : "0"; // Write 3 -> 1 otherwise its 0.
                    } else {
                        binaryRepresentation += "0"; // Pad byte with zeros, if the matrix is fully represented.
                    }

                    // increment i,j
                    if (j != (C.getWidth() - 1)) {
                        j++;
                    } else {
                        if (i != (C.getHeight() - 1)) {
                            i++;
                            j = 0;
                        } else {
                            done = true;
                        }
                    }

                }
                buffer[k] = (byte) Integer.parseInt(binaryRepresentation,2);
            }

            // write buffer to file.
            try{
                fos.write(buffer);
            }catch(Exception e ){
                System.out.println(e);
            }

            // if the buffer is complete, terminate the loop.
            if(done){
                break;
            }

            // Keep track of bits left to write.
            bitsToGo -= numBytes*8;
        }

        try{
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done writing to file.");


    }

}
