package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.algorithms.*;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.matrix.RandomMatrixGeneration;
import org.kramerlab.bmad.visualization.DecompositionLayout;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorReconTest {

    public static void main(String... args) throws Throwable{
        int n = 1000;
        int m = 1000;

        BooleanMatrix matrix = RandomMatrixGeneration.randomMatrix(n,m,0.5,0);

        Heuristic ls = new StandardLocalSearch();
        ErrorReconstruction er = new ErrorReconstruction(3,ls,false,10,0);
        BooleanMatrix recon = er.recursiveErrorReconstruction(matrix,1,10);
        System.out.println(matrix.relativeReconstructionError(recon,1));

//        XORDecompose xor = new XORDecompose(matrix);
//        xor.iterativeDecompose(matrix,10,100);






    }
}


