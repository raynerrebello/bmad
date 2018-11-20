package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.algorithms.LocalSearch;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;

public class localSearchTest {

    public static void main(String... args) throws Throwable{
        BooleanMatrix C = RandomMatrixGeneration.randomMatrix(1000,1000,0.5,0d);
        System.out.println(C.toString());

        LocalSearch search = new LocalSearch(C,20,true);
        search.performDescents();


    }
}
