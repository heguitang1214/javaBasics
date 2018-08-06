package dataStructure.myTree;

import java.util.ArrayList;
import java.util.List;

/**
 * 树的操作实现
 */
public class BinarySearchTree<T extends Comparable> implements Tree<T> {
    //根节点
    private BinaryNode<T> root;

    public BinarySearchTree() {
        root = null;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int height() {
        return 0;
    }

    /**
     * 先序遍历
     */
    @Override
    public String preOrder() {
        String sb = preOrder(root);
        if (sb.length() > 0) {
            //去掉尾部","号
            sb = sb.substring(0, sb.length() - 1);
        }
        return sb;
    }

    private String preOrder(BinaryNode<T> subtree) {
        StringBuffer sb = new StringBuffer();
        if (subtree != null) {//递归结束条件
            //先访问根结点
            sb.append(subtree.data + ",");
            //遍历左子树
            sb.append(preOrder(subtree.left));
            //遍历右子树
            sb.append(preOrder(subtree.right));
        }
        return sb.toString();
    }

//    public List<T> preOrder1() {
//
////        preOrder1(listResult, root);
////        return listResult;
//    }

//    private List<T> preOrder1(BinaryNode<T> subtree) {
//        List<T> listResult = new ArrayList<>();
//        if (subtree != null) {//递归结束条件
//            listResult.add(subtree.data);
//
//            List<T> a = preOrder1(subtree.left);
//
//            listResult.add(a);
//            listResult.add((T) preOrder1(subtree.right));
//        }
//        return listResult;
//    }


    @Override
    public String inOrder() {
        String sb = inOrder(root);
        if (sb.length() > 0) {
            //去掉尾部","号
            sb = sb.substring(0, sb.length() - 1);
        }
        return sb;
    }

    public String inOrder(BinaryNode<T> subtree) {
        StringBuilder sb = new StringBuilder();
        if (subtree != null) {//递归结束条件
            //先遍历左子树
            sb.append(inOrder(subtree.left));
            //再遍历根结点
            sb.append(subtree.data).append(",");
            //最后遍历右子树
            sb.append(inOrder(subtree.right));
        }
        return sb.toString();
    }

    @Override
    public String postOrder() {
        return null;
    }

    @Override
    public String levelOrder() {
        return null;
    }

    @Override
    public void insert(T data) {
        if (data == null)
            throw new RuntimeException("数据不能为空!");
        //插入操作
        root = insert(data, root);
    }

    //递归实现
    private BinaryNode<T> insert(T data, BinaryNode<T> p) {
        if (p == null) {
            p = new BinaryNode<>(data, null, null);
        }

        int compareResult = data.compareTo(p.data);

        if (compareResult < 0) {//左
            p.left = insert(data, p.left);//创建一个节点,给当前节点的左节点
        } else if (compareResult > 0) {//右
            p.right = insert(data, p.right);
        } else {
            ;//已有元素就没必要重复插入了
        }
        return p;
    }


    @Override
    public void remove(T data) {

    }

    @Override
    public T findMin() {
        return null;
    }

    @Override
    public T findMax() {
        return null;
    }

    @Override
    public BinaryNode findNode(T data) {
        return null;
    }

    @Override
    public boolean contains(T data) throws Exception {
        return false;
    }

    @Override
    public void clear() {

    }
}
