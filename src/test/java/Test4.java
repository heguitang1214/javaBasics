
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test4 {

    public static void main(String[] args) {

        String name = "2018vc45";

        String zm = name.replaceAll("[^(a-zA-Z)]","");  //取出字母

        String number = name.replaceAll("[^(0-9)]", "");   //取出数字

        String a = name.replaceAll("8", "A");

        System.out.println(a);
        System.out.println(zm);
        System.out.println(number);
        System.out.println("C".matches("[^(0-9)]"));


        List<String> list = new ArrayList<>();
        list.add("2018s");
        list.add("2018lx ");
        for (String str : list){
            if (str.endsWith("s")){
                System.out.println("s");
            }else if (str.trim().endsWith("x")){
                System.out.println("x");
            }
        }



        String string = "121.dsad";
        String[] strings = string.split("\\.");
        System.out.println(Arrays.toString(strings));


    }

}
