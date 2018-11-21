package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.algorithms.LocalSearch;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;

public class localSearchTest {
    public static void main(String... args) throws Throwable{

        BooleanMatrix C = RandomMatrixGeneration.randomMatrix(100,100,0.3,0d);
        System.out.println(C.toString());

        LocalSearch ls = new LocalSearch(C,3,true);
        ls.randomRestarts(10);
    }
}
