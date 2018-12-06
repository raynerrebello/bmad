package org.kramerlab.bmad.scripts;

import org.kramerlab.bmad.algorithms.LocalSearchReturnMatrix;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;

public class BitManipulation {
    public static void main(String... args) throws Throwable{
        BooleanMatrix m = BinaryParser.binaryToBooleanMatrix(
                    "\\src\\main\\java\\org\\kramerlab\\bmad\\scripts\\binary_matrices\\ratings_6040_3952.bin",6040,3952);
        System.out.println(m.getDensity());
        System.out.println(m.getWidth());
        System.out.println(m.getHeight());

        LocalSearchReturnMatrix localSearch = new LocalSearchReturnMatrix(m,(int) Math.sqrt(Math.min(m.getHeight(),m.getWidth())));

        Tuple<BooleanMatrix,BooleanMatrix> decomp = localSearch.randomRestarts(1,true,false);
        BooleanMatrix recon = decomp._1.booleanProduct(decomp._2,false);

        System.out.println(recon.getDensity()
        );
    }
}
