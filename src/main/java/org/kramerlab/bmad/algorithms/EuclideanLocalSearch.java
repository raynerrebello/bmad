package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;
import weka.core.Instances;

public class EuclideanLocalSearch {
    private BooleanMatrix C;
    private int k;
    public double relativeRecError;

    // Constructor - takes a BooleanMatrix object
    public EuclideanLocalSearch(BooleanMatrix a,int dimension) {
        this.C = a;
        this.k = dimension;
    }

    public Tuple<BooleanMatrix, BooleanMatrix> nextDescent(int bpp,boolean xor){

        int n = this.C.getHeight();
        int m = this.C.getWidth();
        int MAX_ITERATIONS = 10;
        int iter = 0;
        double tol = 1e-4;

        boolean improved;
        double density = Math.sqrt(this.C.getDensity());
        BooleanMatrix C_T = BooleanMatrix.deepTranspose(this.C);

        // Combination matrix
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,this.k,density*density,0d);
        // Basis matrix
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(this.k,m,density,0d);

        while(true){

            // Explore neighbourhood of 1 bit swaps of S
            improved = false; // keep track of if an improvement has been made in this neighbourhood
            for (int i = 0; i < n; i++){

                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B, xor);
                double incumbentRowError = BooleanMatrix.averageEuclideanReconstructionError(this.C.getRow(i),incumbentRow,bpp);

                for (int j = 0; j < this.k; j++){

                    S_i.update(j, BooleanMatrix.not(S_i.apply(j))); // flip bit.
                    BooleanMatrix rowResult = S_i.booleanProduct(B, xor);
                    double rowError = BooleanMatrix.averageEuclideanReconstructionError(this.C.getRow(i),rowResult,bpp);

                    if (incumbentRowError-rowError > tol){
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

                double incumbentRowError = BooleanMatrix.averageEuclideanReconstructionError(C_T.getRow(i),incumbentRow,bpp);

                for (int j = 0; j < this.k; j++){

                    B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // flip bit.
                    BooleanMatrix rowResult = B_Ti.booleanProduct(S_T, xor);
                    double rowError = BooleanMatrix.averageEuclideanReconstructionError(C_T.getRow(i),rowResult,bpp);

                    if (incumbentRowError-rowError > tol){
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
                relativeRecError = BooleanMatrix.averageEuclideanReconstructionError(C,S.booleanProduct(B),bpp);
                break;
            }
            iter+=1;
        }
        //System.out.println("BEST->" + relativeRecError);
        return new Tuple<BooleanMatrix, BooleanMatrix>(S, B);
    }

    public Tuple<BooleanMatrix, BooleanMatrix> randomRestarts(int numRestarts, int bpp, boolean xor) throws InterruptedException{

        double best_recon = 255;
        Tuple<BooleanMatrix, BooleanMatrix> output;
        BooleanMatrix S = null;
        BooleanMatrix B = null;

        for (int i = 0; i < numRestarts; i++) {

            output = nextDescent(bpp,xor);

            if (relativeRecError < best_recon){
                best_recon = relativeRecError;
                S = output._1;
                B = output._2;
            }

            System.out.printf("\r %d out of %d restarts are complete with the best average recon = %f",i+1,numRestarts,best_recon);
        }


        System.out.printf("\n Using %s, %s, Best_recon = %f%n",  xor? "XOR": " OR", "nextDescent", best_recon);
        relativeRecError = best_recon;
        return new Tuple <BooleanMatrix, BooleanMatrix> (S, B);


    }

}
