package org.kramerlab.bmad.algorithms;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;


/**
 * Local Search algorithm, which uses a "hill-climbing" approach to reduce reconstruction error.
 * The neighbourhood of improvements is defined as the set of factorisations in which at most one bit deviates.
 *
 * There are two approaches: NEXT or STEEPEST descent. I
 */

public class LocalSearch {

    private BooleanMatrix C;
    private int k;
    public boolean nextDescent;

    public LocalSearch(BooleanMatrix a,int dimension, boolean nextDescent ) {
        super();
        this.C = a;
        this.k = dimension;
    }

    public void decompose(){

    }

    // Take any old descent direction.
    public double nextDescent(){

        int n = this.C.getHeight();
        int m = this.C.getWidth();
        int it = 0;
        boolean improved = false;
        double density = this.C.getDensity();
        BooleanMatrix C_T = BooleanMatrix.deepTranspose(this.C);

        // Combination matrix
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,this.k,density,0d);
        // Basis matrix
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(this.k,m,density,0d);

        // Print out the initial guess and its recon error.
        BooleanMatrix randomResult = S.booleanProduct(B);
        double randomError = this.C.reconstructionError(randomResult,1d);
        //System.out.print("The initial recon-error is: " + Double.toString(randomError));


        while(true){

            // Explore neighbourhood of 1 bit swaps of S
            improved = false; // keep track of if an improvement has been made in this neighbourhood
            for (int i =0;i<n;i++){

                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B);
                double incumbentRowError = this.C.getRow(i).reconstructionError(incumbentRow,1d);

                for (int j =0;j<this.k;j++){

                    S_i.update(j,BooleanMatrix.not(S_i.apply(j))); // flip bit.
                    BooleanMatrix rowResult = S_i.booleanProduct(B);
                    double rowError = this.C.getRow(i).reconstructionError(rowResult,1d);

                    if (rowError < incumbentRowError){
                        improved = true;
                    }else{
                        S_i.update(j,BooleanMatrix.not(S_i.apply(j))); // undo the change.
                    }
                }
            }

            BooleanMatrix S_T = BooleanMatrix.deepTranspose(S);
            BooleanMatrix B_T = BooleanMatrix.deepTranspose(B);

            for (int i =0;i<m;i++){

                BooleanMatrix B_Ti = B_T.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = B_Ti.booleanProduct(S_T);

                double incumbentRowError = C_T.getRow(i).reconstructionError(incumbentRow,1d);

                for (int j =0;j<this.k;j++){

                    B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // flip bit.
                    BooleanMatrix rowResult = B_Ti.booleanProduct(S_T);
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

//            if(it%10==0) {
//                System.out.println("The recon-error is: " +
//                        Double.toString(this.C.relativeReconstructionError(S.booleanProduct(B), 1d)));
//            }
            it = it +1;

            // stop when our solution is locally optimal wrt to this neighbourhood.
            if (!improved){
                //System.out.println("The final-recon error is: " + Double.toString(this.C.relativeReconstructionError(S.booleanProduct(B),1d)));
                break;
            }

        }

        return this.C.relativeReconstructionError(S.booleanProduct(B),1d);
    }

    // Find the best descent direction.
    public void steepestDescent(){
        int n = this.C.getHeight();
        int m = this.C.getWidth();

        double density = this.C.getDensity();
        BooleanMatrix C_T = BooleanMatrix.deepTranspose(this.C);

        // Combination matrix
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,this.k,density,0d);
        // Basis matrix
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(this.k,m,density,0d);

        // Print out the initial guess and its recon error.
        BooleanMatrix randomResult = S.booleanProduct(B);
        double randomError = this.C.reconstructionError(randomResult,1d);
        System.out.print("The initial recon-error is: " + Double.toString(randomError));

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
            for (int i =0;i<n;i++){

                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B);
                double incumbentRowError = this.C.getRow(i).reconstructionError(incumbentRow,1d);

                for (int j =0;j<this.k;j++){

                    S_i.update(j,BooleanMatrix.not(S_i.apply(j))); // flip bit.
                    BooleanMatrix rowResult = S_i.booleanProduct(B);
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

            for (int i =0;i<m;i++){

                BooleanMatrix B_Ti = B_T.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = B_Ti.booleanProduct(S_T);

                double incumbentRowError = C_T.getRow(i).reconstructionError(incumbentRow,1d);

                for (int j =0;j<this.k;j++){

                    B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // flip bit.
                    BooleanMatrix rowResult = B_Ti.booleanProduct(S_T);
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

            if(it%100==0) {
                System.out.println("The current recon-error is: " +
                        Double.toString(this.C.relativeReconstructionError(S.booleanProduct(B), 1d)));
            }

            if (!(best_i<0)){
                if(flipS==true){
                    S.update(best_i,best_j,BooleanMatrix.not(S.apply(best_i,best_j)));
                }else{
                    B.update(best_i,best_j,BooleanMatrix.not(B.apply(best_i,best_j)));
                }
            }else{
                System.out.println("The final-recon error is: " + Double.toString(this.C.relativeReconstructionError(S.booleanProduct(B),1d)));
                break;
            }

        }


    }

    // Try different initial conditions to find better local minima.
    public void randomRestarts(int numRestarts){
        double best_recon = 1;
        for (int i = 0; i < numRestarts; i++) {
            double r = this.nextDescent();
            if (r<best_recon){
                best_recon = r;
            }
        }

        System.out.println(best_recon);
    }

}