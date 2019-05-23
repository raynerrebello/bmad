package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;
import org.kramerlab.bmad.visualization.DecompositionLayout;

public class ErrorReconstruction {

    public ErrorReconstruction(int levels, Heuristic heuristic, boolean xor, int numRestarts, int bpp) {
        this.heuristic = heuristic;
        this.xor = xor;
        this.numRestarts = numRestarts;
        this.bpp = bpp;
        this.levels = levels;
    }

    public Heuristic heuristic;
    public boolean xor;
    public int numRestarts;
    public int bpp;
    public int levels;

    public BooleanMatrix recursiveErrorReconstruction(BooleanMatrix C, int k,int r){
        if (r>0) {
            Tuple<BooleanMatrix,BooleanMatrix> decomp = this.heuristic.decomposition(C,k,this.xor,this.numRestarts,this.bpp);

            BooleanMatrix reconstruction = decomp._1.booleanProduct(decomp._2,this.xor);

            //DecompositionLayout.showDecomposition(String.valueOf(r),C,decomp._1,decomp._2);

            BooleanMatrix error = reconstruction.xorAdd(C);

            return reconstruction.xorAdd(recursiveErrorReconstruction(error,k+1,r-1));

        }else return RandomMatrixGeneration.randomMatrix(C.getHeight(),C.getWidth(),0,0);
    }

    // assume k % increment == 0
    public Tuple<BooleanMatrix,BooleanMatrix> incrementedXBMaD(BooleanMatrix C, int k,int increment){
        assert (k%increment==0);

        int n = C.getHeight();
        int m = C.getWidth();
        BooleanMatrix S = RandomMatrixGeneration.randomMatrix(n,k,0,0);
        BooleanMatrix B = RandomMatrixGeneration.randomMatrix(k,m,0,0);
        BooleanMatrix residual = C.xorAdd(S.booleanProduct(B,true));
        for (int i = 0; i < k; i+=increment) {
            Tuple<BooleanMatrix,BooleanMatrix> decomp = this.heuristic.decomposition(residual,increment,true,this.numRestarts,this.bpp);
            fillFactors(S,B,decomp._1,decomp._2,i);
            residual = C.xorAdd(S.booleanProduct(B,true));
            //System.out.println(C.relativeReconstructionError(S.booleanProduct(B,true),1));
        }
        System.out.printf("\n Using %s, inc =  %d, Best_recon = %f%n",  xor? "XOR": " OR", increment,
                C.relativeReconstructionError(S.booleanProduct(B,true),1d));
        return new Tuple<>(S,B);
    }

    public static void fillFactors(BooleanMatrix S,BooleanMatrix B, BooleanMatrix s, BooleanMatrix b,int startFill){
        int n = S.getHeight();
        int m = B.getWidth();
        int k = s.getWidth();
        for (int i = 0; i < n ; i++) {
            for (int j = startFill; j < (startFill + k) ; j++) {
                S.update(i,j,s.apply(i,j-startFill));
            }
        }
        for (int i = startFill; i < (startFill + k) ; i++) {
            for (int j = 0; j < m ; j++) {
                B.update(i,j,b.apply(i-startFill,j));
            }
        }
    }




}