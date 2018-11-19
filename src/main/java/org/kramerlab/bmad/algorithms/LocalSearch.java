package org.kramerlab.bmad.algorithms;

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
        this.nextDescent = nextDescent;
    }

    public void performDescents(){

        // Initialise the
        int n = this.C.getHeight();
        int m = this.C.getHeight();
        double density = this.C.getDensity();

        // Combination matrix
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,this.k,density,0d);
        // Basis matrix
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(this.k,m,density,0d);

        BooleanMatrix incumbentResult = S.booleanProduct(B);
        double incumbentError = S.reconstructionError(incumbentResult,1d);

        while(true){

            // Explore neighbourhood of 1 bit swaps of S
            boolean improved = false;

            for (int i =0;i<n;i++){

                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B);

                double incumbentRowError = this.C.reconstructionError(incumbentRow,1d);
                for (int j =0;j<this.k;j++){

                    S_i.update(j,BooleanMatrix.not(S_i.apply(j))); // flip bit.

                    BooleanMatrix rowResult = S_i.booleanProduct(B);
                    double rowError = this.C.reconstructionError(rowResult,1d);

                    if (rowError < incumbentRowError){
                        improved = true;
                    }else{
                        S_i.update(j,BooleanMatrix.not(S_i.apply(j)));
                    }
                }
            }

            // Explore neighbourhood of 1 bit swaps of B

            for (int i =0;i<k;i++){
                BooleanMatrix B_xj = S.get(i);
                for (int j =0;j<m;j++){

                }
            }


        }


    }


}