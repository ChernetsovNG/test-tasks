package ru.nchernetsov.test.yandex.test6;

import java.util.Arrays;

public class Main {

    public static int minimalProduct(int[] array) {
        int length = array.length;
        if (array.length == 0) {
            throw new IllegalArgumentException("");
        } else if (array.length == 1) {
            throw new IllegalArgumentException("");
        }
        Arrays.sort(array);
        int first = array[0];
        int last = array[array.length - 1];
        if (first < 0 && last < 0) {
            return last * array[length - 2];
        } else if (first >= 0 && last >= 0) {
            return first * array[1];
        } else {
            return first * last;
        }
    }
}
