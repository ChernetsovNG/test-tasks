package ru.nchernetsov.test.yandex.test5;

/*
самое важное требование, которое следует удовлетворять в процессе всей реализации -
расставить как можно меньшее количество граблей для разработчика,
который в будущем будет добавлять новые значения

иметь фиксированный список значений, задаваемый на этапе компиялции

иметь строгую типизацию значений

уметь безопасно сравнивать значения по ==

получать все значения

иметь неизменяемый ordinal (порядковый номер с 0 в порядке объявления значений)

получать значения по ordinal

иметь название значения, совпадающее с названием поля для значения

искать по имени
 */

import java.util.ArrayList;
import java.util.List;

public class Enum {

    private static int counter = 0;

    private static final List<Enum> values = new ArrayList<>();

    // Значения Enum'а
    public static final Enum RUB = new Enum("RUB");

    public static final Enum EUR = new Enum("EUR");

    public static final Enum USD = new Enum("USD");

    // ----------------

    private final String value;

    private final int ordinal;

    private Enum(String value) {
        this.value = value;
        this.ordinal = counter++;
        values.add(this);
    }

    public int ordinal() {
        return ordinal;
    }

    public Enum[] values() {
        return values.toArray(Enum[]::new);
    }

    public Enum getByOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal > values.size() - 1) {
            return null;
        }
        return values.get(ordinal);
    }

    public String name() {
        return value;
    }
}

class Main {

    public static void main(String[] args) {
        Example example = Example.A;

        Enum rub = Enum.RUB;

        System.out.println(rub);
    }
}

enum Example {

    A,
    B,
    C
}
