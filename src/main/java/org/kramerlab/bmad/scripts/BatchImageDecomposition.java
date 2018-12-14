package org.kramerlab.bmad.scripts;
import org.kramerlab.bmad.algorithms.*;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.visualization.DecompositionLayout;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BatchImageDecomposition {

    public static void main(String... args) throws Throwable{
        File folder = new File(".\\src\\main\\java\\org\\kramerlab\\bmad\\scripts\\bin");
        File[] listOfFiles = folder.listFiles();
        Pattern p = Pattern.compile("(\\w)_(\\d+)_(\\d+)\\.bin");
        int n;
        int m;
        HashMap<String,Double> map = new HashMap<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {

                Matcher matcher = p.matcher(listOfFiles[i].getName());
                if (matcher.find()) {
                    n = Integer.parseInt(matcher.group(2));
                    m = Integer.parseInt(matcher.group(3));
                    String name = listOfFiles[i].getName().split("-")[2];
                    System.out.println(name);
                    if (!map.containsKey(name)) {
                        map.put(name, 0.0);
                    }

                    BooleanMatrix matrix = BinaryParser.binaryToBooleanMatrix(listOfFiles[i].getPath(),n,m);
                    System.out.printf("%s, of size %d x %d, with density: %f \n","Filepath: " +listOfFiles[i].getPath(),matrix.getHeight(),matrix.getWidth(),matrix.getDensity());


//                    StandardLocalSearch localSearch = new StandardLocalSearch(matrix,(int) Math.sqrt(Math.min(matrix.getHeight(),matrix.getWidth())));
//                    Tuple<BooleanMatrix,BooleanMatrix> decomp = localSearch.randomRestarts(100,false);
//
//                    BooleanMatrix recon = decomp._1.booleanProduct(decomp._2,false);
//                    Heuristic ls = new StandardLocalSearch();
//                    ErrorReconstruction er = new ErrorReconstruction(3,ls,false,30,0);
//                    BooleanMatrix recon = er.recursiveErrorReconstruction(matrix,2,2);

                    XORDecompose xor = new XORDecompose(matrix);
                    Tuple<BooleanMatrix,BooleanMatrix> ans = xor.iterativeDecompose(matrix,(int) (0.5 * (Math.min(n,m))),2,10);
                    BooleanMatrix recon = ans._1.booleanProduct(ans._2,true);

                    //DecompositionLayout.showDecomposition("result",recon,new BooleanMatrix(n,1), new BooleanMatrix(1,m));
                    int k = ans._1.getWidth();
                    System.out.printf("k = %d%n",ans._1.getWidth());
                    System.out.println(matrix.relativeReconstructionError(recon,1));

                    double curr = map.get(name);
                    curr += (double) k*(n+m)/(8d*1024);
                    map.replace(name,curr);
                    BinaryParser.booleanMatrixToBinary(recon,"\\src\\main\\java\\org\\kramerlab\\bmad\\scripts\\out\\" + listOfFiles[i].getName() );

                }


            }
        }

        System.out.println(map);
    }
}
