package org.kramerlab.bmad.algorithms;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;

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

            BooleanMatrix error = reconstruction.xorAdd(C);

            return reconstruction.xorAdd(recursiveErrorReconstruction(error,k,r-1,new SimulatedAnnealing(2000,1,0.99)));

        }else return RandomMatrixGeneration.randomMatrix(C.getHeight(),C.getWidth(),0,0);
    }

    public BooleanMatrix recursiveErrorReconstruction(BooleanMatrix C, int k,int r,Heuristic h){
        if (r>0) {
            Tuple<BooleanMatrix,BooleanMatrix> decomp = h.decomposition(C,k,this.xor,this.numRestarts,this.bpp);

            BooleanMatrix reconstruction = decomp._1.booleanProduct(decomp._2,this.xor);

            BooleanMatrix error = reconstruction.xorAdd(C);

            return reconstruction.xorAdd(recursiveErrorReconstruction(error,k,r-1));

        }else return RandomMatrixGeneration.randomMatrix(C.getHeight(),C.getWidth(),0,0);
    }

}