package ru.nchernetsov;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.nchernetsov.TestTask1.getNeighboringElements;

public class TestTask1Test {

    @Test
    public void elementNotInMatrixShouldReturnEmptyList() {
        Element[][] matrix = {{new Element(0, 0)}};

        List<Element> neighboringElements = getNeighboringElements(matrix, new Element(1, 1));

        // элемента (1,1) нет в массиве
        assertThat(neighboringElements).isEmpty();
    }

    @Test
    public void elementInMatrixOneByOneShouldReturnEmptyList() {
        Element[][] matrix = {{new Element(0, 0)}};

        List<Element> neighboringElements = getNeighboringElements(matrix, new Element(0, 0));

        // элемента 1 есть в массиве, но у него нет соседей
        assertThat(neighboringElements).isEmpty();
    }

    @Test
    public void elementBeyondArrayBoundsByXShouldReturnEmptyList() {
        Element[][] matrix = {
                {new Element(0, 0), new Element(1, 0)},
                {new Element(0, 1), new Element(1, 1)}
        };

        List<Element> neighboringElements = getNeighboringElements(matrix, new Element(-1, 1));

        assertThat(neighboringElements).isEmpty();
    }

    @Test
    public void elementBeyondArrayBoundsByYShouldReturnEmptyList() {
        Element[][] matrix = {
                {new Element(0, 0), new Element(1, 0)},
                {new Element(0, 1), new Element(1, 1)}
        };

        List<Element> neighboringElements = getNeighboringElements(matrix, new Element(1, 5));

        assertThat(neighboringElements).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullElementShouldThrowException() {
        Element[][] matrix = {{new Element(0, 0)}};
        getNeighboringElements(matrix, null);
    }

    @Test
    public void elementShouldReturnItNeighboursTest1() {
        Element[][] matrix = {
                {new Element(0, 0), new Element(1, 0)},
                {new Element(0, 1), new Element(1, 1)}};

        List<Element> neighboringElements = getNeighboringElements(matrix, new Element(0, 0));

        assertThat(neighboringElements).hasSize(3);
        assertThat(neighboringElements.get(0)).isEqualTo(new Element(1, 0));
        assertThat(neighboringElements.get(1)).isEqualTo(new Element(0, 1));
        assertThat(neighboringElements.get(2)).isEqualTo(new Element(1, 1));
    }

    @Test
    public void elementShouldReturnItNeighboursTest2() {
        Element[][] matrix = {
                {new Element(0, 0), new Element(1, 0), new Element(2, 0)},
                {new Element(0, 1), new Element(1, 1), new Element(2, 1)},
                {new Element(0, 2), new Element(1, 2), new Element(2, 2)}};

        List<Element> neighboringElements = getNeighboringElements(matrix, new Element(1, 1));

        assertThat(neighboringElements).hasSize(8);
        assertThat(neighboringElements.get(0)).isEqualTo(new Element(0, 0));
        assertThat(neighboringElements.get(1)).isEqualTo(new Element(1, 0));
        assertThat(neighboringElements.get(2)).isEqualTo(new Element(2, 0));
        assertThat(neighboringElements.get(3)).isEqualTo(new Element(0, 1));
        assertThat(neighboringElements.get(4)).isEqualTo(new Element(2, 1));
        assertThat(neighboringElements.get(5)).isEqualTo(new Element(0, 2));
        assertThat(neighboringElements.get(6)).isEqualTo(new Element(1, 2));
        assertThat(neighboringElements.get(7)).isEqualTo(new Element(2, 2));
    }

    @Test
    public void elementShouldReturnItNeighboursTest3() {
        Element[][] matrix = {
                {new Element(0, 0), new Element(1, 0), new Element(2, 0)},
                {new Element(0, 1), new Element(1, 1), new Element(2, 1)},
                {new Element(0, 2), new Element(1, 2), new Element(2, 2)}};

        List<Element> neighboringElements = getNeighboringElements(matrix, new Element(2, 1));

        assertThat(neighboringElements).hasSize(5);
        assertThat(neighboringElements.get(0)).isEqualTo(new Element(1, 0));
        assertThat(neighboringElements.get(1)).isEqualTo(new Element(2, 0));
        assertThat(neighboringElements.get(2)).isEqualTo(new Element(1, 1));
        assertThat(neighboringElements.get(3)).isEqualTo(new Element(1, 2));
        assertThat(neighboringElements.get(4)).isEqualTo(new Element(2, 2));
    }

    @Test
    public void elementShouldReturnItNeighboursTest4() {
        // массив 2 х 3
        Element[][] matrix = {
                {new Element(0, 0), new Element(1, 0), new Element(2, 0)},
                {new Element(0, 1), new Element(1, 1), new Element(2, 1)}};

        List<Element> neighboringElements = getNeighboringElements(matrix, new Element(1, 1));

        assertThat(neighboringElements).hasSize(5);
        assertThat(neighboringElements.get(0)).isEqualTo(new Element(0, 0));
        assertThat(neighboringElements.get(1)).isEqualTo(new Element(1, 0));
        assertThat(neighboringElements.get(2)).isEqualTo(new Element(2, 0));
        assertThat(neighboringElements.get(3)).isEqualTo(new Element(0, 1));
        assertThat(neighboringElements.get(4)).isEqualTo(new Element(2, 1));
    }
}
