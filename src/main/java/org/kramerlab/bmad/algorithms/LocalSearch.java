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
        this.nextDescent = nextDescent;
    }

    public void performDescents(){

        int n = this.C.getHeight();
        int m = this.C.getHeight();
        double density = this.C.getDensity();
        boolean improved = false;
        // Combination matrix
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,this.k,density,0d);
        // Basis matrix
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(this.k,m,density,0d);

        BooleanMatrix incumbentResult = S.booleanProduct(B);
        double incumbentError = this.C.reconstructionError(incumbentResult,1d);
        System.out.print(" The intial recon error is: " + Double.toString(this.C.relativeReconstructionError(S.booleanProduct(B),1d)));

        int it = 0;
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
//            System.out.println(it);
//            incumbentResult = S.booleanProduct(B);
//            incumbentError = this.C.reconstructionError(incumbentResult,1d);

//            // Explore neighbourhood of 1 bit swaps of B
//            for (int i =0;i<k;i++){
//                for (int j =0;j<m;j++){
//
//                    B.update(i,j,BooleanMatrix.not(B.apply(i,j)));
//
//                    BooleanMatrix result = S.booleanProduct(B);
//                    double error = this.C.getRow(i).reconstructionError(result,1d);
//
//                    if (error < incumbentError) {
//                        incumbentError = error;
//                        incumbentResult = result;
//                        improved = true;
//                    }else {
//                        B.update(i,j,BooleanMatrix.not(B.apply(i,j)));
//                    }
//
//                }
//            }


            incumbentError = this.C.reconstructionError(incumbentResult,1d);

//            // Explore neighbourhood of 1 bit swaps of B
//            for (int i =0;i<k;i++){
//                for (int j =0;j<m;j++){
//
//                    B.update(i,j,BooleanMatrix.not(B.apply(i,j)));
//
//                    BooleanMatrix result = S.booleanProduct(B);
//                    double error = this.C.getRow(i).reconstructionError(result,1d);
//
//                    if (error < incumbentError) {
//                        incumbentError = error;
//                        incumbentResult = result;
//                        improved = true;
//                    }else {
//                        B.update(i,j,BooleanMatrix.not(B.apply(i,j)));
//                    }
//
//                }
//            }



            // stop when our solution is locally optimal wrt to this neighbourhood.
            System.out.print(" The recon error is: " + Double.toString(this.C.relativeReconstructionError(S.booleanProduct(B),1d)));
            it = it +1;
            System.out.println(it);
            if (!improved|| it>10000){
                System.out.print(" The recon error is: " + Double.toString(this.C.relativeReconstructionError(S.booleanProduct(B),1d)));
                break;
            }


        }


    }


}