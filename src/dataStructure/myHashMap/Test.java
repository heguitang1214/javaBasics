package dataStructure.myHashMap;


/**
 * @author he_guitang
 * @version [1.0 , 2018/8/31]
 */
public class Test {

    public static void main(String[] args) {

        Map<String, String> map = new HashMap<>();
        for (int i = 1; i < 600; i++) {
            map.put(i + "", "数据" + i);
        }


        System.out.println(map.size());

        System.out.println(map.get("582"));

    }


}
