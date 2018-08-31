package dataStructure.myHashMap;


/**
 * @author he_guitang
 * @version [1.0 , 2018/8/31]
 */
public class Test {

    public static void main(String[] args) {

        Map<String, String> map = new HashMap<>();
        map.put("1", "数据1");
        map.put("2", "数据2");
        map.put("1", "数据1修改");

        System.out.println(map.size());

        System.out.println(map.get("1"));
        System.out.println(map.get("2"));

    }


}
