package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;

public interface Heuristic {
    Tuple<BooleanMatrix,BooleanMatrix> decomposition(BooleanMatrix C,int k,boolean xor,int numberRestarts,int bpp);
}
