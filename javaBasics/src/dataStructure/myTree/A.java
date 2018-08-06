package dataStructure.myTree;

/**
 * Created by 11256 on 2018/7/29.
 */
public class A  {

    private int sum = 0;

    public String print(String str){
        StringBuilder sb = new StringBuilder();
        sum ++;
        if (sum < 10){
            sb.append(str);
            print(sum + ",");
        }
        return sb.toString();
    }



}
