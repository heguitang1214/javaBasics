public class Test5 {

    public static void main(String[] args) {

        String name = "2018vc45";
        String number = name.replaceAll("[^(0-9)]", "");   //取出数字
        System.out.println(number);
        String str = "23";
        System.out.println(str.matches("[\\d]+"));



        String number1 = name.replaceAll("[(0-9)]", "");   //取出数字
        System.out.println("aaaaaaBBBBB".replaceAll("a", "c"));


        System.out.println("2018/01/31".matches("[1-9]{1}[0-9]{3}([./])\\d{1,2}\\1\\d{1,2}"));

        System.out.println("2018.01.31".replaceAll("[./]", "-"));

    }

}
