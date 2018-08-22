package baseDemo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/22]
 *          组合集合内中的数据,获取到每一个组合
 *          获取集合内满足相加为50的数据
 */
public class DataCombination {

    public static void main(String[] args) {
        Distribution info40 = new Distribution(40);
        Distribution info8 = new Distribution(8);
        Distribution info9 = new Distribution(9);
        Distribution info58 = new Distribution(58);
        Distribution info1 = new Distribution(1);
        Distribution info25 = new Distribution(25);
        List<Distribution> list = new ArrayList<>();
        list.add(info40);
        list.add(info8);
        list.add(info9);
        list.add(info8);
        list.add(info58);
        list.add(info1);
        list.add(info25);
        list.add(info1);



    }

    /**
     * 返回最佳组合
     */
    private static void distributionMethod(Distribution max, List<Distribution> resource, List<Distribution> result){
        result.add(max);
        resource.remove(max);
        if (max.getNumber() >= 50) return;

        //返回集合中还差多少数据
        Integer sum = result.stream().mapToInt(Distribution::getNumber).sum();
        Integer difference = 50 - sum;

        if (difference == 0) return;
        if (resource.size() == 0) return;

        List<Distribution> infos = resource.stream().filter(e -> e.getNumber() <= difference).collect(Collectors.toList());
        if (infos == null || infos.size() == 0) return;
        distributionMethod(infos.get(0), resource, result);
    }


    /**
     * 分配数据实体
     */
    static class Distribution implements Comparable<Distribution> {
        private String purchaseNo;//其他的数据
        private Integer number;

        public Distribution(Integer number) {
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
        public int compareTo(Distribution o) {
            return o.getNumber().compareTo(this.getNumber());
        }
    }


}
