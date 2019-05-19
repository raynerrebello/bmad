package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;

import java.util.ArrayList;
import java.util.BitSet;

import static org.kramerlab.bmad.visualization.DecompositionLayout.showDecomposition;

public class XORDShowImage extends XORDecompose {

    public XORDShowImage(BooleanMatrix a) {
        super(a);
    }


//    public static byte[] toByteArray(BitSet a, int size) {
//        byte[] output = new byte[size];
//        for (int i = 0; i < size; i++) {
//            output[i] = a.get(i) ? (byte) 3 : (byte) 0;
//        }
//        return output;
//    }



    public Tuple<BooleanMatrix, BooleanMatrix> decompose(BooleanMatrix input, int decomposeDim, int startType) {
        /*
         * startType:    0 = start with randomly generated non-zero vector;
         *               1 = start with density-based vector (set to 1 if row/col density >= matrix density);
         *               2 = start with a random copy of non-zero row/col from the original matrix.
         */
        int counter = 0;

        if (input.getDensity() == 0) {
            relativeRecError = 0;
            calculatdRecError = 0;
            return new Tuple<BooleanMatrix, BooleanMatrix>(new BooleanMatrix(input.getHeight(), decomposeDim), new BooleanMatrix(decomposeDim, input.getWidth()));
        } else {
            canImprove = true;
            int height = input.getHeight();
            int width = input.getWidth();
            int k = decomposeDim;
            BooleanMatrix a, colMatrix, rowMatrix, resMatrix = new BooleanMatrix(height, width);
            BitSet colVec, rowVec;
            byte[][]tempCol = new byte[height][1], tempRow = new byte[1][width];

            ArrayList<byte[]> rowArray = new ArrayList<byte[]>();
            ArrayList<byte[]> colArray = new ArrayList<byte[]>(); // this one will be transposed later;

            while (k > 0 && canImprove) {
                a = (k == decomposeDim) ? input : resMatrix;

                Tuple<BitSet, BitSet> pair = getPair(a, startType);
                colVec = pair._1;
                rowVec = pair._2;
                counter ++;


                if (colVec.cardinality() == 0 || rowVec.cardinality() == 0) {
                    canImprove = false;  // if any new pair contains a [0] vector, no improvement can be made, terminate decomposition.
                    k -= 1;
                } else {
                    resMatrix = getResidualMatrix(a, colVec, rowVec);
                    k -= 1;
                    if (resMatrix.getDensity() == 0) {
                        canImprove = false;  // if any new pair contains a [0] vector, no improvement can be made, terminate decomposition.
                    }
                    rowArray.add(toByteArray(rowVec, width));
                    colArray.add(toByteArray(colVec, height));


                    tempRow[0] = toByteArray(rowVec, width);

                    for (int j = 0; j < height; j++) {
                        tempCol[j][0] = colVec.get(j) == true? (byte) 3 : (byte) 0;
                    }
                    String title = String.format("pair at round %d%n", counter);
                    BooleanMatrix col = new BooleanMatrix(tempCol);
                    BooleanMatrix row = new BooleanMatrix(tempRow);
//                    System.out.printf("ColVec: %s,   RowVec: %s%n%ntempColMatrix: %n%s%n%nTempRowMatrix: %n%s%n%n", printableBitSet(colVec, height), printableBitSet(rowVec, width), col, row);


                    showDecomposition(title, a, col, row);

                }
            }


            rowMatrix = new BooleanMatrix(rowArray.toArray(new byte[0][0]));
            colMatrix = new BooleanMatrix(colArray.toArray(new byte[0][0]));
            colMatrix = BooleanMatrix.deepTranspose(colMatrix);

            relativeRecError = resMatrix.getDensity();
            finalResMatrix = resMatrix;

            return new Tuple<BooleanMatrix, BooleanMatrix>(colMatrix, rowMatrix);
        }
    }




}
