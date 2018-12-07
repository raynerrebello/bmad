package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;

public interface Heuristic {
    Tuple<BooleanMatrix,BooleanMatrix> decomposition(boolean xor,int numberRestarts,int bpp);
}
