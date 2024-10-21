package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int aColumns = matrixA.length;
        final int aRows = matrixA[0].length;
        final int bColumns = matrixB.length;
        final int bRows = matrixB[0].length;
        final int[][] matrixC = new int[aColumns][aRows];

        List<Callable<Boolean>> callables = new ArrayList<>();
        for (int j = 0; j < bColumns; j++) {
            final int m = j;
            Callable<Boolean> callable = () -> calculate(matrixA, matrixB, matrixC, aColumns, aRows, bRows, m);
            callables.add(callable);
        }
        executor.invokeAll(callables);
        return matrixC;
    }

    private static Boolean calculate(int[][] matrixA, int[][] matrixB, int[][] matrixC, int aColumns, int aRows, int bRows, int m ) {
        final int[] thatColumn = new int[bRows];
        for (int k = 0; k < aColumns; k++ ) {
            thatColumn[k] = matrixB[k][m];
        }

        for (int i = 0; i < aRows; i++) {
            int[] thisRow = matrixA[i];
            int sum = 0;
            for (int k = 0; k < aColumns; k++) {
                sum+=thisRow[k] * thatColumn[k];
            }
            matrixC[i][m] = sum;
        }
        return true;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int aColumns = matrixA.length;
        final int aRows = matrixA[0].length;
        final int bRows = matrixB[0].length;
        final int[][] matrixC = new int[aColumns][aRows];

        try {
            for (int j = 0; ;j++) {
                calculate(matrixA, matrixB, matrixC, aColumns, aRows, bRows, j);
            }
        } catch (IndexOutOfBoundsException ex) {
            //ignore
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
