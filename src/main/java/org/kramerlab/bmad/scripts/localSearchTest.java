package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.algorithms.LocalSearch;
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
        LocalSearch algorithm = new LocalSearch(matrixA, 10);

        String FILENAME = "Results - LocalSearchTest.csv";
        String header = String.format("%n%s%n, Height, Width, Density, Dimension, Algorithm, ReconError_OR, ReconError_XOR%n", java.time.LocalDateTime.now());
        RunExperiment.writeResults(header, FILENAME);


        // Using nextDescent, with OR, then with XOR
        double nextDescent_OR = algorithm.decompose(1, false);
        double nextDescent_XOR = algorithm.decompose(1, true);

        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "nextDescent", nextDescent_OR, nextDescent_XOR), FILENAME);

        // Using steepDescent, with OR, then with XOR
        double steepDescent_OR = algorithm.decompose(2, false);
        double steepDescent_XOR = algorithm.decompose(2, true);

        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "steepDescent", nextDescent_OR, nextDescent_XOR), FILENAME);

        // Using randomRestarts, with OR, then with XOR
        double randomRestarts_nD_OR = algorithm.decompose(3, 10, true, false);
        double randomRestarts_nD_XOR = algorithm.decompose(3, 10, true, true);

        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "randomRestarts-nextDescent", randomRestarts_nD_OR, randomRestarts_nD_XOR), FILENAME);

        double randomRestarts_sD_OR = algorithm.decompose(3, 10, false, false);
        double randomRestarts_sD_XOR = algorithm.decompose(3, 10, false, true);

        RunExperiment.writeResults(String.format("%d, %d, %f, %d, %s, %f, %f%n", height, width, density, dimension, "randomRestarts-steepDescent", randomRestarts_sD_OR, randomRestarts_sD_XOR), FILENAME);
    }
}
