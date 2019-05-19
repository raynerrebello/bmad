package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;

import java.util.Random;


public class HillClimber implements Heuristic{


    public Tuple<BooleanMatrix,BooleanMatrix> decomposition(BooleanMatrix C,int k,boolean xor,int numberRestarts,int bpp){
        if(numberRestarts<=1){
            return nextDescent(C,k,xor);
        }else{
            return randomRestarts(C,k,numberRestarts,xor);
        }
    }

    public Tuple<BooleanMatrix,BooleanMatrix> initialise(BooleanMatrix C, int k){
        int n = C.getHeight();
        int m = C.getWidth();
        boolean fillS = (n>m) ?  true:false;
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,k,0,0);
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(k,m,0,0);

        Random ran = new Random();


        if (fillS == true) {
            for (int j = 0; j < k ; j++) {
                int x = ran.nextInt(m);
                for (int i = 0; i < n; i++) {
                    S.update(i,j,C.apply(i,x));
                }
            }
        }else{

            for (int i = 0; i < k ; i++) {
                int x = ran.nextInt(n);
                for (int j = 0; j < m; j++) {
                    B.update(i,j,C.apply(x,j));
                }
            }

        }

        return new Tuple<>(S,B);
    }

    public Tuple<BooleanMatrix, BooleanMatrix> nextDescent(BooleanMatrix C,int k,boolean xor) {

        int MAX_ITERATIONS = 10000000;
        double minDifference = 0;

        boolean improved;
        int iter = 0;
        int n = C.getHeight();
        int m = C.getWidth();

        BooleanMatrix C_T = BooleanMatrix.deepTranspose(C);
        Tuple <BooleanMatrix,BooleanMatrix> init = initialise(C,k);
        BooleanMatrix S = init._1;
        BooleanMatrix B = init._2;
        long numberOfNeighbours = (long) (k*(n+m));
        long neighboursChecked = 0;
        boolean localMinimum = false;

        while (true) {
            while (true) {
                // Explore neighbourhood of 1 bit swaps of S
                improved = false; // keep track of if an improvement has been made in this neighbourhood

                for (int i = 0; i < n; i++) {

                    BooleanMatrix S_i = S.getRow(i);// still points to the same row
                    BooleanMatrix incumbentRow = S_i.booleanProduct(B, xor);
                    double incumbentRowError = C.getRow(i).reconstructionError(incumbentRow, 1d);

                    for (int j = 0; j < k; j++) {

                        S_i.update(j, BooleanMatrix.not(S_i.apply(j))); // flip bit.
                        BooleanMatrix rowResult = S_i.booleanProduct(B, xor);
                        double rowError = C.getRow(i).reconstructionError(rowResult, 1d);
                        neighboursChecked+=1;
                        if (incumbentRowError - rowError > minDifference) {
                            improved = true;
                            incumbentRowError = rowError;
                            neighboursChecked =0;
                        } else {
                            S_i.update(j, BooleanMatrix.not(S_i.apply(j))); // undo the change
                        }
                    }
                    if (neighboursChecked > numberOfNeighbours) {
                        localMinimum = true;
                        break;
                    }
                }

                if (localMinimum == true) {
                    break;
                }
                BooleanMatrix S_T = BooleanMatrix.deepTranspose(S);
                BooleanMatrix B_T = BooleanMatrix.deepTranspose(B);

                for (int i = 0; i < m; i++) {

                    BooleanMatrix B_Ti = B_T.getRow(i);// still points to the same row
                    BooleanMatrix incumbentRow = B_Ti.booleanProduct(S_T, xor);
                    double incumbentRowError = C_T.getRow(i).reconstructionError(incumbentRow, 1d);

                    for (int j = 0; j < k; j++) {

                        B_Ti.update(j, BooleanMatrix.not(B_Ti.apply(j))); // flip bit.
                        BooleanMatrix rowResult = B_Ti.booleanProduct(S_T, xor);
                        double rowError = C_T.getRow(i).reconstructionError(rowResult, 1d);
                        neighboursChecked+=1;
                        if (incumbentRowError - rowError > minDifference) {
                            improved = true;
                            incumbentRowError = rowError;
                            neighboursChecked=0;
                        } else {
                            B_Ti.update(j, BooleanMatrix.not(B_Ti.apply(j))); // undo the change.
                        }
                    }

                    if (neighboursChecked > numberOfNeighbours) {
                        localMinimum = true;
                        break;
                    }
                }

                S = BooleanMatrix.deepTranspose(S_T);
                B = BooleanMatrix.deepTranspose(B_T);

                if (!improved || iter > MAX_ITERATIONS) {
                    break;
                }
                if (localMinimum == true) {
                    break;
                }
                iter += 1;
            }

            return new Tuple<>(S, B);
        }
    }

    public Tuple<BooleanMatrix, BooleanMatrix> randomRestarts(BooleanMatrix C,int k,int numRestarts, boolean xor) {

        double bestError = 255;
        double error;
        Tuple<BooleanMatrix, BooleanMatrix> output;
        BooleanMatrix S = null;
        BooleanMatrix B = null;

        for (int i = 0; i < numRestarts; i++) {

            output = nextDescent(C, k, xor);
            error = C.relativeReconstructionError(output._1.booleanProduct(output._2),1d);
            if (error < bestError){
                bestError = error;
                S = output._1;
                B = output._2;
            }

            System.out.printf("\r %d out of %d restarts are complete with the best average recon = %f",i+1,numRestarts,bestError);
        }

        System.out.printf("\n Using %s, %s, Best_recon = %f%n",  xor? "XOR": " OR", "nextDescent", bestError);
        return new Tuple <> (S, B);

    }


}