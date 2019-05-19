package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;

public class SimulatedAnnealing implements Heuristic{

    private final double t0;
    private final double tmin;
    private final double alpha;

    public SimulatedAnnealing(double t0, double tmin,double alpha){
        this.t0 = t0;
        this.tmin =tmin;
        this.alpha = alpha;
    }

    public Tuple<BooleanMatrix,BooleanMatrix> decomposition(BooleanMatrix C,int k,boolean xor,int numberRestarts,int bpp){
        if(bpp<=1){
            return anneal(C,k,xor);
        }else{
            return anneal(C,k,xor,bpp);

        }
    }

    public Tuple<BooleanMatrix,BooleanMatrix> anneal(BooleanMatrix C, int k,boolean xor){
        double density = Math.sqrt(1 - Math.pow(1 - C.getDensity(),1./k));
        int n = C.getHeight();
        int m = C.getWidth();

        double t = this.t0;
        double delta;

        BooleanMatrix C_T = BooleanMatrix.deepTranspose(C);
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,k,density,0d);
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(k,m,density,0d);

        BooleanMatrix bestS = S;
        BooleanMatrix bestB = B;
        double bestError = 1;
        double error;
        boolean changed = false;

        while(t>=tmin){

            for (int i = 0; i < n; i++){
                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B, xor);
                double incumbentRowError = C.getRow(i).relativeReconstructionError(incumbentRow,1);

                for (int j = 0; j < k; j++){
                    S_i.update(j, BooleanMatrix.not(S_i.apply(j))); // flip bit.
                    BooleanMatrix rowResult = S_i.booleanProduct(B, xor);
                    double rowError = C.getRow(i).relativeReconstructionError(rowResult,1);
                    delta =  rowError-incumbentRowError;
                    if (delta < 0){
                        incumbentRowError = rowError;
                        error = C.relativeReconstructionError(S.booleanProduct(B),1);
                        changed = true;
                        if ( error < bestError) {
                            bestB = B;
                            bestS = S;
                            bestError = error;
                        }
                    }else if(Math.exp(-delta/t) > Math.random()){
                        incumbentRowError = rowError;
                        changed = true;
                    }else{
                        S_i.update(j, BooleanMatrix.not(S_i.apply(j)));
                    }

                    if(changed){
                        t = alpha * t;
                        changed = false;
                    }
                }

            }


            BooleanMatrix S_T = BooleanMatrix.deepTranspose(S);
            BooleanMatrix B_T = BooleanMatrix.deepTranspose(B);

            for (int i = 0; i < m; i++){

                BooleanMatrix B_Ti = B_T.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = B_Ti.booleanProduct(S_T, xor);
                double incumbentRowError = C_T.getRow(i).relativeReconstructionError(incumbentRow,1);

                for (int j = 0; j < k; j++){

                    B_Ti.update(j,BooleanMatrix.not(B_Ti.apply(j))); // flip bit.
                    BooleanMatrix rowResult = B_Ti.booleanProduct(S_T, xor);
                    double rowError = C_T.getRow(i).relativeReconstructionError(rowResult,1);
                    delta =  rowError-incumbentRowError;
                    if (delta < 0){
                        incumbentRowError = rowError;
                        S = BooleanMatrix.deepTranspose(S_T);
                        B = BooleanMatrix.deepTranspose(B_T);
                        error = C.relativeReconstructionError(S.booleanProduct(B),1);
                        changed = true;
                        if ( error < bestError) {
                            bestB = B;
                            bestS = S;
                            bestError = error;
                        }
                    }else if(Math.exp(-delta/t) > Math.random()){
                        incumbentRowError = rowError;
                        changed = true;
                    }else{
                        B_Ti.update(j, BooleanMatrix.not(B_Ti.apply(j)));
                    }

                    if(changed){
                        t = alpha * t;
                        changed = false;
                    }
                }
            }

            S = BooleanMatrix.deepTranspose(S_T);
            B = BooleanMatrix.deepTranspose(B_T);

        }

        System.out.println(bestError);
        return new Tuple<>(bestS, bestB);
    }

    public Tuple<BooleanMatrix,BooleanMatrix> anneal(BooleanMatrix C, int k,boolean xor,int bpp){

        double density = Math.sqrt(1 - Math.pow(1 - C.getDensity(),1./k));
        int n = C.getHeight();
        int m = C.getWidth();

        double t = this.t0;
        double delta;

        BooleanMatrix C_T = BooleanMatrix.deepTranspose(C);
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,k,density,0d);
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(k,m,density,0d);

        BooleanMatrix bestS = S;
        BooleanMatrix bestB = B;
        double bestError = 255;
        double error;
        boolean changed = false;

        while(t>=tmin){

            for (int i = 0; i < n; i++){
                BooleanMatrix S_i = S.getRow(i);// still points to the same row
                BooleanMatrix incumbentRow = S_i.booleanProduct(B, xor);
                double incumbentRowError = C.getRow(i).averageEuclideanReconstructionError(incumbentRow,bpp);

                for (int j = 0; j < k; j++){
                    S_i.update(j, BooleanMatrix.not(S_i.apply(j))); // flip bit.
                    BooleanMatrix rowResult = S_i.booleanProduct(B, xor);
                    double rowError = C.getRow(i).averageEuclideanReconstructionError(rowResult,bpp);
                    delta =  rowError-incumbentRowError;
                    if (delta < 0){
                        incumbentRowError = rowError;
                        error = C.averageEuclideanReconstructionError(S.booleanProduct(B),bpp);
                        changed = true;
                        if ( error < bestError) {
                            bestB = B;
                            bestS = S;
                            bestError = error;
                        }
                    }else if(Math.exp(-delta/t) > Math.random()){
                        incumbentRowError = rowError;
                        changed = true;
                    }else{
                        S_i.update(j, BooleanMatrix.not(S_i.apply(j)));
                    }

                    if(changed){
                        t = alpha * t;
                        changed = false;
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
                    delta =  rowError-incumbentRowError;
                    if (delta < 0){
                        incumbentRowError = rowError;
                        S = BooleanMatrix.deepTranspose(S_T);
                        B = BooleanMatrix.deepTranspose(B_T);
                        error = C.averageEuclideanReconstructionError(S.booleanProduct(B),bpp);
                        changed = true;
                        if ( error < bestError) {
                            bestB = B;
                            bestS = S;
                            bestError = error;
                        }
                    }else if(Math.exp(-delta/t) > Math.random()){
                        incumbentRowError = rowError;
                        changed = true;
                    }else{
                        B_Ti.update(j, BooleanMatrix.not(B_Ti.apply(j)));
                    }

                    if(changed){
                        t = alpha * t;
                        changed = false;
                    }
                }
            }

            S = BooleanMatrix.deepTranspose(S_T);
            B = BooleanMatrix.deepTranspose(B_T);

        }

        System.out.println(bestError);
        return new Tuple<>(bestS, bestB);
    }
}
