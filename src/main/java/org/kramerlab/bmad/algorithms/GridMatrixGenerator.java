package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.matrix.BooleanMatrix;

import javax.swing.text.StyledEditorKit;
import java.util.ArrayList;
import java.util.Random;

/**
 * Generates special matrices including:
 *      framed matrix, chessboard matrix, or user-specified matrices with grids of 1s and 0s (with optional noise as flipped values).
 * Returns a BooleanMatrix object. Can be converted to Weka Instances object by calling the .toInstances();.
 */

public class GridMatrixGenerator {


    /**
     * Test code of the static methods. Can use as example of calling.
     */
    public static void main(String[] args) {

        int blockHeight = 7;
        int blockWidth = 8;
        int numRowFromBlock = 2;
        int numColFromBlock = 3;
        boolean ones1 = false;
        Test_getCentredFramedGrid(blockHeight, blockWidth, numRowFromBlock, numColFromBlock, ones1);



        System.out.println("\n\n\n");
        int totalHeight = 15;
        int totalWidth = 20;
        int[] endpoints = {2, 3, 7, 10};
        boolean ones2 = true;
        Test_getAnyFramedGrid(totalHeight, totalWidth, endpoints, ones2);

        System.out.println("\n\n\n");
        int numSquarePerRow = 5;
        int squareDim = 4;
        boolean ones3 = false;
        Test_getSquareChessBoard (numSquarePerRow, squareDim, ones3);

    }


    //---------------------------3 test methods ------------------------------------------------------------------

    private static void Test_getCentredFramedGrid(int blockHeight, int blockWidth, int numRowFromBlock, int numColFromBlock, boolean ones)  {

        try {
            BooleanMatrix output = getCentredFramedGrid(blockHeight, blockWidth, numRowFromBlock, numColFromBlock, ones);
            System.out.println(output);

            addNoiseToAll(output, 0.3);
            System.out.printf("%n%n%s", output);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private static void Test_getAnyFramedGrid(int totalHeight, int totalWidth, int[] endpoints, boolean ones)  {

        try {
            BooleanMatrix output = getAnyFramedGrid(totalHeight, totalWidth, endpoints, ones);
            System.out.println(output);

            addNoiseToAll(output, 0.3);
            System.out.printf("%n%n%s", output);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private static void Test_getSquareChessBoard (int numSquarePerRow, int squareDim, boolean startOnes) {

        BooleanMatrix output = getSquareChessBoard (numSquarePerRow, squareDim, startOnes);
        System.out.println(output);

        addNoiseToAll(output, 0.3);
        System.out.printf("%n%n%s", output);
    }

    //---------------------------3 test methods --------------------------------------------------------------------





    /**
     * Returns a BooleanMatrix object with a block at user-specified location filled with opposite value as the frame.
     * @param blockHeight: number of rows of the centre block.
     * @param blockWidth: number of cols of the centre block.
     * @param numRowFromBlock: number of rows above and below the block.
     * @param numColFromBlock: number of cols on both sides of the block.
     * @param ones: fill-value (true = (byte) 3, false = (byte) 0).
     * @return: BooleanMatrix object.
     * @throws IllegalArgumentException
     */

    public static BooleanMatrix getCentredFramedGrid (int blockHeight, int blockWidth, int numRowFromBlock, int numColFromBlock, boolean ones) throws IllegalArgumentException{
        int totalHeight = blockHeight + 2 * numRowFromBlock;
        int totalWidth = blockWidth + 2 * numColFromBlock;

        byte[][] output = new byte[totalHeight][totalWidth];
        byte valFill = (ones)? (byte) 3 : (byte) 0;
        byte valFrame = (ones)? (byte) 0: (byte) 3;

        System.out.printf("------------ Called getCentredFramedGrid ------------%n");
        System.out.printf("Matrix height = %d, width = %d, block height = %d, block width = %d, %d rows from top/bottom, %d cols from sides%n",
                totalHeight, totalWidth, blockHeight, blockWidth, numRowFromBlock, numColFromBlock);
        System.out.printf("Block position:  rows %d to %d, cols %d to %d%n%n", numRowFromBlock, blockHeight + numRowFromBlock, blockWidth, blockWidth + numColFromBlock);


        if(numRowFromBlock > totalHeight || numColFromBlock > totalWidth){
            throw new IllegalArgumentException("WARNING! The number of row / col from frame must be <= total height / width!");
        }else{
            for(int i = 0; i < totalHeight; i ++){
                for(int j = 0; j < totalWidth; j ++){
                    output[i][j] = (i >= numRowFromBlock && i <= totalHeight - numRowFromBlock - 1
                                    && j >= numColFromBlock && j <= totalWidth - numColFromBlock - 1)? valFill : valFrame;
                }
            }
            return new BooleanMatrix(output);
        }
    }





    /**
     * Returns a BooleanMatrix object with a block at user-specified location filled with opposite value as the frame.
     * @param totalHeight
     * @param totalWidth
     * @param endpoints: upper left and lower right coordinates of the center grid:
     *                 [0] upper left corner - row #
     *                 [1] upper left corner - col #
     *                 [2] lower right corner - row #
     *                 [3] lower right corner - col #
     * @param ones: fill-value (true = (byte) 3, false = (byte) 0).
     * @return a BooleanMatrix object.
     * @throws IllegalArgumentException if block boundary is outside the overalll matrix.
     */
    public static BooleanMatrix getAnyFramedGrid (int totalHeight, int totalWidth, int[] endpoints, boolean ones) throws IllegalArgumentException{
        int upleft_r = endpoints[0];
        int upleft_c = endpoints[1];
        int lowright_r = endpoints[2];
        int lowright_c = endpoints[3];

        byte[][] output = new byte[totalHeight][totalWidth];
        byte valFill = (ones)? (byte) 3 : (byte) 0;
        byte valFrame = (ones)? (byte) 0: (byte) 3;

        int blockHeight = endpoints[3] - endpoints[1];
        int blockWidth = endpoints[2] - endpoints[0];

        System.out.printf("------------ Called getAnyFramedGrid ------------%n");
        System.out.printf("Matrix height = %d, width = %d, block height = %d, block width = %d%n", totalHeight, totalWidth, blockHeight, blockWidth);
        System.out.printf("Block position:  rows %d to %d, cols %d to %d%n%n", upleft_r, upleft_c, lowright_r,  lowright_c);

        if(endpoints.length != 4){
            throw new IllegalArgumentException("WARNING! int[] endpoint takes exactly 4 integer values of the row-col coordinates for upper-left and lower-right corners of the center block!");
        } else if((upleft_r > lowright_r || upleft_c > lowright_c) || (upleft_r == lowright_r && upleft_c == lowright_c)){
            throw new IllegalArgumentException("WARNING! upper-left coordinates must be both smaller than the lower-right corner coordinates!");
        }else{
            for(int i = 0; i < totalHeight; i ++){
                for(int j = 0; j < totalWidth; j ++){
                    output[i][j] = (i >= upleft_r && i <= lowright_r && j >= upleft_c && j <= lowright_c)? valFill : valFrame;
                }
            }
            return new BooleanMatrix(output);
        }
    }





    /**
     * Generates an n x n square chessboard patter matrix consists of k smaller squares per row/col.      *
     * @param numSquarePerRow: total number of unit squares per each col/ row.
     * @param squareDim: dimension of the unit square (k x k)
     * @param startOnes: starting unit fill = (if startOnes: (byte) 3), else: (byte) 0)
     * @return BooleanMatrix object
     */

    public static BooleanMatrix getSquareChessBoard (int numSquarePerRow, int squareDim, boolean startOnes){

        int size = numSquarePerRow * squareDim;
        boolean toFill = startOnes;

        byte[][] output = new byte[size][size];

        System.out.printf("------------ Called getSquareChessBoard ------------%n");
        System.out.printf("Chessboard setting:  # of blocks = %d, each block is %d by %d, starting with startOnes = %b%n%n",
                numSquarePerRow, squareDim, squareDim, startOnes);

        for(int i = 0; i < size; i ++) {
            toFill = ((i > 0 && i % (squareDim) == 0) ? !toFill : toFill);  // only flip fill value when row > 0 and rows-done == block-height;

            for (int j = 0; j < size; j++) {
                toFill = ((j > 0 && j % (squareDim) == 0) ? !toFill : toFill); // only flip fill value when col > 0 and columns-done == block-width;
                output[i][j] = ((toFill ? (byte) 3 : (byte) 0));
            }

                toFill = (numSquarePerRow % 2 == 0)? !toFill : toFill;  // if have even number of squares, flip again to start next row.
        }
        return new BooleanMatrix(output);
    }



    /**
     * In-place method. Adds noise to the WHOLE matrix by randomly flipping value in n% of all cells.
     * @param matrix: a BooleanMatrix object.
     * @param noiseRatio: percentage of noise out of all cells as a floating point number.
     */

    public static void addNoiseToAll(BooleanMatrix matrix, double noiseRatio) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();

        System.out.printf("%n................addNoiseToAll called.%nPerceived matrix height = %d, width = %d%n", height, width);

        int numNoise = (int) Math.round(height * width * noiseRatio);
        System.out.printf("NoiseRatio = %f, number of cells to flip as noise: %d%n", noiseRatio, numNoise);

        Random randGenerator = new Random();
        ArrayList<String> randNumList = new ArrayList<String>();

        int x, y;

        for (int j = 0; j < numNoise; j++) {
            x = randGenerator.nextInt(height);
            y = randGenerator.nextInt(width);

            String xy = String.valueOf(x) + String.valueOf(y);

            // if duplicate, regenerate random pair of coordinates, else flip the value in grid[x][y].
            if (randNumList.contains(xy)) {
                x = randGenerator.nextInt(height);
                y = randGenerator.nextInt(width);
            } else {
//                matrix.update(x, y, matrix.not(matrix.apply(x, y)));
                matrix.update(x, y, matrix.not(matrix.apply(x, y)));
            }
                randNumList.add(xy);
            }
        }





}


