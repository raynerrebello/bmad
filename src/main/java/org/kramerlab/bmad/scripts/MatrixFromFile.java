package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.matrix.BooleanMatrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads  a file containing boolean values, returns a BooleanMatrix object.
 * Example of calling: BooleanMatrix b = MatrixFromFile.convert("Book1.csv", ",");
 */

public class MatrixFromFile {

//    public static void main(String[] args) throws Exception {
////        BooleanMatrix output = convert("test.txt", ", ");
//        BooleanMatrix output = convert("Book1.csv", ",");
//        System.out.println(output);
//    }

    /**
     * Reads a file of binary values, converts it into a row-major BooleanMatrix.
     *
     * @param FILENAME
     * @param delimiter: needs to strictly correspond the string segments between values, e.g. " " or ", "..
     * @return a BooleanMatrix object of bytes with value from file (True = 3, False = 0)
     * @throws Exception
     */

    public static BooleanMatrix convert(String FILENAME, String delimiter) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            ArrayList<byte[]> outputList = new ArrayList<byte[]>();
            String line;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(delimiter);
                ArrayList<Byte> temp = new ArrayList<Byte>();

                // parse each line into a byte array:
                // 1) parse each value into int, 2) encode (True = 3, False = 0), 3) cast into bytes.

                for (int i = 0; i < values.length; i++) {
                    int val = Integer.parseInt(values[i]);
                    temp.add(val == 1 ? (byte) 3 : (byte) 0);
                }
                byte[] tempArray = new byte[(temp.size())];

                for (int j = 0; j < values.length; j++) {
                    tempArray[j] = (byte) temp.get(j);
                }

                // Add each line (byte array) into the output arraylist.
                outputList.add(tempArray);
            }

            // Convert output arraylist into byte[][], feed into BooleanMatrix constructor.
            BooleanMatrix convertedMatrix = new BooleanMatrix(outputList.toArray(new byte[][]{{}}));
            return convertedMatrix;
        }
    }
}
