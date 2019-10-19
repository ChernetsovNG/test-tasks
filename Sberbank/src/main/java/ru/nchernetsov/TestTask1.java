package ru.nchernetsov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Создать класс, к примеру Element, с двумя атрибутами int x, int y (координаты в матрице)
 * Создать двумерный массив [][] и наполнить его экземплярами созданного класса (размер матрицы, к примеру, 9 элементов)
 * реализовать метод, который получает на вход объект класса Element, и возвращает список «соседних» элементов из матрицы
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

        // если массив единичного размера, то соседей нет
        if (n == 1 && m == 1) {
            return Collections.emptyList();
        }

        // если элемент за границами массива, то соседей нет
        if (!isInArray(n, m, x, y)) {
            return Collections.emptyList();
        }

        /*
         максимум может быть 8 соседей. В цикле по соседям, в зависимости от того,
         лежат ли координаты соседа в пределах массива, добавляем их в результирующий список
         */
        List<Element> result = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;  // сам элемент пропускаем
                }
                int neighborY = y + i;
                int neighborX = x + j;
                if (isInArray(n, m, neighborX, neighborY)) {
                    result.add(arr[neighborY][neighborX]);
                }
            }
        }

        return result;
    }

    private static boolean isInArray(int n, int m, int x, int y) {
        return y >= 0 && y <= n - 1 && x >= 0 && x <= m - 1;
    }
}
