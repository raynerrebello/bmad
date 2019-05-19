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

public class TestXBMaD{
    public static void main(String[] args) throws Exception {
        int[] kValues = {1, 2, 4, 8, 16, 32, 64, 128, 256};
        int numberOfRepeats = 100;
        int n;
        int m;
        double reconError = 0;
        double targetDensity = 0;
        double coverage = 0;
        boolean exceeded = false;


        File folder = new File(".\\src\\main\\java\\org\\kramerlab\\bmad\\exp\\data");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < 8; i++) {

            if (listOfFiles[i].isFile()) {
                String name = listOfFiles[i].getName().split("/")[0];
                System.out.println(name);
                BooleanMatrix T = MatrixFromFile.convert(listOfFiles[i].getPath(), ",");
                targetDensity = T.getDensity();
                n = T.getHeight();
                m = T.getWidth();
                exceeded = false;
                XORDecompose xbmad = new XORDecompose(T);

                for (int k : kValues) {
                    if (k > Math.min(n, m)) {
                        if (exceeded == false) {
                            k = Math.min(n, m);
                            exceeded = true;
                        } else {
                            continue;
                        }
                    }
                    for (int j = 0; j < numberOfRepeats; j++) {

                        long startTime = System.nanoTime();

                        Tuple<BooleanMatrix, BooleanMatrix> ans = xbmad.iterativeDecompose(T, k, 2, 10);

                        long endTime = System.nanoTime();
                        double duration = (endTime - startTime) / 1e9;

                        BooleanMatrix R = ans._1.booleanProduct(ans._2);
                        reconError = T.relativeReconstructionError(R, 1d);
                        //1 - (residualMatrix.getDensity() / input.getDensity());
                        coverage = 1 - (reconError / targetDensity);
                        int[] stats = BooleanMatrix.getStats(T, R);

                        if (j != numberOfRepeats - 1) {
                            System.out.printf("\r %d of %d repeats for k = %d done.", j + 1, numberOfRepeats, k);
                        } else {
                            System.out.printf("\r %d of %d repeats for k = %d done.\n", j + 1, numberOfRepeats, k);
                        }
                        String filename = ".\\src\\main\\java\\org\\kramerlab\\bmad\\exp\\xorout\\" + name + ".txt";
                        String outputstring = String.format("%d,%d,%f,%f,%f,%f,%f,%d,%d,%d,%d\n", k, ans._1.getWidth(), reconError, coverage, targetDensity, R.getDensity(),
                                duration, stats[0], stats[1], stats[2], stats[3]);
                        File f = new File(filename);

                        PrintWriter out = null;
                        if (f.exists() && !f.isDirectory()) {
                            out = new PrintWriter(new FileOutputStream(new File(filename), true));
                        } else {
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
