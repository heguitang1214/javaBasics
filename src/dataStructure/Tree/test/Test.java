package dataStructure.Tree.test;

import dataStructure.Tree.BinaryTree.BinarySearchTree;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/6]
 */
public class Test {
    public static void main(String[] args) {
        BinarySearchTree<Integer> binarySearchTree = new BinarySearchTree<>();
        binarySearchTree.insert(75);
        binarySearchTree.insert(5);
        binarySearchTree.insert(52);
        binarySearchTree.insert(98);
        binarySearchTree.insert(54);
        binarySearchTree.insert(23);
        binarySearchTree.insert(65);
        binarySearchTree.insert(100);
        binarySearchTree.insert(110);
        binarySearchTree.insert(120);
        binarySearchTree.insert(130);
        binarySearchTree.insert(64);
        binarySearchTree.print();

        binarySearchTree.remove(52);

        binarySearchTree.print();

        System.out.println("先序遍历" + binarySearchTree.preOrder());
//        System.out.println("先序" + binarySearchTree1.preOrder());
        System.out.println("中序遍历" + binarySearchTree.inOrder());
//        System.out.println("中序" + binarySearchTree1.inOrder());
        System.out.println("后序遍历" + binarySearchTree.postOrder());
//        System.out.println("后序" + binarySearchTree1.postOrder());
        System.out.println("层次遍历" + binarySearchTree.levelOrder());

        System.out.println("深度:" + binarySearchTree.height());
        System.out.println("大小:" + binarySearchTree.size());
        System.out.println("最小:" + binarySearchTree.findMin());
        System.out.println("最大:" + binarySearchTree.findMax());
//        System.out.println("大小:" + binarySearchTree.size1());




    }
}
