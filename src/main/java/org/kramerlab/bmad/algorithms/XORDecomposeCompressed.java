package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;

import java.util.*;

public class XORDecomposeCompressed extends XORDecompose {
    public static boolean canImprove = true;
    public static BooleanMatrix finalResMatrix;
    public int totalSize = 0, height = 0, width = 0;
    public double relativeRecError;
    public double calculatdRecError;


    public XORDecomposeCompressed(
            BooleanMatrix a){
        super(a);
    }


    /**
     * Alternative version of decompose. Iteratively call decompose with startType == 0 (use random vector) for n times,
     * return the two BooleanMatrix factors with the minimum relative reconstruction error.
     * @param input: A BooleanMatrix object, the original matrix to be decomposed.
     * @param decomposeDim: the dimension of factor matrices.
     * @param startType:
     *                 0 = start with randomly generated non-zero vector;
     *                 2 = start with a random copy of non-zero row/col from the original matrix.
     * @param numIteration: the number of time to iterate (repeatedly call decompose).
     * @return: A Tuple<BooleanMatrix, BooleanMatrix> with the smallest relative reconstruction error among all iterations. (._1 = column matrix, ._2 = row matrix).
     */
    public Tuple<BooleanMatrix, BooleanMatrix> iterativeDecompose(BooleanMatrix input, int decomposeDim,  int startType, int numIteration){
        /*
         * startType:    0 = start with randomly generated non-zero vector;
         *               2 = start with a random copy of non-zero row/col from the original matrix.
         */
        double min = 1, error = -99;
        Tuple<BooleanMatrix, BooleanMatrix> temp, output;
        int pos = 0;
        ArrayList<Tuple<BooleanMatrix, BooleanMatrix>> tupleList = new ArrayList<Tuple<BooleanMatrix, BooleanMatrix>> ();

        XORDecomposeCompressed xorDec = new XORDecomposeCompressed(input);

        if(input.getDensity() == 0){
            return xorDec.decompose(input, decomposeDim, startType);
        }else {
            for (int i = 0; i < numIteration; i++) {
                temp = xorDec.decompose(input, decomposeDim, startType);
                error = xorDec.relativeRecError;
                tupleList.add(temp);
                if (error <= min) {
                    min = error;
                    pos = i;
                }
            }

            relativeRecError = min;
            output = tupleList.get(pos);
            BooleanMatrix approximation = xorDec.getProductMatrices(output._1, output._2);

            calculatdRecError = input.relativeReconstructionError(approximation, 1d);
            return output;
        }
    }


    /**
     * Standard decomposition method, with 3 initial-parth-choosing types as described below.
     * Takes in a BooleanMatrix object to decompose, returns a Tuple of two factor matrices (BooleanMatrices objects), and updates a public field of relative reconstruction error.
     * @param input: A BooleanMatrix object, the original matrix to be decomposed.
     * @param decomposeDim: the dimension of factor matrices.
     * @param startType: 0 = start with randomly generated non-zero vector;
     *                   1 = start with density-based vector (set to 1 if row/col density >= matrix density);
     *                   2 = start with a random copy of non-zero row/col from the original matrix.
     * @return: A Tuple<BooleanMatrix, BooleanMatrix> with the smallest relative reconstruction error among all iterations. (._1 = column matrix, ._2 = row matrix).
     */
    public Tuple<BooleanMatrix, BooleanMatrix> decompose(BooleanMatrix input, int decomposeDim, int startType) {
        /*
         * startType:    0 = start with randomly generated non-zero vector;
         *               1 = start with density-based vector (set to 1 if row/col density >= matrix density);
         *               2 = start with a random copy of non-zero row/col from the original matrix.
         */
        if (input.getDensity() == 0) {
            relativeRecError = 0;
            calculatdRecError = 0;
            return new Tuple<BooleanMatrix, BooleanMatrix>(new BooleanMatrix(input.getHeight(), decomposeDim), new BooleanMatrix(decomposeDim, input.getWidth()));
        } else {
            canImprove = true;
            int height = input.getHeight();
            int width = input.getWidth();
            int k = decomposeDim;
            boolean fixRow = width > height ? true : false;
            BooleanMatrix a, colMatrix, rowMatrix, resMatrix = new BooleanMatrix(height, width);
            BitSet colVec, rowVec;

            ArrayList<byte[]> rowArray = new ArrayList<byte[]>();
            ArrayList<byte[]> colArray = new ArrayList<byte[]>(); // this one will be transposed later;

            while(k > 0 && canImprove) {
                a = (k == decomposeDim) ? input : resMatrix;

                Tuple<BitSet, BitSet> pair = getPair(a, startType);
                colVec = pair._1;
                rowVec = pair._2;

                if (colVec.cardinality() == 0 || rowVec.cardinality() == 0) {
                    canImprove = false;  // if any new pair contains a [0] vector, no improvement can be made, terminate decomposition.
                    k -= 1;
                } else {
                    resMatrix = getResidualMatrix(a, colVec, rowVec);
                    k -= 1;
                    if(resMatrix.getDensity() == 0){
                        canImprove = false;  // if any new pair contains a [0] vector, no improvement can be made, terminate decomposition.
                    }
                    if(rowArray.isEmpty() || colArray.isEmpty()){
                        rowArray.add(toByteArray(rowVec, width));
                        colArray.add(toByteArray(colVec, height));
                    }else{
                        Tuple<ArrayList<byte[]>, ArrayList<byte[]>> tempoutput =
                                compressAndAdd(rowArray, colArray, rowVec, colVec, fixRow);
                        colArray = tempoutput._1;
                        rowArray = tempoutput._2;
                    }

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


    /**
     * Takes a Tuple of a by k and k by b BooleanMatrices, perform the following:
     * If a >= b, target = tuple._1, partner = tuple._2; otherwise swap.
     *       2) target matrix:  remove duplicate row/cols, keep the first occurrence (lowest index among all duplicates);
     *       2) partner matrix: for each group of rows/cols that are identical, xor-add all, update the value to the lowest index one, then remove the rest.
     * Return the updated pair as a new Tuple in the same order as in the original Tuple.
     * @param pair: a Tuple<BooleanMatrix, BooleanMatrix> representing two factor matrices of dimensions a x k, k x b.
     * @return: a new Tuple<BooleanMatrix, BooleanMatrix> of dimensions (a-p) by k,  k by (b-q),  p, q >= 0.
     */
    public static Tuple<BooleanMatrix, BooleanMatrix> compressResult(Tuple<BooleanMatrix, BooleanMatrix> pair) {

        // if k == 1, no need to compress, return original input.

        if (pair._1.getWidth() == 1) {
            System.out.println("Cannot compress further, returning original input.");
            return pair;
        } else {

            // 1) Find the index of identical rows/cols of the fixed matrix

            BooleanMatrix colMatrix = pair._1;
            BooleanMatrix rowMatrix = pair._2;
            int height = colMatrix.getHeight();
            int k = colMatrix.getWidth();
            int width = rowMatrix.getWidth();
            boolean fixRow = width > height ? true : false;

            ArrayList<Integer> duplicates;
            HashMap<Integer, Set<Integer>> fullList;

            boolean removeRow = fixRow? true : false;
            boolean combineRow = !removeRow;
            BooleanMatrix compressed = fixRow? rowMatrix: colMatrix;
            BooleanMatrix xored = fixRow? colMatrix : rowMatrix;

            // 1) get indices of duplicated row/cols of the target factor matrix
            Tuple<ArrayList<Integer>, HashMap<Integer, Set<Integer>>> lists = compressed.getDuplicatesAndOriginal(removeRow);
            duplicates = lists._1;
            fullList = lists._2;


            System.out.printf("colMatrix: %n%s%n%nrowMatrix: %n%s%n%n", colMatrix, rowMatrix);
            System.out.printf("fixRow = %s, checking %s, duplicates = %s, fullList = %s%n", fixRow, fixRow? "rowMatrix" : "colMatrix", duplicates, fullList);

            // 2) Remove duplicates from the target matrix
            System.out.printf("before - compressed: %n%s%n%n", compressed);

            compressed = compressed.removeDuplicates(duplicates, removeRow);
//            System.out.printf("after - compressed: %n%s%n%n", compressed);

            // 3) XOR-add corresponding row/cols from the partner matrix per each group of duplicates.
            xored = xored.combineVectors(lists, combineRow);

            Tuple<BooleanMatrix, BooleanMatrix> output = removeRow? new Tuple<>(xored, compressed): new Tuple<>(compressed, xored);
            return output;
        }
    }


    /**
     * Takes two ArrayList<byte[]> objects that stores rows/cols, two BitSet objects representing row/cols, a boolean flag fixRow.
     * Compares target vector (as byte[] translated from the BitSet object) with the corresponding array.
     *      if the vector already exists in the array, don't add, take down index in array, xor-add the other vector with the other array[index];
     *      else, add both vector to corresponding arrays.
     * @param rowArray: A byte[] array representing existing rows in the row-factor-matrix. (values = (byte) 3 for true, (byte) 0 for false).
     * @param colArray:  A byte[] array representing existing columns in the column-factor-matrix. (values = (byte) 3 for true, (byte) 0 for false).
     * @param rowVec: A BitSet object representing the row vector, where a bit is set if the value is true, otherwise 0.
     * @param colVec: A BitSet object representing the col vector, where a bit is set if the value is true, otherwise 0.
     * @param fixRow: A boolean flag indicating whether to compare the row BitSet with rowArray (if true) or col Bitset with colArray(if false).
     * @return: Tuple<ArrayList<byte[]>, ArrayList<byte[]>> holding the updated rowArray and colArray.
     */

    public static Tuple<ArrayList<byte[]>, ArrayList<byte[]>> compressAndAdd(ArrayList<byte[]> rowArray, ArrayList<byte[]> colArray, BitSet rowVec, BitSet colVec, boolean fixRow){
        int pos = -99;
        int colSize = colArray.get(0).length;
        int rowSize = rowArray.get(0).length;
        byte[] col = toByteArray(colVec, colSize);
        byte[] row = toByteArray(rowVec, rowSize);
        Tuple<ArrayList<byte[]>, ArrayList<byte[]>> output;

        ArrayList<byte[]> fixedArray = fixRow? rowArray : colArray;
        ArrayList<byte[]> partnerArray = fixRow? colArray : rowArray;
        byte[] fixedVec = fixRow? row : col;
        byte[] partner = fixRow? col : row ;

        for(int i = 0; i < fixedArray.size(); i++){
            if(Arrays.equals(fixedArray.get(i), fixedVec)){
                pos = i;
                System.out.printf("Found match at pos = %s%n", pos);
            }
        }
        if(pos > 0){
            byte[] anchor = partnerArray.get(pos);
            byte[] current = partner;
            System.out.printf("pos = %d, anchor = %s, current = %s%n", pos, Arrays.toString(anchor), Arrays.toString(current));
            for(int j = 0; j < anchor.length; j++) {
                anchor[j] = (anchor[j] == current[j]) ? (byte) 0 : (byte) 3;
            }
            output = fixRow? new Tuple<ArrayList<byte[]>, ArrayList<byte[]>> (partnerArray, fixedArray) : new Tuple<ArrayList<byte[]>, ArrayList<byte[]>> (fixedArray, partnerArray);

        }else{
            colArray.add(toByteArray(colVec, colSize));
            rowArray.add(toByteArray(rowVec, rowSize));
            output = new Tuple<ArrayList<byte[]>, ArrayList<byte[]>> (colArray, rowArray);
        }
        return output;
    }


}



