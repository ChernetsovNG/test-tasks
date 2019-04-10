package ru.nchernetsov.test.yandex.test1;

public class Main {

    /*
    Пусть N такое число, что 0xff = 0xc0 + N. Напишите представление числа N в десятичной системе счисления
    */
    public static void main(String[] args) throws InterruptedException {
        int a = 0xff;
        int b = 0xc0;
        int N = a - b;
        System.out.printf("a = %d, b = %d, N = %d\n", a, b, N);
    }
}
