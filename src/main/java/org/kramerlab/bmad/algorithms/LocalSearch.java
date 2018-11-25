package org.kramerlab.bmad.algorithms;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;
import weka.core.Instances;
import java.util.*;


/**
 * Local Search algorithm, which uses a "hill-climbing" approach to reduce reconstruction error.
 * The neighbourhood of improvements is defined as the set of factorisations in which at most one bit deviates.
 *
 * There are two approaches: NEXT or STEEPEST descent. I
 */

public class LocalSearch {

    private BooleanMatrix C;
    private int k;

    // Constructor - takes a BooleanMatrix object
    public LocalSearch(BooleanMatrix a,int dimension) {
        this.C = a;
        this.k = dimension;
    }

    // Overloaded constructor, takes a Weka Instances object.
    public LocalSearch(Instances instanceA, int dimension) {
        this(new BooleanMatrix(instanceA), dimension);
    }


    /**
     * Main portal for performing decomposition.
     * Calls one of the three algorithms using either OR or XOR with booleanProduct.
     *
     * @param algorithm: 1, use nextDecent;  2, use steepDescent; 3, use randomRestarts.
     * @param numRestarts: for algorithm 3 randomRestarts only.
     * @param xor: true: booleanProduct uses XOR;  false: booleanProduct uses OR.
     * @return double relativeDeconstructionError.
     */
//
    public double decompose(int algorithm, int numRestarts, boolean nextDescent, boolean xor){
        try{
            if(algorithm == 1){
                return nextDescent(xor);
            }else if (algorithm == 2){
                return steepestDescent(xor);
            }else if (algorithm == 3) {
                return randomRestarts(numRestarts, nextDescent, xor);
            }
        }catch(Exception e){
            System.out.println("WARNING: There are only 3 algorithms: 1 = nextDescent, 2 = steepDescent, 3 = randomRestarts.");
            }
        return -999;
    }

    /**
     * Overloadd decompose method for calling nextDescent and steepDescent with only two parameters.
     */

    public double decompose(int algorithm, boolean xor){
        try{
            if(algorithm == 1){
                return nextDescent(xor);
            }else if(algorithm == 2){
                return steepestDescent(xor);
            }
        }catch(Exception e){
            System.out.println("WARNING: This version of decomposition method only calls two algorithms: 1 = nextDescent, 2 = steepDescent.");
        }
        return -999;
    }



    /**
     * Calls the decompose method while calculating the runtime.
     * @return a tuple of <(double) relativeReconstructionError, (long) runtime>.
     */

    public Tuple<Double, Long> decomposeWithRuntime(int algorithm, int numRestarts, boolean nextDescent, boolean xor){
        double relativeReconError;

        long startTime = System.nanoTime();

        relativeReconError = decompose(algorithm, numRestarts, nextDescent, xor);

        long stopTime = System.nanoTime();
        long runTime = stopTime - startTime;

        Tuple<Double, Long> result = new Tuple<Double, Long>(relativeReconError, runTime);
        return result;
    }

    /**
     * Overloaded decomposeWithRuntime that only calls nextDescent and steepDescent.
     * @return a tuple of <(double) relativeReconstructionError, (long) runtime>.
     */

    public Tuple<Double, Long> decomposeWithRuntime(int algorithm, boolean xor){
        double relativeReconError;

        long startTime = System.nanoTime();

        relativeReconError = decompose(algorithm, xor);

        long stopTime = System.nanoTime();
        long runTime = stopTime - startTime;

        Tuple<Double, Long> result = new Tuple<Double, Long>(relativeReconError, runTime);
        return result;
    }



    /**
     *  Hill-climbing method 1 - Take any old descent direction.
     *  Returns a (double) relativeReconstructionError.
     *  @param xor: boolean indicator for calling booleanProduct with XOR vs. OR.
     */

    public double nextDescent(boolean xor){

//        System.out.printf("%n--------------------Running nextDescent with %s--------------------%n", xor? "XOR": "OR");

        int n = this.C.getHeight();
        int m = this.C.getWidth();
        int it = 0;
        double relativeRecError;

        boolean improved = false;
        double density = Math.sqrt(this.C.getDensity());
        BooleanMatrix C_T = BooleanMatrix.deepTranspose(this.C);

        // Combination matrix
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,this.k,density,0d);
        // Basis matrix
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(this.k,m,density,0d);

        // Print out the initial guess and its recon error.
        BooleanMatrix randomResult = S.booleanProduct(B, xor);
        double randomError = this.C.reconstructionError(randomResult,1d);
//        System.out.println("The initial recon-error is: " + randomError);


        while(true){

            // Explore neighbourhood of 1 bit swaps of S
            improved = false; // keep track of if an improvement has been made in this neighbourhood
            for (int i = 0; i < n; i++){

                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B, xor);
                double incumbentRowError = this.C.getRow(i).reconstructionError(incumbentRow,1d);

                for (int j = 0; j < this.k; j++){

                    S_i.update(j, BooleanMatrix.not(S_i.apply(j))); // flip bit.
                    BooleanMatrix rowResult = S_i.booleanProduct(B, xor);
                    double rowError = this.C.getRow(i).reconstructionError(rowResult,1d);

                    if (rowError < incumbentRowError){
                        improved = true;
                    }else{
                        S_i.update(j, BooleanMatrix.not(S_i.apply(j))); // undo the change.
                    }
                }
            }

            BooleanMatrix S_T = BooleanMatrix.deepTranspose(S);
            BooleanMatrix B_T = BooleanMatrix.deepTranspose(B);

            for (int i = 0; i < m; i++){

                BooleanMatrix B_Ti = B_T.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = B_Ti.booleanProduct(S_T, xor);

                double incumbentRowError = C_T.getRow(i).reconstructionError(incumbentRow,1d);

                for (int j = 0; j < this.k; j++){

                    B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // flip bit.
                    BooleanMatrix rowResult = B_Ti.booleanProduct(S_T, xor);
                    double rowError = C_T.getRow(i).reconstructionError(rowResult,1d);

                    if (rowError < incumbentRowError){
                        improved = true;
                    }else{
                        B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // undo the change.
                    }
                }
            }

            S = BooleanMatrix.deepTranspose(S_T);
            B = BooleanMatrix.deepTranspose(B_T);

//            if(it%1==0) {
//                System.out.println("The recon-error is: " +
//                        Double.toString(this.C.relativeReconstructionError(S.booleanProduct(B, xor), 1d)));
//            }
            it = it +1;

            // stop when our solution is locally optimal wrt to this neighbourhood.
            if (!improved){
                relativeRecError = this.C.relativeReconstructionError(S.booleanProduct(B, xor),1d);
                System.out.println("The final-recon error is: " + relativeRecError);
                break;
            }
        }
        return relativeRecError;
    }




    /**
     *  Hill-climbing method 2 - Find the best descent direction.
     *  Returns a (double) relativeReconstructionError.
     * @param xor: boolean indicator for calling booleanProduct with XOR vs. OR.
     */

    public double steepestDescent(boolean xor){

//        System.out.printf("%n--------------------Running steepDescent with %s--------------------%n", xor? "XOR": "OR");

        int n = this.C.getHeight();
        int m = this.C.getWidth();
        double relativeRecError;

        double density = this.C.getDensity();
        BooleanMatrix C_T = BooleanMatrix.deepTranspose(this.C);

        // Combination matrix
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,this.k,density,0d);
        // Basis matrix
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(this.k,m,density,0d);

        // Print out the initial guess and its recon error.
        BooleanMatrix randomResult = S.booleanProduct(B, xor);
        double randomError = this.C.reconstructionError(randomResult,1d);
//        System.out.println("The initial recon-error is: " + randomError);

        int best_i;
        int best_j;
        boolean flipS = false;
        double incumbentErrorDelta;
        int it = 0;

        while(true){

            best_i = -1;
            best_j = -1;
            incumbentErrorDelta = 0d;

            // Explore neighbourhood of 1 bit swaps of S
            for (int i = 0; i < n; i++){

                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B, xor);
                double incumbentRowError = this.C.getRow(i).reconstructionError(incumbentRow,1d);

                for (int j = 0; j < this.k; j++){

                    S_i.update(j,BooleanMatrix.not(S_i.apply(j))); // flip bit.
                    BooleanMatrix rowResult = S_i.booleanProduct(B,xor);
                    double rowError = this.C.getRow(i).reconstructionError(rowResult,1d);

                    if (rowError - incumbentRowError < incumbentErrorDelta){
                        best_i = i;
                        best_j = j;
                        incumbentErrorDelta = rowError - incumbentRowError;
                        flipS = true;
                    }

                    S_i.update(j,BooleanMatrix.not(S_i.apply(j)));
                }
            }

            BooleanMatrix S_T = BooleanMatrix.deepTranspose(S);
            BooleanMatrix B_T = BooleanMatrix.deepTranspose(B);

            for (int i = 0; i < m; i++){

                BooleanMatrix B_Ti = B_T.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = B_Ti.booleanProduct(S_T, xor);

                double incumbentRowError = C_T.getRow(i).reconstructionError(incumbentRow,1d);

                for (int j = 0; j < this.k; j++){

                    B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // flip bit.
                    BooleanMatrix rowResult = B_Ti.booleanProduct(S_T, xor);
                    double rowError = C_T.getRow(i).reconstructionError(rowResult,1d);

                    if (rowError - incumbentRowError < incumbentErrorDelta){
                        best_i = j;
                        best_j = i;
                        incumbentErrorDelta = rowError - incumbentRowError;
                        flipS = false;
                    }

                    B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j)));
                }
            }

            it = it +1;

//            if(it % 1 == 0) {
//                relativeRecError = this.C.relativeReconstructionError(S.booleanProduct(B, xor), 1d);
//                System.out.println("The current recon-error is: " + relativeRecError);
//
//            }

            if (!(best_i < 0)){
                if(flipS == true){
                    S.update(best_i,best_j,BooleanMatrix.not(S.apply(best_i,best_j)));
                }else{
                    B.update(best_i,best_j,BooleanMatrix.not(B.apply(best_i,best_j)));
                }
            }else{
                relativeRecError = this.C.relativeReconstructionError(S.booleanProduct(B, xor), 1d);
//                System.out.println("The final-recon error is: " + relativeRecError);

                break;
            }
        }
        return relativeRecError;
    }


    /**
     * Try different initial conditions to find better local minima.
     * @param numRestarts
     * @param xor: true = use XOR in booleanProduct, false = use OR in booleanProduct
     */

    public double randomRestarts(int numRestarts, boolean nextDescent, boolean xor){

//        System.out.printf("%n--------------------Running randomRestarts with %s, %s--------------------%n", xor? "XOR": "OR",  nextDescent? "nextDescent": "steepDescent");

        double best_recon = 1;
        double r;
        for (int i = 0; i < numRestarts; i++) {
            if(nextDescent) {
                r = this.nextDescent(xor);
            }else{
                r = this.steepestDescent(xor);
            }
            if (r < best_recon){
                best_recon = r;
            }
        }


        System.out.printf("Using %s, %s, Best_recon = %f%n",  xor? "XOR": " OR", nextDescent? "nextDescent": "steepDescent", best_recon);
        return best_recon;
    }



}