package test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/27]
 */
public class Test1 {

    File f = new File(this.getClass().getResource("/").getPath());
    File f1 = new File(this.getClass().getResource("").getPath());

    public static void main(String[] args) {
        //  /D:/myjava/javaBasics/out/production/javaBasics/
        System.out.println(Class.class.getClass().getResource("/").getPath() );

        String str = "1";
        System.out.println(str.matches("^//d+(//.//d+)?$"));
        System.out.println(str.matches("^(-)?[0-9]*$"));


        Set<Integer> ids = new HashSet<>();
        ids.add(3);

        Set<Integer> idss = new HashSet<>();
        idss.add(3);
        idss.add(32);
        idss.add(33);
        ids.removeAll(idss);

        System.out.println(ids);

    }

}
