package test;

import baseDemo.RMBUppercase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/21]
 */
public class Test {

    public static void main(String[] args) {
//        test1();
//        test2();
        Info info = new Info(40);
        Info info1 = new Info(8);
        Info info2 = new Info(9);
        Info info3 = new Info(8);
        Info info4 = new Info(58);
        Info info5 = new Info(1);
        Info info6 = new Info(25);
        Info info7 = new Info(1);
        List<Info> list = new ArrayList<>();
        list.add(info);
        list.add(info1);
        list.add(info2);
        list.add(info3);
        list.add(info4);
        list.add(info5);
        list.add(info6);
        list.add(info7);

        Collections.sort(list);
        while(list.size() > 0){
            List<Info> res = new ArrayList<>();
            test3(list.get(0), list, res);
            List<Integer> list1 = res.stream().map(Info::getNumber).collect(Collectors.toList());
            System.out.println(list1);
        }


    }

    /**
     * 返回最佳组合
     */
    private static void test3(Info max, List<Info> resource, List<Info> result){
        result.add(max);
        resource.remove(max);
        if (max.getNumber() >= 50) return;

        //返回集合中还差多少数据
        Integer sum = result.stream().mapToInt(Info::getNumber).sum();
        Integer cha = 50 - sum;

        if (cha == 0) return;
        if (resource.size() == 0) return;

        List<Info> infos = resource.stream().filter(e -> e.getNumber() <= cha).collect(Collectors.toList());
        if (infos == null || infos.size() == 0) return;
        test3(infos.get(0), resource, result);
    }



    private static void test1(){
        // 浮点数的打印
        System.out.println(new BigDecimal("10000000000").toString());
        // 普通的数字字符串
        System.out.println(new BigDecimal("100.000").toString());
        // 去除末尾多余的0
        System.out.println(new BigDecimal("100.000").toString());
        // 避免输出科学计数法
        System.out.println(new BigDecimal("100.000").stripTrailingZeros().toPlainString());
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDate localDate = localDateTime.toLocalDate();
        System.out.println(localDate);
        LocalDate date = LocalDate.now();
        String str1 = date.toString();
        System.out.println(">>>>>>>" + str1);
        System.out.println(Arrays.toString(localDate.toString().split("-")));
        String str = RMBUppercase.number2CNMontrayUnit(new BigDecimal(0.298));
        System.out.println(str);
        System.out.println(new BigDecimal(0.298).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());// 0.2
    }

   static class Info implements Comparable<Info>{

        private String purchaseNo;
        private Integer number;

        public Info(Integer number) {
            this.number = number;
        }

        public String getPurchaseNo() {
            return purchaseNo;
        }

        public void setPurchaseNo(String purchaseNo) {
            this.purchaseNo = purchaseNo;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        @Override
        public int compareTo(Info o) {
            return o.getNumber().compareTo(this.getNumber());
        }
    }

}



