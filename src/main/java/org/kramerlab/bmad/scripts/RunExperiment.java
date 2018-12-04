package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.scripts.MatrixFromFile;
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
import java.util.*;


/**
 * Comparing reconstruction error and runtime among: 1) different algorithms, 2) different tau values, 3) with OR vs. XOR in BooleanProduct function.
 */
public class RunExperiment {
    
    public static int height = 100;
    public static int width = 20;
    public static double density = 0.3;
    public static int dim = 2;
    public static double assocThreshold = 0.25;
    public static int numRestarts = 3;

    public static void main(String[] args)throws Exception{

//        Instances a = matrixFromFile();
//        System.out.printf("A: %n%s%n%n", a);

//        Instances b = matrixFromRandGen(height, width, density);
//        System.out.printf("B: %n%s%n%n", b);
        BooleanMatrix matrixC = MatrixFromFile.convert("Book1.csv", ",");
        Instances c = matrixC.toInstances();
//        System.out.println(matrixC);

        decompositionTest(c,dim, assocThreshold);
        }



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

    public static void decompositionTest(Instances instances, int dim, double assocThreshold) {

        BooleanMatrix matrixA = new BooleanMatrix(instances);

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

        long timeMilli = System.currentTimeMillis();
        final String FILENAME = String.format("results%s.csv",timeMilli);


        // Create new CSV file with unique file name, crate header row.
        String header = String.format("Date/Time, Height, Width, Density, Dimension, AssocThreshold, Tau, Algorithm, ReconError_OR, ReconError_XOR, Runtime_OR(nanoSec), Runtime_XOR(nanoSec)%n");
        writeResults(header, FILENAME);



        // Run decomposition with various tau value, on each algorithm, with BooleanProduct using both OR and XOR

        for (double tau = 0.15; tau < 0.35; tau += 0.01) {
//            String tauInfo = String.format("%n%n%n--------------------------- tau = %f ------------------------------%n", tau);
//            System.out.print(tauInfo);
//            writeResults(tauInfo, FILENAME);

            BooleanMatrixDecomposition allAlgorithms[] = {algorithm_LocIter, algorithm_DBP, algorithm_BEST_UNCONFIGURED, algorithm_BestConfigured};

            for (int pos = 0; pos < allAlgorithms.length; pos++) {
                BooleanMatrixDecomposition algorithm = allAlgorithms[pos];

//                String currentAlgorithm = String.format("%nAlgorithm used: %s%n", allAlgorithms[pos]);
//                writeResults(currentAlgorithm, FILENAME);
//                System.out.printf(currentAlgorithm);


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

                // draw the result
//            DecompositionLayout.showDecomposition(
//                    " tau = " + tau,
//                    instances,
//                    learnableRepresentation,
//                    basisRows
//            );

                // for calculation of errors,
                // convert everything to boolean matrices again
                BooleanMatrix a = new BooleanMatrix(instances);
                BooleanMatrix b = new BooleanMatrix(basisRows);
                BooleanMatrix c = new BooleanMatrix(learnableRepresentation);


                double reconErrorOR = a.relativeReconstructionError(c.booleanProduct(b), 1d);
                double reconErrorXOR = a.relativeReconstructionError(c.booleanProduct(b, true), 1d);

                // write the reconstruction errors to result file and print to console

//                String resultOR =  String.format("with  OR: Relative reconstruction error for tau = %f is %f, runtime = %d nanoSec.%n",
//                        tau,a.relativeReconstructionError(c.booleanProduct(b), 1d),totalTime);
//
//                String resultXOR =  String.format("with XOR: Relative reconstruction error for tau = %f is %f, runtime = %d nanoSec.%n",
//                        tau, a.relativeReconstructionError(c.booleanProduct(b, true), 1d), totalTime);
//
//                writeResults(resultOR, FILENAME);
//                System.out.print(resultOR);
//
//                writeResults(resultXOR, FILENAME);
//                System.out.print(resultXOR);


                // write results into file, one case per row.
                String result = String.format("%s /%s, %d, %d, %f, %d, %f, %f, %s, %f, %f, %d, %d%n", java.time.LocalDate.now(), java.time.LocalTime.now(),
                        height, width, density, dim, assocThreshold, tau, algorithm, reconErrorOR, reconErrorXOR, totalTime, totalTime);

                writeResults(result, FILENAME);

                }

            // -----------------------------------------
            // For each iteration, also run all versions of LocalSearch decompositions and write for comparison:
            // -----------------------------------------

            // Using nextDescent, with OR, then with XOR

            ArrayList<Tuple> output = new ArrayList<Tuple>();
            ArrayList<String> names = new ArrayList<String>();

            output.add(localSearch.decomposeWithRuntime(1, false));
            names.add("nextDescent");
            output.add(localSearch.decomposeWithRuntime(1, true));
            names.add("nextDescent");

            output.add(localSearch.decomposeWithRuntime(2, false));
            names.add("steepDescent");
            output.add(localSearch.decomposeWithRuntime(2, true));
            names.add("steepDescent");

            output.add(localSearch.decomposeWithRuntime(3, numRestarts, true, false));
            names.add(String.format("randRestart_%d_ND", numRestarts));
            output.add(localSearch.decomposeWithRuntime(3, numRestarts, true, true));
            names.add(String.format("randRestart_%d_ND", numRestarts));

            output.add(localSearch.decomposeWithRuntime(3, numRestarts, false, false));
            names.add(String.format("randRestart_%d_SD", numRestarts));
            output.add(localSearch.decomposeWithRuntime(3, numRestarts, false, true));
            names.add(String.format("randRestart_%d_SD", numRestarts));



            for(int i = 0; i < output.size(); i = i + 2){
                Tuple result_or = output.get(i);
                Tuple result_xor = output.get(i + 1);

                String error_or = result_or._1.toString();
                String runtime_or = result_or._2.toString();

                String error_xor = result_xor._1.toString();
                String runtime_xor = result_xor._2.toString();
                String name = names.get(i);


                writeResults(String.format("%s /%s, %d, %d, %f, %d, %f, %f, %s, %s, %s, %s, %s%n", java.time.LocalDate.now(), java.time.LocalTime.now(),
                        height, width, density, dim, assocThreshold, tau, name, error_or, error_xor, runtime_or, runtime_xor), FILENAME);
            }
        }
    }




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