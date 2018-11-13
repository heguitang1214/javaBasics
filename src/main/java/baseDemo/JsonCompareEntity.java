package baseDemo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import utils.json.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


/**
 * 利用Json比较实体的内容
 */
public class JsonCompareEntity {

    /**
     *  比较json内容，提供给对外调用
     * @param beforeJson json1
     * @param afterJson json2
     * @param relEntities 对应字段映射关系
     * @return 返回List描述结果
     */
    private static List<String> compareEntitys(String beforeJson, String afterJson, List<RelEntity> relEntities) {
        //返回值的容器
        List<String> resultList = new ArrayList<>();
        if (relEntities == null || relEntities.size() == 0){
            return resultList;
        }
        JSONObject beforeObject;
        JSONObject afterObject;
        try {
            beforeObject = JSONArray.parseObject(beforeJson);
            afterObject = JSONArray.parseObject(afterJson);
        }catch (Exception e){
            System.out.println("JSON数据格式异常，转换失败！");
            resultList.add("JSON数据格式异常，转换失败");
            return resultList;
        }
        try {
            entityContrast(beforeObject, afterObject, relEntities, resultList);
        }catch (Exception e){
            System.out.println("JSON数据对比出现未知异常！");
            resultList.clear();
            resultList.add("JSON数据对比出现未知异常");
            return resultList;
        }
        return resultList;
    }

    /**
     *  对象的对比方法
     * @param beforeSrc 对象1
     * @param afterSrc 对象2
     * @param relEntities 映射关系
     * @param resultList 返回容器
     */
    private static void entityContrast(Object beforeSrc, Object afterSrc, List<RelEntity> relEntities, List<String> resultList) {
        //映射关系字段
        for (RelEntity relEntity : relEntities) {
            String[] beforeArr = relEntity.getPartyA().split("\\.");
            String[] afterArr = relEntity.getPartyB().split("\\.");
            LinkedList<String> beforeList = arrTurnLinkedList(beforeArr);
            LinkedList<String> afterList = arrTurnLinkedList(afterArr);
            analysisJson(beforeSrc, afterSrc, beforeList, afterList, null, null, relEntity, resultList);
        }
    }

    /**
     * 处理JSON数据对比
     */
    private static void analysisJson(Object beforeData, Object afterData,
                                     LinkedList<String> beforeLinkedList, LinkedList<String> afterLinkedList,
                                     String beforeKey, String afterKey,
                                     RelEntity relEntity, List<String> resultList) {
        if (beforeLinkedList.size() > 0) {
            beforeKey = beforeLinkedList.getFirst();
        }
        if (afterLinkedList.size() > 0) {
            afterKey = afterLinkedList.getFirst();
        }
        if (beforeLinkedList.size() > 0) {
            beforeLinkedList.removeFirst();
        }
        if (afterLinkedList.size() > 0) {
            afterLinkedList.removeFirst();
        }
        if (beforeData == null || afterData == null) {
            resultList.add(relEntity.getPartyADesc() + "是[" + beforeData + "]," + relEntity.getPartyBDesc() + "是[" + afterData + "]");
            return;
        }
        if (beforeData instanceof JSONObject) {
            Object beforeObject = ((JSONObject) beforeData).get(beforeKey);
            if (afterData instanceof JSONObject) {
                Object afterObject = ((JSONObject) afterData).get(afterKey);
                //实现不同深度对象的比较
                if (beforeLinkedList.size() == 0 && afterLinkedList.size() == 0) {
                    //对象类型的比较
                    objectTypeCompare(beforeObject, afterObject, relEntity, resultList);
                } else if (beforeLinkedList.size() == 0 && afterLinkedList.size() > 0) {
                    analysisJson(beforeData, afterObject, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                } else if (beforeLinkedList.size() > 0 && afterLinkedList.size() == 0) {
                    analysisJson(beforeObject, afterData, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                } else {
                    analysisJson(beforeObject, afterObject, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                }
            } else if (afterData instanceof JSONArray) {
                JSONArray afterJsonArr = (JSONArray) afterData;
//                直接跳到末尾节点,对象和数组的对比
                String lastNode = afterLinkedList.getLast();
                afterLinkedList.clear();
                if (lastNode.contains("#")) {
                    afterKey = lastNode.split("#")[0];
                }
                for (Object object : afterJsonArr) {
                    analysisJson(beforeData, object, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                }
            } else {
                resultList.add("类型匹配失败:" + relEntity.getPartyADesc() + "属于JSONObject类型，" + relEntity.getPartyBDesc() + "未匹配到合适的类型,既不是JSONObject,也不是JSONArray,它的值是:" + afterData);
            }
        } else if (beforeData instanceof JSONArray) {
            JSONArray beforeJsonArr = (JSONArray) beforeData;
            if (afterData instanceof JSONArray) {
                JSONArray afterJsonArr = (JSONArray) afterData;
                jsonArrayHandle(beforeJsonArr, afterJsonArr, beforeLinkedList, afterLinkedList,
                        beforeKey, afterKey, relEntity, resultList);
            } else if (afterData instanceof JSONObject) {
//                数组和对象的对比
                String lastNode = beforeLinkedList.getLast();
                beforeLinkedList.clear();
                if (lastNode.contains("#")) {
                    beforeKey = lastNode.split("#")[0];
                }
                //数据循环对比
                for (Object object : beforeJsonArr) {
                    analysisJson(object, afterData, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                }
            } else {
                resultList.add("类型匹配失败:" + relEntity.getPartyADesc() + "属于JSONObject类型，" + relEntity.getPartyBDesc() + "未匹配到合适的类型,既不是JSONObject,也不是JSONArray,它的值是:" + afterData);
            }
        } else {
            resultList.add("类型匹配失败:" + relEntity.getPartyADesc() + "既不是一个对象,也不是一个数组!其内容是:" + beforeData);
        }
    }

    private static void jsonArrayHandle(JSONArray beforeJsonArr, JSONArray afterJsonArr,
                                        LinkedList<String> beforeLinkedList, LinkedList<String> afterLinkedList,
                                        String beforeKey, String afterKey, RelEntity relEntity, List<String> resultList) {
        String beforeSortKey = null, afterSortKey = null, beforeSrc = beforeKey, afterSrc = afterKey;
        if (beforeKey != null && beforeKey.contains("#")) {
            beforeSortKey = getSortKey(beforeKey);
            beforeKey = beforeKey.split("#")[0];
        }
        if (afterKey != null && afterKey.contains("#")) {
            afterSortKey = getSortKey(afterKey);
            afterKey = afterKey.split("#")[0];
        }
        if (beforeLinkedList.size() == 0 && afterLinkedList.size() == 0) {
            String masterKey, slaveKey;
            for (int i = 0; i < beforeJsonArr.size(); i++) {
                beforeLinkedList.addFirst(beforeKey);
                afterLinkedList.addFirst(afterKey);
                Object afterObject = null;
                masterKey = ((JSONObject) beforeJsonArr.get(i)).getString(beforeSortKey);
                for (int j = 0; j < afterJsonArr.size(); j++) {
                    slaveKey = ((JSONObject) afterJsonArr.get(j)).getString(afterSortKey);
                    if (StringUtils.isNotBlank(masterKey) && StringUtils.isNotBlank(slaveKey)) {
                        if (masterKey.equals(slaveKey)) {
                            afterObject = afterJsonArr.get(j);
                        }
                    } else {
                        if (i < afterJsonArr.size()) {
                            afterObject = afterJsonArr.get(i);
                        }
                    }
                }
                analysisJson(beforeJsonArr.get(i), afterObject, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
            }
        } else {
            String lastNode = "";
            //层级不同的时候,需要进行排序操作
            if (beforeLinkedList.size() > 0) {
//                afterLinkedList.addFirst(afterKey);
                for (int i = 0; i < beforeJsonArr.size(); i++) {
                    afterLinkedList.addFirst(afterSrc);
                    if (i == 0) {
                        lastNode = beforeLinkedList.getFirst();
                    } else {
                        beforeLinkedList.addFirst(lastNode);
                    }
                    Object o = ((JSONObject) beforeJsonArr.get(i)).get(beforeKey);
                    analysisJson(o, afterJsonArr, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                }
            } else {
//                beforeLinkedList.addFirst(beforeSrc);
                for (int i = 0; i < afterJsonArr.size(); i++) {
                    beforeLinkedList.addFirst(beforeSrc);
                    if (i == 0) {
                        lastNode = afterLinkedList.getFirst();
                    } else {
                        afterLinkedList.addFirst(lastNode);
                    }

                    Object o = ((JSONObject) afterJsonArr.get(i)).get(afterKey);
                    analysisJson(beforeJsonArr, o, beforeLinkedList, afterLinkedList, beforeKey , afterKey, relEntity, resultList);
                }
            }
        }
    }


    /**
     * 获取排序的key
     * @param str 参数字符
     * @return 排序的key
     */
    private static String getSortKey(String str) {
        if (StringUtils.isNotBlank(str)) {
            String[] arr = str.split("#");
            if (arr.length > 1) {
                return arr[1];
            }
        }
        return null;
    }

    /**
     * 对象类型匹配比对
     */
    private static void objectTypeCompare(Object beforeObject, Object afterObject, RelEntity relEntity, List<String> resultList) {
        if (beforeObject instanceof BigInteger && afterObject instanceof BigInteger) {
            if (!beforeObject.equals(afterObject)) {
                resultList.add(relEntity.getPartyADesc() + "是[" + beforeObject + "]," + relEntity.getPartyBDesc() + "是[" + afterObject + "]");
            }
        } else if (beforeObject instanceof String && afterObject instanceof String) {
            if (!beforeObject.equals(afterObject)) {
                resultList.add(relEntity.getPartyADesc() + "是[" + beforeObject + "]," + relEntity.getPartyBDesc() + "是[" + afterObject + "]");
            }
        } else if (beforeObject instanceof BigDecimal && afterObject instanceof BigDecimal) {
            int big = ((BigDecimal) beforeObject).compareTo((BigDecimal) afterObject);
            if (big != 0) {
                resultList.add(relEntity.getPartyADesc() + "是[" + beforeObject + "]," + relEntity.getPartyBDesc() + "是[" + afterObject + "]");
            }
        } else {
            //未判断的类型，直接使用equals比较
            if (!beforeObject.equals(afterObject)) {
                resultList.add(relEntity.getPartyADesc() + "是[" + beforeObject + "]," + relEntity.getPartyBDesc() + "是[" + afterObject + "]");
            }
        }
    }

    private static LinkedList<String> arrTurnLinkedList(String[] strArr) {
        LinkedList<String> linkedList = new LinkedList<>();
        if (strArr == null || strArr.length == 0) {
            return linkedList;
        }
        for (String str : strArr) {
            linkedList.addLast(str);
        }
        return linkedList;
    }

    static class RelEntity {
        private String partyA;
        private String partyADesc;
        private String partyB;
        private String partyBDesc;

        public RelEntity(String partyA, String partyADesc, String partyB, String partyBDesc) {
            this.partyA = partyA;
            this.partyADesc = partyADesc;
            this.partyB = partyB;
            this.partyBDesc = partyBDesc;
        }

        public String getPartyA() {
            return partyA;
        }

        public void setPartyA(String partyA) {
            this.partyA = partyA;
        }

        public String getPartyADesc() {
            return partyADesc;
        }

        public void setPartyADesc(String partyADesc) {
            this.partyADesc = partyADesc;
        }

        public String getPartyB() {
            return partyB;
        }

        public void setPartyB(String partyB) {
            this.partyB = partyB;
        }

        public String getPartyBDesc() {
            return partyBDesc;
        }

        public void setPartyBDesc(String partyBDesc) {
            this.partyBDesc = partyBDesc;
        }
    }


    static class PartyA {
        private String a_id;
        private String a_age;
        private String a_name;

        public PartyA(String a_id, String a_age, String a_name) {
            this.a_id = a_id;
            this.a_age = a_age;
            this.a_name = a_name;
        }

        public String getA_id() {
            return a_id;
        }

        public void setA_id(String a_id) {
            this.a_id = a_id;
        }

        public String getA_age() {
            return a_age;
        }

        public void setA_age(String a_age) {
            this.a_age = a_age;
        }

        public String getA_name() {
            return a_name;
        }

        public void setA_name(String a_name) {
            this.a_name = a_name;
        }
    }

    static class PartyB {
        private String b_id;
        private String b_age;
        private String b_name;

        public PartyB(String b_id, String b_age, String b_name) {
            this.b_id = b_id;
            this.b_age = b_age;
            this.b_name = b_name;
        }

        public String getB_id() {
            return b_id;
        }

        public void setB_id(String b_id) {
            this.b_id = b_id;
        }

        public String getB_age() {
            return b_age;
        }

        public void setB_age(String b_age) {
            this.b_age = b_age;
        }

        public String getB_name() {
            return b_name;
        }

        public void setB_name(String b_name) {
            this.b_name = b_name;
        }
    }

    public static void main(String[] args) {
        RelEntity relEntity1 = new RelEntity("001001.line1.reportno", "a的名字", "001001.line1.reportno", "b的名字");
        RelEntity relEntity2 = new RelEntity("002006.line3.dataorg#orderno", "line3名称", "002006.line3.dataorg#orderno", "line3名称2");
        RelEntity relEntity3 = new RelEntity("002006.line3.dataorg", "名称", "002006.line1.employer", "数据");
        RelEntity relEntity4 = new RelEntity("002006.line3.dataorg#orderno", "名称", "002006.test.line1#name.dataorg#orderno", "数据");//#name


        String str1 = "{\"002006\":{\"line3\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line1\":[{\"orderno\":\"1\",\"employeraddress\":\"江苏省南京市玄武区孝陵卫街道顾家营公交场站（已详）\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"2\",\"employeraddress\":\"江苏省南京玄武区中央路258号江南大厦\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"3\",\"employeraddress\":\"江苏省南京市玄武区顾家营公交场厂站\",\"employer\":\"南京公交总公司\"},{\"orderno\":\"4\",\"employeraddress\":\"--\",\"employer\":\"南京市公共交通总公司\"},{\"orderno\":\"5\",\"employeraddress\":\"南京马群第四修理厂南京公交总公司第四修理厂\",\"employer\":\"南京公交总公司第四修理厂\"}]}}";

        String str2 = "{\"002006\":{\"line3\":[{\"orderno\":\"5\",\"dataorg\":\"广州银行hgt\"},{\"orderno\":\"4\",\"dataorg\":\"平安银行南京城中支行hgt\"},{\"orderno\":\"1\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"2\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"3\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line1\":[{\"orderno\":\"1\",\"employeraddress\":\"江苏省南京市玄武区孝陵卫街道顾家营公交场站（已详）\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"2\",\"employeraddress\":\"江苏省南京玄武区中央路258号江南大厦\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"3\",\"employeraddress\":\"江苏省南京市玄武区顾家营公交场厂站\",\"employer\":\"南京公交总公司\"},{\"orderno\":\"4\",\"employeraddress\":\"--\",\"employer\":\"南京市公共交通总公司\"},{\"orderno\":\"5\",\"employeraddress\":\"南京马群第四修理厂南京公交总公司第四修理厂\",\"employer\":\"南京公交总公司第四修理厂\"}]}}";

        String str3 = "{\"002006\":{\"line3\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}]}}";

        String str4 = "{\"002006\":{\"test\":[{\"name\":\"bbb\",\"line1\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行_bbb\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行_bbb\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行_bbb\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行_bbb\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部_bbb\"}],\"line2\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行_bbb_line2\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行_bbb_line2\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行_bbb_line2\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行_bbb_line2\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部_bbb_line2\"}]},{\"name\":\"aaa\",\"line1\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行_aaa\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行_aaa\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行_aaa\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行_aaa\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部_aaa\"}],\"line2\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行_aaa_line2\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行_aaa_line2\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行_aaa_line2\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行_aaa_line2\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部_aaa_line2\"}]}]}}";

        String str5 = "{\"002006\":{\"line3\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"}]}}";

        String str6 = "{\"002006\":{\"test\":[{\"name\":\"bbb\",\"line1\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行_bbb\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行_bbb\"}]},{\"name\":\"aaa\",\"line1\":[{\"orderno\":\"3\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行_aaa\"}]}]}}";


//        List<String> list = compareEntitys(str1, str2, Arrays.asList(relEntity2));
        List<String> list = compareEntitys(str5, str6, Arrays.asList(relEntity4));
        System.out.println("一共" + list.size() + "条差异！");
        for (String str : list) {
            System.out.println("结果:" + str);
        }

        //保存最后的key值就好

        /**
         * 1.实体长度不一致
         * 2.数组中包含排序的数据
         * 3.数组长度不一致的数据
         */

    }

}
