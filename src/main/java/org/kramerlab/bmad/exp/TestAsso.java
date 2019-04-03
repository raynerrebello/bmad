package org.kramerlab.bmad.exp;

import org.kramerlab.bmad.algorithms.BooleanMatrixDecomposition;
import org.kramerlab.bmad.algorithms.XORDecompose;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.scripts.BinaryParser;
import org.kramerlab.bmad.scripts.MatrixFromFile;
import org.kramerlab.bmad.visualization.DecompositionLayout;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestAsso {
    public static void main(String[] args) throws Exception{
        int[] kValues = {5,10,25,50,75,100};
        int numberOfRepeats = 100;
        int n;
        int m;
        double reconError = 0;
        double targetDensity = 0;
        double coverage = 0;

        BooleanMatrixDecomposition bestUnconfig = BooleanMatrixDecomposition.BEST_UNCONFIGURED;
        File folder = new File(".\\src\\main\\java\\org\\kramerlab\\bmad\\exp\\data");
        File[] listOfFiles = folder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String name= listOfFiles[i].getName().split("\\.")[0];
                    System.out.println(name);
                    BooleanMatrix T = MatrixFromFile.convert(listOfFiles[i].getPath(),",");
                    targetDensity = T.getDensity();
                    n = T.getHeight();
                    m = T.getWidth();

                        for (int k:kValues) {
                            if (k > Math.min(n,m)) {
                                continue;
                            }
                            for (int j = 0; j < numberOfRepeats; j++) {
                            Tuple<BooleanMatrix,BooleanMatrix> ans = bestUnconfig.decompose(T,k);
                            BooleanMatrix R = ans._1.booleanProduct(ans._2);
                            reconError = T.relativeReconstructionError(R,1d);
                            //1 - (residualMatrix.getDensity() / input.getDensity());
                            coverage = 1 - (reconError/targetDensity);

                            System.out.printf(" For %s: k = %d   coverage =  %f   error = %n",name,k,coverage);

                            String filename= ".\\src\\main\\java\\org\\kramerlab\\bmad\\exp\\assoout\\" + name +".txt";
                            String outputstring = String.valueOf(k) + "," + String.valueOf(reconError) + ","  + String.valueOf(coverage) + "\n";
                            File f = new File(filename);

                            PrintWriter out = null;
                            if ( f.exists() && !f.isDirectory() ) {
                                out = new PrintWriter(new FileOutputStream(new File(filename), true));
                            }
                            else {
                                out = new PrintWriter(filename);
                            }
                            out.append(outputstring);
                            out.close();

                        }
                    }

                }
            }

    }
}
