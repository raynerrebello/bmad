package org.kramerlab.bmad.scripts;
import org.kramerlab.bmad.algorithms.*;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.visualization.DecompositionLayout;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BatchImageDecomposition {

    public static void main(String... args) throws Throwable{
        File folder = new File(".\\src\\main\\java\\org\\kramerlab\\bmad\\scripts\\bin");
        File[] listOfFiles = folder.listFiles();
        Pattern p = Pattern.compile("(\\w)_(\\d+)_(\\d+)\\.bin");
        int n;
        int m;

        for (int i = 8; i < 9; i++) {
            if (listOfFiles[i].isFile()) {

                Matcher matcher = p.matcher(listOfFiles[i].getName());
                if (matcher.find()) {
                    n = Integer.parseInt(matcher.group(2));
                    m = Integer.parseInt(matcher.group(3));

                    BooleanMatrix matrix = BinaryParser.binaryToBooleanMatrix(listOfFiles[i].getPath(),n,m);
                    System.out.printf("%s, of size %d x %d, with density: %f \n","Filepath: " +listOfFiles[i].getPath(),matrix.getHeight(),matrix.getWidth(),matrix.getDensity());

//                    StandardLocalSearch localSearch = new StandardLocalSearch(matrix,(int) Math.sqrt(Math.min(matrix.getHeight(),matrix.getWidth())));
//                    Tuple<BooleanMatrix,BooleanMatrix> decomp = localSearch.randomRestarts(100,false);
//
//                    BooleanMatrix recon = decomp._1.booleanProduct(decomp._2,false);
                    Heuristic ls = new StandardLocalSearch();
                    ErrorReconstruction er = new ErrorReconstruction(3,ls,false,30,0);
                    BooleanMatrix recon = er.recursiveErrorReconstruction(matrix,2,2);
                    DecompositionLayout.showDecomposition("result",recon,new BooleanMatrix(n,1), new BooleanMatrix(1,m));
                    System.out.println(matrix.relativeReconstructionError(recon,1));
                    BinaryParser.booleanMatrixToBinary(recon,"\\src\\main\\java\\org\\kramerlab\\bmad\\scripts\\out\\" + listOfFiles[i].getName() );

                }
            }
        }
    }
}
