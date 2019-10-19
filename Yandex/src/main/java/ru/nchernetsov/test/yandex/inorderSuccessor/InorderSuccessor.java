package ru.nchernetsov.test.yandex.inorderSuccessor;

/**
 * Поиск в двоичном дереве поиска минимального элемента, большего, чем заданный
 */
public class InorderSuccessor {

    public Node inorderSuccessor(Node node) {
        Node right = node.getRight();
        Node parent = node.getParent();
        if (right != null) {
            return getMinimumNode(right);
        } else {
            return parent != null ? liftUntilCannotGoRight(node) : null;
        }
    }

    public Node getMinimumNode(Node node) {
        Node left = node.getLeft();
        return left != null ? getMinimumNode(left) : node;
    }

    public Node liftUntilCannotGoRight(Node node) {
        Node parent = node.getParent();
        if (parent == null) {
            return null;
        }
        if (parent.getRight() == node) {
            return liftUntilCannotGoRight(parent);
        } else if (parent.getLeft() == node) {
            return parent;
        }
        return null;
    }
}
