package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.algorithms.LocalSearch;
import org.kramerlab.bmad.algorithms.LocalSearchReturnMatrix;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;
import weka.core.Instances;

public class localSearchTest {

    /**
     * Testing the three LocalSearch decomposition methods, with "or" vs. "xor" cases.
     * Write results to a CSV file.
     */
    public static void main(String... args) throws Throwable{

//        BooleanMatrix C = RandomMatrixGeneration.randomMatrix(100,100,0.3,0d);
//        System.out.println(C + "\n");
//
//        LocalSearch ls = new LocalSearch(C,3);
//        ls.randomRestarts(10, true,true);
//        ls.randomRestarts(10, false,false);


        int height = 300;
        int width = 200;
        double density = 0.3;
        int dimension = 15;

        Instances matrixA = RunExperiment.matrixFromRandGen(100, 20, 0.3);
        LocalSearchReturnMatrix algorithm = new LocalSearchReturnMatrix(matrixA, 10);

        String FILENAME = "Results - LocalSearchTest.csv";
        String header = String.format("%n%s%n, Height, Width, Density, Dimension, Algorithm, ReconError_OR, ReconError_XOR%n", java.time.LocalDateTime.now());
        RunExperiment.writeResults(header, FILENAME);


//        // Using nextDescent, with OR, then with XOR
//        double nextDescent_OR = algorithm.decompose(1, false);
//        double nextDescent_XOR = algorithm.decompose(1, true);
//
//        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "nextDescent", nextDescent_OR, nextDescent_XOR), FILENAME);
//
//        // Using steepDescent, with OR, then with XOR
//        double steepDescent_OR = algorithm.decompose(2, false);
//        double steepDescent_XOR = algorithm.decompose(2, true);
//
//        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "steepDescent", nextDescent_OR, nextDescent_XOR), FILENAME);
//
//        // Using randomRestarts, with OR, then with XOR
//        double randomRestarts_nD_OR = algorithm.decompose(3, 10, true, false);
//        double randomRestarts_nD_XOR = algorithm.decompose(3, 10, true, true);
//
//        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "randomRestarts-nextDescent", randomRestarts_nD_OR, randomRestarts_nD_XOR), FILENAME);
//
//        double randomRestarts_sD_OR = algorithm.decompose(3, 10, false, false);
//        double randomRestarts_sD_XOR = algorithm.decompose(3, 10, false, true);
//
//        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "randomRestarts-steepDescent", randomRestarts_sD_OR, randomRestarts_sD_XOR), FILENAME);



// ------------------ Calling LocalSearchReturnMatrix -----------------------------



        // Using nextDescent, with OR, then with XOR

        Tuple<BooleanMatrix, BooleanMatrix> output1 = algorithm.decompose(1, false);
        double nextDescent_OR = algorithm.relativeRecError;

        Tuple<BooleanMatrix, BooleanMatrix> output2 = algorithm.decompose(1, true);
        double nextDescent_XOR = algorithm.relativeRecError;

        System.out.printf("Errors: nextDescent_OR = %f,  nextDescent_XOR = %f%n", nextDescent_OR, nextDescent_XOR);
        System.out.printf("%nextDescent_OR:%nMatrix S:%n%s%nMatrix B:%n%s%n", output1._1, output1._2);
        System.out.printf("%nextDescent_XOR:%nMatrix S:%n%s%nMatrix B:%n%s%n", output2._1, output2._2);

        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "nextDescent", nextDescent_OR, nextDescent_XOR), FILENAME);


//        // Using steepDescent, with OR, then with XOR

        Tuple<BooleanMatrix, BooleanMatrix> output3 = algorithm.decompose(2, false);
        double steepestDescent_OR = algorithm.relativeRecError;

        Tuple<BooleanMatrix, BooleanMatrix> output4 = algorithm.decompose(2, true);
        double steepestDescent_XOR = algorithm.relativeRecError;

        System.out.printf("Relative ReconErrors: steepestDescent_OR = %f,  steepestDescent_XOR = %f%n", steepestDescent_OR, steepestDescent_XOR);
        System.out.printf("%nsteepestDescent_OR:%nMatrix S:%n%s%nMatrix B:%n%s%n", output3._1, output3._2);
        System.out.printf("%nsteepestDescent_XOR:%nMatrix S:%n%s%nMatrix B:%n%s%n", output4._1, output4._2);

        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "steepDescent", nextDescent_OR, nextDescent_XOR), FILENAME);
//


//        // Using randomRestarts, with OR, then with XOR

        // with nextDescent:

        Tuple<BooleanMatrix, BooleanMatrix> output5 = algorithm.decompose(3, 10, true, false);
        double randRestart_nD_OR = algorithm.relativeRecError;

        Tuple<BooleanMatrix, BooleanMatrix> output6 = algorithm.decompose(3, 10, true, true);
        double randRestart_nD_XOR = algorithm.relativeRecError;

        System.out.printf("Relative ReconErrors: randRestart_OR = %f,  randRestart_XOR = %f%n", randRestart_nD_OR, randRestart_nD_XOR);
        System.out.printf("%nrandRestart_ND_OR:%nMatrix S:%n%s%nMatrix B:%n%s%n", output5._1, output5._2);
        System.out.printf("%nrandRestart_ND_XOR:%nMatrix S:%n%s%nMatrix B:%n%s%n", output6._1, output6._2);

        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "randomRestarts-nextDescent", randRestart_nD_OR, randRestart_nD_XOR), FILENAME);


        // with steepestDescent:

        Tuple<BooleanMatrix, BooleanMatrix> output7 = algorithm.decompose(3, 10, false, false);
        double randRestart_sD_OR = algorithm.relativeRecError;

        Tuple<BooleanMatrix, BooleanMatrix> output8 = algorithm.decompose(3, 10, false, true);
        double randRestart_sD_XOR = algorithm.relativeRecError;

        System.out.printf("Relative ReconErrors: randRestart_OR = %f,  randRestart_XOR = %f%n", randRestart_sD_OR, randRestart_sD_XOR);
        System.out.printf("%nrandRestart_SD_OR:%nMatrix S:%n%s%nMatrix B:%n%s%n", output7._1, output5._2);
        System.out.printf("%nrandRestart_SD_XOR:%nMatrix S:%n%s%nMatrix B:%n%s%n", output8._1, output6._2);

        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "randomRestarts-steepDescent", randRestart_sD_OR, randRestart_sD_XOR), FILENAME);





    }
}
