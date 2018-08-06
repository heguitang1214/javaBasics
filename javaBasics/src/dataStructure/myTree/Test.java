package dataStructure.myTree;

import java.util.List;

/**
 * Created by 11256 on 2018/7/29.
 */
public class Test {

    public static void main(String[] args) {

        BinarySearchTree<Integer> binarySearchTree = new BinarySearchTree<>();

        binarySearchTree.insert(7);
        binarySearchTree.insert(2);
        binarySearchTree.insert(1);
        binarySearchTree.insert(5);
        binarySearchTree.insert(9);
        binarySearchTree.insert(8);
        binarySearchTree.insert(11);



        String str = binarySearchTree.preOrder();
        System.out.println("先序遍历结果:" + str);
        String str1 = binarySearchTree.inOrder();
        System.out.println("中序遍历结果:" + str1);

//        List<Integer> str2 = binarySearchTree.preOrder1();
//        System.out.println("先序遍历结果:" + str2);

        A a = new A();
        System.out.println("asdas:"+a.print("213"));

    }





}
