package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;

public class EuclideanLocalSearch {

    public Tuple<BooleanMatrix,BooleanMatrix> decomposition(BooleanMatrix C,int k,boolean xor,int numberRestarts,int bpp){
        if(numberRestarts<=1){
            return nextDescent(C,k,bpp,xor);
        }else{
            return randomRestarts(C,k,numberRestarts,bpp,xor);
        }
    }

    public Tuple<BooleanMatrix, BooleanMatrix> nextDescent(BooleanMatrix C,int k,int bpp,boolean xor){

        int MAX_ITERATIONS = 10000000;
        double minDifference = 1e-3;
        double density = Math.sqrt(1 - Math.pow(1 - C.getDensity(),1./k));

        boolean improved;
        int iter = 0;
        int n = C.getHeight();
        int m = C.getWidth();

        BooleanMatrix C_T = BooleanMatrix.deepTranspose(C);

        // Combination matrix
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,k,density,0d);
        // Basis matrix
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(k,m,density,0d);

        while(true){
            // Explore neighbourhood of 1 bit swaps of S
            improved = false; // keep track of if an improvement has been made in this neighbourhood
            for (int i = 0; i < n; i++){

                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B, xor);
                double incumbentRowError = C.getRow(i).averageEuclideanReconstructionError(incumbentRow,bpp);

                for (int j = 0; j < k; j++){

                    S_i.update(j, BooleanMatrix.not(S_i.apply(j))); // flip bit.
                    BooleanMatrix rowResult = S_i.booleanProduct(B, xor);
                    double rowError = C.getRow(i).averageEuclideanReconstructionError(rowResult,bpp);

                    if (incumbentRowError-rowError > minDifference){
                        improved = true;
                        incumbentRowError = rowError;
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
                double incumbentRowError = C_T.getRow(i).averageEuclideanReconstructionError(incumbentRow,bpp);

                for (int j = 0; j < k; j++){

                    B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // flip bit.
                    BooleanMatrix rowResult = B_Ti.booleanProduct(S_T, xor);
                    double rowError = C_T.getRow(i).averageEuclideanReconstructionError(rowResult,bpp);

                    if (incumbentRowError-rowError > minDifference){
                        improved = true;
                        incumbentRowError = rowError;
                    }else{
                        B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // undo the change.
                    }
                }
            }

            S = BooleanMatrix.deepTranspose(S_T);
            B = BooleanMatrix.deepTranspose(B_T);

            if (!improved || iter > MAX_ITERATIONS){
                break;
            }
            iter+=1;

        }

        return new Tuple<>(S, B);
    }

    public Tuple<BooleanMatrix, BooleanMatrix> randomRestarts(BooleanMatrix C,int k,int numRestarts, int bpp, boolean xor) {

        double bestAverageEuclideanError = 255;
        double averageEuclideanError;
        Tuple<BooleanMatrix, BooleanMatrix> output;
        BooleanMatrix S = null;
        BooleanMatrix B = null;

        for (int i = 0; i < numRestarts; i++) {

            output = nextDescent(C,k,bpp,xor);
            averageEuclideanError = C.averageEuclideanReconstructionError(output._1.booleanProduct(output._2),bpp);
            if (averageEuclideanError < bestAverageEuclideanError){
                bestAverageEuclideanError = averageEuclideanError;
                S = output._1;
                B = output._2;
            }

            System.out.printf("\r %d out of %d restarts are complete with the best average recon = %f",i+1,numRestarts,bestAverageEuclideanError);
        }

        System.out.printf("\n Using %s, %s, Best_recon = %f%n",  xor? "XOR": " OR", "nextDescent", bestAverageEuclideanError);
        return new Tuple <> (S, B);


    }

}
