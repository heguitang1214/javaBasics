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
     * 比较json内容
     *
     * @param beforeJson  json1
     * @param afterJson   json1
     * @param relEntities 对应字段映射关系,建立字段的映射关系即可
     */
    private static List<String> compareEntitys(String beforeJson, String afterJson, List<RelEntity> relEntities) {
        //返回值的容器
        List<String> resultList = new ArrayList<>();
        JSONObject beforeObject = JSONArray.parseObject(beforeJson);
        JSONObject afterObject = JSONArray.parseObject(afterJson);
        entityContrast(beforeObject, afterObject, relEntities, resultList);
        return resultList;
    }

    private static void entityContrast(Object beforeSrc, Object afterSrc, List<RelEntity> relEntities, List<String> resultList) {
        //映射关系字段
        for (RelEntity relEntity : relEntities) {
            String[] beforeArr = relEntity.getPartyA().split("\\.");
            String[] afterArr = relEntity.getPartyB().split("\\.");
            LinkedList<String> beforeList = arrTurnLinkedList(beforeArr);
            LinkedList<String> afterList = arrTurnLinkedList(afterArr);
            analysisJson(beforeSrc, afterSrc, beforeList, afterList, relEntity, resultList);
        }
    }


    /**
     * 解析JSON内容,进行数据的比较
     */
    private static void analysisJson(Object beforeData, Object afterData,
                                     LinkedList<String> beforeLinkedList, LinkedList<String> afterLinkedList,
                                     RelEntity relEntity, List<String> resultList) {
        String beforeKey = "", afterKey = "";
        if (beforeLinkedList.size() > 0) {
            beforeKey = beforeLinkedList.getFirst();
        }
        if (afterLinkedList.size() > 0) {
            afterKey = afterLinkedList.getFirst();
        }
        //对象存在空值
        if (beforeData == null || afterData == null) {
            resultList.add(relEntity.getPartyADesc() + "是[" + beforeData + "]," + relEntity.getPartyBDesc() + "是[" + afterData + "]");
        }
        if (beforeLinkedList.size() > 0) {
            beforeLinkedList.removeFirst();
        }
        if (afterLinkedList.size() > 0) {
            afterLinkedList.removeFirst();
        }
        if (beforeData instanceof JSONObject) {
            if (afterData instanceof JSONObject) {
//                if (beforeLinkedList.size() > 0) {
//                    beforeLinkedList.removeFirst();
//                }
//                if (afterLinkedList.size() > 0) {
//                    afterLinkedList.removeFirst();
//                }
                if (beforeLinkedList.size() == 0 && afterLinkedList.size() == 0) {
                    Object beforeObject = ((JSONObject) beforeData).get(beforeKey);
                    Object afterObject = ((JSONObject) afterData).get(afterKey);
                    //对象类型的比较
                    objectTypeCompare(beforeObject, afterObject, relEntity, resultList);
                } else if (beforeLinkedList.size() == 0 && afterLinkedList.size() > 0) {
                    Object afterObject = ((JSONObject) afterData).get(afterKey);
                    analysisJson(beforeData, afterObject, beforeLinkedList, afterLinkedList, relEntity, resultList);
                } else if (beforeLinkedList.size() > 0 && afterLinkedList.size() == 0) {
                    Object beforeObject = ((JSONObject) beforeData).get(beforeKey);
                    analysisJson(beforeObject, afterData, beforeLinkedList, afterLinkedList, relEntity, resultList);
                } else {
                    Object beforeObject = ((JSONObject) beforeData).get(beforeKey);
                    Object afterObject = ((JSONObject) afterData).get(afterKey);
                    analysisJson(beforeObject, afterObject, beforeLinkedList, afterLinkedList, relEntity, resultList);
                }
            } else {
                resultList.add("类型不匹配:" + relEntity.getPartyADesc() + "是[JSONObject]," + relEntity.getPartyBDesc() + "非[JSONObject]");
            }
        } else if (beforeData instanceof JSONArray) {
            if (afterData instanceof JSONArray) {
                JSONArray beforeJsonArr = (JSONArray) beforeData;
                JSONArray afterJsonArr = (JSONArray) afterData;
                //todo 可以优化
//                beforeLinkedList.removeFirst();
//                afterLinkedList.removeFirst();

                String materSortKey = null, slaveSortKey = null;
                if (beforeKey.contains("#")) {
                    materSortKey = getSortKey(beforeKey, "#");
                    beforeKey = beforeKey.split("#")[0];
                }
                if (afterKey.contains("#")) {
                    slaveSortKey = getSortKey(afterKey, "#");
                    afterKey = afterKey.split("#")[0];
                }




                if (afterJsonArr.size() > beforeJsonArr.size()) {
                    jsonArrayHandle(afterJsonArr, beforeJsonArr, beforeLinkedList, afterLinkedList,
                            beforeKey, afterKey, relEntity, resultList, materSortKey, slaveSortKey);
                } else {
                    jsonArrayHandle(beforeJsonArr, afterJsonArr, beforeLinkedList, afterLinkedList,
                            beforeKey, afterKey, relEntity, resultList, materSortKey, slaveSortKey);
                }
            } else {
                resultList.add("类型不匹配:" + relEntity.getPartyADesc() + "是[JSONArray]," + relEntity.getPartyBDesc() + "非[JSONArray]");
            }
        } else {
            resultList.add("类型匹配失败:" + relEntity.getPartyADesc() + "既不是一个对象,也不是一个数组!其内容是:" + beforeData);
        }
    }


    /**
     * 获取排序的key
     *
     * @param str            参数字符
     * @param splitCharacter 拆分符号
     * @return 排序的key
     */
    private static String getSortKey(String str, String splitCharacter) {
        if (StringUtils.isNotBlank(str)) {
            String[] arr = str.split("#");
            if (arr.length > 1) {
                return arr[1];
            }
        }
        return null;
    }

    private static void handleSortKey(String key, String sortKey, String strKey) {
        if (StringUtils.isNotBlank(strKey)) {
            if (strKey.contains("#")){
                String[] arr = strKey.split("#");
                key = arr[0];
                sortKey = arr[1];
            }else {
                key = strKey;
            }
        }
    }


    /**
     * JsonArray类型的处理
     * todo 处理集合嵌套的问题
     */
    private static void jsonArrayHandle(JSONArray materJsonArr, JSONArray slaveJsonArr,
                                        LinkedList<String> beforeLinkedList, LinkedList<String> afterLinkedList,
                                        String beforeKey, String afterKey, RelEntity relEntity, List<String> resultList,
                                        String materSortKey, String slaveSortKey) {
        if (StringUtils.isNotBlank(materSortKey) && StringUtils.isNotBlank(slaveSortKey)) {
            for (Object beforeObject : materJsonArr) {
                beforeLinkedList.addFirst(beforeKey);
                afterLinkedList.addFirst(afterKey);
                Object afterObject = null;
                String masterKey, slaveKey;
                if (beforeObject instanceof JSONObject) {
                    masterKey = ((JSONObject) beforeObject).getString(materSortKey);
                } else {
                    resultList.add("数组比对失败,获取类型不成功,不是一个JSONObject类型!");
                    continue;
                }
                for (Object slaveObject : slaveJsonArr) {
                    //标记
                    if (slaveObject instanceof JSONObject) {
                        slaveKey = ((JSONObject) slaveObject).getString(slaveSortKey);
                    } else {
                        resultList.add("数组比对失败,获取类型不成功,不是一个JSONObject类型!");
                        continue;
                    }
                    if (masterKey.equals(slaveKey)) {
                        afterObject = slaveObject;
                    }
                }
                analysisJson(beforeObject, afterObject, beforeLinkedList, afterLinkedList, relEntity, resultList);
            }
        } else {
            for (int i = 0; i < materJsonArr.size(); i++) {
                beforeLinkedList.addFirst(beforeKey);
                afterLinkedList.addFirst(afterKey);
                Object masterObject = materJsonArr.get(i);
                Object beforeObject = null;
                if (i < slaveJsonArr.size()) {
                    beforeObject = slaveJsonArr.get(i);
                }
                analysisJson(masterObject, beforeObject, beforeLinkedList, afterLinkedList, relEntity, resultList);
            }
        }
    }


    /**
     * 对象类型匹配比对
     */
    private static void objectTypeCompare(Object beforeObject, Object afterObject, RelEntity relEntity, List<String> resultList) {
        if (beforeObject instanceof BigInteger && afterObject instanceof BigInteger) {
            System.out.println("BigInteger" + beforeObject);
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
            resultList.add(relEntity.getPartyADesc() + "是[" + beforeObject + "]," + relEntity.getPartyBDesc() + "是[" + afterObject + "]");
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

    private static LinkedList<String> arrTurnLinkedList2(String[] strArr) {
        LinkedList<String> linkedList = new LinkedList<>();
        if (strArr == null || strArr.length == 0) {
            return linkedList;
        }
        for (String str : strArr) {
            String[] arr = str.split("\\.");
            for (String s : arr) {
                linkedList.addLast(s);
            }
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
        RelEntity relEntity4 = new RelEntity("002006.line3.dataorg#orderno", "名称", "002006.test.line1.dataorg#orderno", "数据");


        String str1 = "{\"002006\":{\"line3\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line1\":[{\"orderno\":\"1\",\"employeraddress\":\"江苏省南京市玄武区孝陵卫街道顾家营公交场站（已详）\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"2\",\"employeraddress\":\"江苏省南京玄武区中央路258号江南大厦\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"3\",\"employeraddress\":\"江苏省南京市玄武区顾家营公交场厂站\",\"employer\":\"南京公交总公司\"},{\"orderno\":\"4\",\"employeraddress\":\"--\",\"employer\":\"南京市公共交通总公司\"},{\"orderno\":\"5\",\"employeraddress\":\"南京马群第四修理厂南京公交总公司第四修理厂\",\"employer\":\"南京公交总公司第四修理厂\"}]}}";

        String str2 = "{\"002006\":{\"line3\":[{\"orderno\":\"5\",\"dataorg\":\"广州银行hgt\"},{\"orderno\":\"4\",\"dataorg\":\"平安银行南京城中支行hgt\"},{\"orderno\":\"1\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"2\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"3\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line1\":[{\"orderno\":\"1\",\"employeraddress\":\"江苏省南京市玄武区孝陵卫街道顾家营公交场站（已详）\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"2\",\"employeraddress\":\"江苏省南京玄武区中央路258号江南大厦\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"3\",\"employeraddress\":\"江苏省南京市玄武区顾家营公交场厂站\",\"employer\":\"南京公交总公司\"},{\"orderno\":\"4\",\"employeraddress\":\"--\",\"employer\":\"南京市公共交通总公司\"},{\"orderno\":\"5\",\"employeraddress\":\"南京马群第四修理厂南京公交总公司第四修理厂\",\"employer\":\"南京公交总公司第四修理厂\"}]}}";

        String str3 = "{\"002006\":{\"line3\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}]}}";

        String str4 = "{\"002006\":{\"test\":[{\"line1\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行VIP\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line2\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}]}]}}";

//        List<String> list = compareEntitys(str1, str2, Arrays.asList(relEntity2));
        List<String> list = compareEntitys(str3, str4, Arrays.asList(relEntity4));
        for (String str : list) {
            System.out.println("结果:" + str);
        }


        /**
         * 1.实体长度不一致
         * 2.数组中包含排序的数据
         * 3.数组长度不一致的数据
         */

    }

}
