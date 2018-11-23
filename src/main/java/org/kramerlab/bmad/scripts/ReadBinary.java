package org.kramerlab.bmad.scripts;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.kramerlab.bmad.algorithms.BooleanMatrixDecomposition;
import org.kramerlab.bmad.algorithms.LocalSearch;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;

import java.io.*;


public class ReadBinary {

    public static void main(String... args) throws Throwable{
        File arr = new File("C:\\Users\\rayne\\IdeaProjects\\bmad\\src\\main\\java\\org\\kramerlab\\bmad\\scripts\\doge_450_6400.bin");
        InputStream inputStream = new FileInputStream(arr);
        long length = arr.length();

        byte[][] C = new byte[450][6400];

        System.out.println(length);

        for (int i = 0; i <450 ; i++) {

            for (int j = 0; j <800; j++) {
                byte[] bite = new byte[1];
                inputStream.read(bite);
                byte bit = bite[0];

                String s1 = String.format("%8s", Integer.toBinaryString(bit & 0xFF)).replace(' ', '0');

                for (int b = 0; b < s1.length(); b++) {
                    //System.out.print(s1.charAt(b)); // 10000001
                    if (s1.charAt(b) == '1') {
                        C[i][j*7+b] = (byte) 0;
                    } else {
                        C[i][j*7+b] = (byte) 3;
                    }
                }

            }


        }

        System.out.println("STARTING NOW");
        BooleanMatrix q = new BooleanMatrix(C);

        BooleanMatrix mat = BooleanMatrix.deepTranspose(q);
//
//        BooleanMatrixDecomposition bc = BooleanMatrixDecomposition.BEST_CONFIGURED(0.15);
//        Tuple<BooleanMatrix,BooleanMatrix> tb = bc.decompose(mat,100);
//        System.out.println(mat.relativeReconstructionError(tb._1.booleanProduct(tb._2,false),1d ));
        LocalSearch ls = new LocalSearch(mat,400);
        ls.randomRestarts(1000,true,false);

    }

}
