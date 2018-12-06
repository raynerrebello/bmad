package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;

public class SimulatedAnnealing {

    private final BooleanMatrix C;
    private final int k;
    private double relativeRecError;

    public SimulatedAnnealing(BooleanMatrix C, int dimension){
        this.C = C;
        this.k = dimension;
    }

    public Tuple<BooleanMatrix,BooleanMatrix> anneal(double t0, double tmin,double alpha,boolean xor){





    }
}
