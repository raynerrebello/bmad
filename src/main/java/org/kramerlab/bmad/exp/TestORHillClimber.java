package org.kramerlab.bmad.exp;

import org.kramerlab.bmad.algorithms.HillClimber;
import org.kramerlab.bmad.algorithms.XORDecompose;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.scripts.MatrixFromFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class TestORHillClimber {
    public static void main(String[] args) throws Exception {
        int[] kValues = {1, 2, 4, 8, 16, 32, 64, 128, 256};
        int n;
        int m;
        double reconError = 0;
        double targetDensity = 0;
        double coverage = 0;
        HillClimber hillClimber;
        hillClimber = new HillClimber();
        File folder = new File("./src/main/java/org/kramerlab/bmad/exp/datasets");
        File[] listOfFiles = folder.listFiles();

        for (int j = Integer.parseInt(args[0]); j < Integer.parseInt(args[1]); j++) {

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String name = listOfFiles[i].getName().split("\\.")[0];
                    System.out.println(name);
                    BooleanMatrix T = MatrixFromFile.convert(listOfFiles[i].getPath(), ",");
                    targetDensity = T.getDensity();
                    n = T.getHeight();
                    m = T.getWidth();

                    for (int k : kValues) {

                        if (k > Math.min(n, m)) {
                            continue;
                        }

                        long startTime = System.nanoTime();

                        Tuple<BooleanMatrix, BooleanMatrix> ans = hillClimber.decomposition(T, k, false, 10,8);

                        long endTime = System.nanoTime();

                        double duration = (endTime - startTime) / 1e9;
                        BooleanMatrix R = ans._1.booleanProduct(ans._2);
                        reconError = T.relativeReconstructionError(R, 1d);

                        //1 - (residualMatrix.getDensity() / input.getDensity());
                        coverage = 1 - (reconError / targetDensity);
                        int[] stats = BooleanMatrix.getStats(T, R);

                        String filename = String.format("./src/main/java/org/kramerlab/bmad/exp/hcorout/k_%d_r_%d_%s.txt",k,(j+1),name);
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

            System.out.println(String.format("%d of %d restarts done...",(j+1),Integer.parseInt(args[1])-Integer.parseInt(args[0]) ));

        }
    }
    }
