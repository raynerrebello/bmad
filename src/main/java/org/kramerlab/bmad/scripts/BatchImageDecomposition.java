package org.kramerlab.bmad.scripts;
import org.kramerlab.bmad.algorithms.EuclideanLocalSearch;
import org.kramerlab.bmad.algorithms.LocalSearchReturnMatrix;
import org.kramerlab.bmad.algorithms.SimulatedAnnealing;
import org.kramerlab.bmad.algorithms.StandardLocalSearch;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;

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

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                Matcher matcher = p.matcher(listOfFiles[i].getName());
                if (matcher.find()) {
                    n = Integer.parseInt(matcher.group(2));
                    m = Integer.parseInt(matcher.group(3));

                    BooleanMatrix matrix = BinaryParser.binaryToBooleanMatrix(listOfFiles[i].getPath(),n,m);
                    System.out.printf("%s, of size %d x %d, with density: %f \n","Filepath: " +listOfFiles[i].getPath(),matrix.getHeight(),matrix.getWidth(),matrix.getDensity());

                    StandardLocalSearch localSearch = new StandardLocalSearch(matrix,(int) Math.sqrt(Math.min(matrix.getHeight(),matrix.getWidth())));
                    Tuple<BooleanMatrix,BooleanMatrix> decomp = localSearch.randomRestarts(100,false);

                    BooleanMatrix recon = decomp._1.booleanProduct(decomp._2,false);

                    BinaryParser.booleanMatrixToBinary(recon,"\\src\\main\\java\\org\\kramerlab\\bmad\\scripts\\out\\" + listOfFiles[i].getName() );

                }
            }
        }
    }
}
