package ru.nchernetsov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * - Создать  класс к примеру Element с двумя атрибутами  int x,int y. (координаты в матрице)
 * - Создать двумерный массив [][] и наполнить его экземплярами созданного класса размер матрицы к примеру 9 элементов
 * - реализовать метод который получает на вход объект класса Element и возвращает список «соседних» экземпляров их матрицы
 */
public class TestTask1 {

    /**
     * Метод возвращает список соседних элементов в массиве для заданного элемента
     */
    public static List<Element> getNeighboringElements(Element[][] arr, Element element) {
        if (element == null) {
            throw new IllegalArgumentException("element is null");
        }
        if (arr == null) {
            return Collections.emptyList();
        }
        int x = element.getX();
        int y = element.getY();

        int n = arr.length;
        int m = arr[0].length;

        // если элемент за границами массива
        if (x < 0 || x >= m) {
            return Collections.emptyList();
        } else if (y < 0 || y >= m) {
            return Collections.emptyList();
        }

        // если массив единичного размера, то соседей нет
        if (n == 1 && m == 1) {
            return Collections.emptyList();
        }

        // максимум может быть 8 соседей, с координатами:
        int x1 = x - 1;
        int y1 = y - 1;

        int x2 = x;
        int y2 = y - 1;

        int x3 = x + 1;
        int y3 = y - 1;

        int x4 = x - 1;
        int y4 = y;

        int x5 = x + 1;
        int y5 = y;

        int x6 = x - 1;
        int y6 = y + 1;

        int x7 = x;
        int y7 = y + 1;

        int x8 = x + 1;
        int y8 = y + 1;

        // Далее, в зависимости от того, лежат ли координаты соседа в пределах массива, добавляем его в
        // результирующий список
        List<Element> result = new ArrayList<>();
        if (isInArray(n, m, x1, y1)) {
            result.add(arr[y1][x1]);
        }
        if (isInArray(n, m, x2, y2)) {
            result.add(arr[y2][x2]);
        }
        if (isInArray(n, m, x3, y3)) {
            result.add(arr[y3][x3]);
        }
        if (isInArray(n, m, x4, y4)) {
            result.add(arr[y4][x4]);
        }
        if (isInArray(n, m, x5, y5)) {
            result.add(arr[y5][x5]);
        }
        if (isInArray(n, m, x6, y6)) {
            result.add(arr[y6][x6]);
        }
        if (isInArray(n, m, x7, y7)) {
            result.add(arr[y7][x7]);
        }
        if (isInArray(n, m, x8, y8)) {
            result.add(arr[y8][x8]);
        }

        return result;
    }

    private static boolean isInArray(int n, int m, int x, int y) {
        return y >= 0 && y <= n - 1 && x >= 0 && x <= m - 1;
    }
}
