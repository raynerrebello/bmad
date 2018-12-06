package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.CathyLocal.XORDecomposeWithPrint;
import org.kramerlab.bmad.algorithms.*;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static java.lang.Math.max;


/**
 * Comparing reconstruction error and runtime among: 1) different algorithms, 2) different tau values, 3) with OR vs. XOR in BooleanProduct function.
 */
public class RunExperiment {
//    private static long timeMilli = System.currentTimeMillis();
//    private static final String FILENAME = String.format("results%s.csv",timeMilli);

//    private static int height = 50;
//    private static int width = 20;
//    private static double density = 0.3;
//    private static int dim = 2;
//    private static double assocThreshold = 0.25;
    private static int numRestarts = 3;
    public enum matrixType {matrixFromFile, matrixFromRandomGen, centredFramedMatrix, userDefinedFramedMatrix, squareChessBoard};



//    public static void main(String[] args)throws Exception{
//
//        long timeMilli = System.currentTimeMillis();
//        final String FILENAME = String.format("results%s.csv",timeMilli);
//
//        Instances a = MatrixFromFile.convert("Book1.csv", ",").toInstances();
//        decompositionTest(FILENAME, matrixType.matrixFromFile.toString(), a, dim, assocThreshold);
//
//        Instances b = matrixFromRandGen(height, width, density);
//        decompositionTest(FILENAME, matrixType.matrixFromRandomGen.toString(), b, dim, assocThreshold);
//
//
//        BooleanMatrix matrixC = GridMatrixGenerator.getCentredFramedGrid(3, 4, 3, 4, true);
//        GridMatrixGenerator.addNoiseToAll(matrixC, 0.2);
//        Instances c = matrixC.toInstances();
//        decompositionTest(FILENAME, matrixType.centredFramedMatrix.toString(), c,dim, assocThreshold);
//
//
//        BooleanMatrix matrixD = GridMatrixGenerator.getAnyFramedGrid(10, 20, new int[]{3, 2, 8, 15}, true);
//        GridMatrixGenerator.addNoiseToAll(matrixD, 0.2);
//        Instances d = matrixD.toInstances();
//        decompositionTest(FILENAME, matrixType.userDefinedFramedMatrix.toString(), d,dim, assocThreshold);
//
//
//
//        BooleanMatrix matrixE = GridMatrixGenerator.getSquareChessBoard(5, 3, false);
//        GridMatrixGenerator.addNoiseToAll(matrixE, 0.2);
//        Instances e = matrixE.toInstances();
//        decompositionTest(FILENAME, matrixType.squareChessBoard.toString(), e,dim, assocThreshold);
//
//
//        }



    /**
     * returns a weka instance from data file
     */

    public static Instances matrixFromFile(){
        for(String file:new String[]{
                // "datasets/kinase_labels.arff",
                // "datasets/labels.nci.disc.noid.arff",
                // "datasets/labels.gene.arff",
                "datasets/demo.arff"}) {

            Instances instances = null;
            try {
                // load some data
                instances = new DataSource(file).getDataSet();
                return instances;

            } catch (Exception e) {
                System.out.println(
                        "The file '" + file +
                                "' is probably not in the current directory."
                );
                System.out.println("Full stack trace: ");
                e.printStackTrace();
            }
        }
        return null;
    }



    /**
     * returns a weka instance from randomly generated BooleanMatrix objects,
     * with specified height, width, density.
     */

    public static Instances matrixFromRandGen(int height, int width, double density) {
        BooleanMatrix randMatrixA = RandomMatrixGeneration.randomMatrix(height, width, density,0);
        Instances b = randMatrixA.toInstances();
        return b;
    }




    /**
     * different DBP-based sub-alrogithms on boolean matrix decomposition.
     */

    public static void decompositionTest(String FILENAME, String typeName, Instances instances, int dim, double assocThreshold) {

        BooleanMatrix matrixA = new BooleanMatrix(instances);
        int height = matrixA.getHeight();
        int width = matrixA.getWidth();
        double density = matrixA.getDensity();

        // creating different decomposition algorithms
        // in same sequence as in BooleanMatrixDecomposition

        LocalSearch localSearch = new LocalSearch(instances, dim);

        BooleanMatrixDecomposition algorithm_LocIter =
                new BooleanMatrixDecomposition(
                        new IdentityGenerator(),
                        new FastLoc(),
                        new Iter(), 1d);


        BooleanMatrixDecomposition algorithm_DBP =
                new BooleanMatrixDecomposition(
                        new AssociationGenerator(0.5),
                        new GreedySelector(),
                        new IdentityCombinator(), 1d);

        BooleanMatrixDecomposition algorithm_BEST_UNCONFIGURED =
                new BooleanMatrixDecomposition(
                        new IdentityGenerator(),
                        new FastLoc(),
                        new CombinatorPipeline(new DensityGreedyCombinator(),
                                new Iter()), 1d);

        BooleanMatrixDecomposition algorithm_BestConfigured = BooleanMatrixDecomposition.BEST_CONFIGURED(assocThreshold);


        // Create new file name, write the current call's date, time to differentiate from others


        // Create new CSV file with unique file name, crate header row.
        String header = String.format("Date/Time, MatrixTypeName, Height, Width, Density, Dimension, AssocThreshold, Tau, Algorithm, ReconError_OR, ReconError_XOR, Runtime_OR(nanoSec), Runtime_XOR(nanoSec), colMatrix_size, rowMatrix_size%n");
        writeResults(header, FILENAME);


        // Run decomposition with various tau value, on each algorithm, with BooleanProduct using both OR and XOR

//        for (double tau = 0.15; tau < 0.35; tau += 0.01) {
        double tau = 0.3;

//            String tauInfo = String.format("%n%n%n--------------------------- tau = %f ------------------------------%n", tau);
//            System.out.print(tauInfo);
//            writeResults(tauInfo, FILENAME);

        BooleanMatrixDecomposition allAlgorithms[] = {algorithm_LocIter, algorithm_DBP, algorithm_BEST_UNCONFIGURED, algorithm_BestConfigured};


        for (int pos = 0; pos < allAlgorithms.length; pos++) {
            BooleanMatrixDecomposition algorithm = allAlgorithms[pos];

            String currentAlgorithm = String.format("%n%s, Algorithm used: %s%n", java.time.LocalTime.now(), allAlgorithms[pos]);
//                writeResults(currentAlgorithm, FILENAME);
            System.out.printf(currentAlgorithm);


            // decompose

            // starting time - for computing run-time
            long startTime = System.nanoTime();

            Tuple<Instances, Instances> t = algorithm.decompose(instances,
                    max(instances.numAttributes() / 100, dim));

            long stopTime = System.nanoTime();
            long totalTime = stopTime - startTime;

            // notice, that the decompose() method is "the right way round",
            // from Weka's point of view
            Instances basisRows = t._2;
            Instances learnableRepresentation = t._1;


            // for calculation of errors,
            // convert everything to boolean matrices again
            BooleanMatrix a = new BooleanMatrix(instances);
            BooleanMatrix b = new BooleanMatrix(basisRows);
            BooleanMatrix c = new BooleanMatrix(learnableRepresentation);

            double reconErrorOR = a.relativeReconstructionError(c.booleanProduct(b), 1d);
            double reconErrorXOR = a.relativeReconstructionError(c.booleanProduct(b, true), 1d);

            // write results into file, one case per row.
            String result = String.format("%s /%s, %s, %d, %d, %f, %d, %f, %f, %s, %f, %f, %d, %d%n", java.time.LocalDate.now(), java.time.LocalTime.now(),
                    typeName, height, width, density, dim, assocThreshold, tau, algorithm, reconErrorOR, reconErrorXOR, totalTime, totalTime);

            writeResults(result, FILENAME);
        }


        // -----------------------------------------
        //    Use XORDecompose:
        // -----------------------------------------

        XORDecompose xorDec = new XORDecompose(matrixA);


        String currentAlgorithm = String.format("%n%s, Algorithm used: %s%n", java.time.LocalTime.now(), "XORDecompose");
//                writeResults(currentAlgorithm, FILENAME);
        System.out.printf(currentAlgorithm);


        // decompose

        // starting time - for computing run-time
        long startTime = System.nanoTime();

        Tuple<BooleanMatrix, BooleanMatrix> output1 = xorDec.iterativeDecompose(matrixA, dim, 10);

        long stopTime = System.nanoTime();
        long totalTime = stopTime - startTime;

        // notice, that the decompose() method is "the right way round",
        // from Weka's point of view
        BooleanMatrix rowMatrix = output1._2;
        BooleanMatrix colMatrix = output1._1;

        double reconErrorXOR = xorDec.relativeRecError;

        // write results into file, one case per row.
        String result = String.format("%s /%s, %s, %d, %d, %f, %d, %f, %f, %s, %f, %f, %d, %d, %s, %s%n", java.time.LocalDate.now(), java.time.LocalTime.now(),
                typeName, height, width, density, dim, assocThreshold, tau, "XorDecomposeIter10", reconErrorXOR, reconErrorXOR, totalTime, totalTime,
                colMatrix.size(), colMatrix.size());

        writeResults(result, FILENAME);
    }




            // -----------------------------------------
            // Use LocalSearch - For each iteration, also run all versions of LocalSearch decompositions and write for comparison:
            // -----------------------------------------
//
//            // Using nextDescent, with OR, then with XOR

//            ArrayList<Tuple> output = new ArrayList<Tuple>();
//            ArrayList<String> names = new ArrayList<String>();
//
//            output.add(localSearch.decomposeWithRuntime(1, false));
//            names.add("nextDescent");
//            output.add(localSearch.decomposeWithRuntime(1, true));
//            names.add("nextDescent");
//
////            output.add(localSearch.decomposeWithRuntime(2, false));
////            names.add("steepDescent");
////            output.add(localSearch.decomposeWithRuntime(2, true));
////            names.add("steepDescent");
//
//            output.add(localSearch.decomposeWithRuntime(3, numRestarts, true, false));
//            names.add(String.format("randRestart_%d_ND", numRestarts));
//            output.add(localSearch.decomposeWithRuntime(3, numRestarts, true, true));
//            names.add(String.format("randRestart_%d_ND", numRestarts));
//
////            output.add(localSearch.decomposeWithRuntime(3, numRestarts, false, false));
////            names.add(String.format("randRestart_%d_SD", numRestarts));
////            output.add(localSearch.decomposeWithRuntime(3, numRestarts, false, true));
////            names.add(String.format("randRestart_%d_SD", numRestarts));
//
//
//
//            for(int i = 0; i < output.size(); i = i + 2){
//                Tuple result_or = output.get(i);
//                Tuple result_xor = output.get(i + 1);
//
//                String error_or = result_or._1.toString();
//                String runtime_or = result_or._2.toString();
//
//                String error_xor = result_xor._1.toString();
//                String runtime_xor = result_xor._2.toString();
//                String name = names.get(i);
//
//
//                writeResults(String.format("%s /%s, %s, %d, %d, %f, %d, %f, %f, %s, %s, %s, %s, %s%n", java.time.LocalDate.now(), java.time.LocalTime.now(),
//                        typeName, height, width, density, dim, assocThreshold, tau, name, error_or, error_xor, runtime_or, runtime_xor), FILENAME);
//            }
//        }



//    }




    /**
     * writes input content (string) into a file continuously without overwrite.
     */

    public static void writeResults(String content, String FILENAME){
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
             fw = new FileWriter(FILENAME, true);
             bw = new BufferedWriter(fw);
             bw.write(content);

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }






















}