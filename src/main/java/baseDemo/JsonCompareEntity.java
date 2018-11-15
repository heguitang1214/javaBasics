package baseDemo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import utils.json.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * 利用Json比较实体的内容
 */
public class JsonCompareEntity {

    /**
     * 比较json内容，提供给对外调用
     *
     * @param beforeJson  json1
     * @param afterJson   json2
     * @param relEntities 对应字段映射关系
     * @return 返回List描述结果
     */
    private static List<String> compareEntitys(String beforeJson, String afterJson, List<RelEntity> relEntities) {
        //返回值的容器
        List<String> resultList = new ArrayList<>();
        if (relEntities == null || relEntities.size() == 0) {
            return resultList;
        }
        JSONObject beforeObject;
        JSONObject afterObject;
        try {
            beforeObject = JSONArray.parseObject(beforeJson);
            afterObject = JSONArray.parseObject(afterJson);
        } catch (Exception e) {
            System.out.println("JSON数据格式异常，转换失败！");
            resultList.add("JSON数据格式异常，转换失败");
            return resultList;
        }
        try {
            entityContrast(beforeObject, afterObject, relEntities, resultList);
        } catch (Exception e) {
            e.printStackTrace();
            resultList.clear();
            resultList.add("JSON数据对比出现未知异常");
            return resultList;
        }
        return resultList;
    }

    /**
     * 对象的对比方法
     *
     * @param beforeSrc   对象1
     * @param afterSrc    对象2
     * @param relEntities 映射关系
     * @param resultList  返回容器
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
            beforeLinkedList.removeFirst();
        }
        if (afterLinkedList.size() > 0) {
            afterKey = afterLinkedList.getFirst();
            afterLinkedList.removeFirst();
        }
        if (beforeData == null || afterData == null) {
            if (beforeData instanceof JSONObject) {
                beforeData = ((JSONObject) beforeData).get(beforeKey);
            }
            if (afterData instanceof JSONObject){
                afterData = ((JSONObject) afterData).get(afterKey);
            }
//            System.out.println(relEntity.getPartyADesc() + "是[" + beforeData + "]," + relEntity.getPartyBDesc() + "是[" + afterData + "]");
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
//                对象和数组的对比
                analysisJson(beforeObject, afterJsonArr, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
            } else {
//                System.out.println("类型匹配失败:" + relEntity.getPartyADesc() + "属于JSONObject类型，" + relEntity.getPartyBDesc() + "未匹配到合适的类型,既不是JSONObject,也不是JSONArray,它的值是:" + afterData);
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
                Object afterObject = ((JSONObject) afterData).get(afterKey);
                analysisJson(beforeData, afterObject, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
            } else {
//                System.out.println("类型匹配失败:" + relEntity.getPartyADesc() + "属于JSONObject类型，" + relEntity.getPartyBDesc() + "未匹配到合适的类型,既不是JSONObject,也不是JSONArray,它的值是:" + afterData);
                resultList.add("类型匹配失败:" + relEntity.getPartyADesc() + "属于JSONObject类型，" + relEntity.getPartyBDesc() + "未匹配到合适的类型,既不是JSONObject,也不是JSONArray,它的值是:" + afterData);
            }
        } else {
//            System.out.println("类型匹配失败:" + relEntity.getPartyADesc() + "既不是一个对象,也不是一个数组!其内容是:" + beforeData);
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
                    System.out.println(beforeJsonArr);
//                    System.out.println("通过["+beforeKey+"]获取值,为" + o); //todo line3_2的问题
                    if (o != null) {
                        analysisJson(o, afterJsonArr, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                    }
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
                    if (o != null) {
                        analysisJson(beforeJsonArr, o, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                    }
                }
            }
        }
    }

    private static void jsonArrayHandle1(JSONArray beforeJsonArr, JSONArray afterJsonArr,
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
                    analysisJson(beforeJsonArr, o, beforeLinkedList, afterLinkedList, beforeKey, afterKey, relEntity, resultList);
                }
            }
        }
    }


    /**
     * 获取排序的key
     *
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



    public static void main(String[] args) {
        //大道数据模拟
        String str1 = "{\"icrCreditDTO\":{\"reportBaseInfo\":{\"reportNo\":\"2015061000001934756505\",\"name\":\"张李五\",\"reportCreateTime\":\"2015/06/11 02:38:41\"},\"line1\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"}],\"line2\":[{\"orderno\":\"3\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"1\",\"dataorg\":\"华夏银行\"}],\"line3\":[{\"name\":\"300\",\"age\":\"50\",\"line3_1\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行_hgt\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"}]},{\"name\":\"100\",\"age\":\"60\",\"line3_2\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"}]}]}}";
        //半刻数据结构
        String str2 = "{\"001001\":{\"reportNo\":\"2015061000001934756505_hgt\",\"name\":\"张李五_hgt\",\"queryTime\":\"2015/06/11 02:38:40\",\"line1\":[{\"orderno\":\"3\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"1\",\"dataorg\":\"华夏银行\"}],\"line2\":{\"line2_1\":[{\"orderno\":\"31\",\"dataorg\":\"广州银行_hgt\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"1\",\"dataorg\":\"华夏银行\"}]},\"line3\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"}]}}";

        //映射关系
        //1.正常属性对应
        RelEntity relEntity1 = new RelEntity("icrCreditDTO.reportBaseInfo.reportNo", "大道报告号:", "001001.reportNo", "半刻报告号:");
        //2.深度不一致属性数据对应
        RelEntity relEntity2 = new RelEntity("icrCreditDTO.reportBaseInfo.name", "大道用户名:", "001001.name", "半刻用户名:");
        //3.通过数组的顺序来判断,  用广州银行来和华夏银行作比较, 用华夏银行和广州银行做比较
        RelEntity relEntity3 = new RelEntity("icrCreditDTO.line1.dataorg#orderno", "大道银行名:", "001001.line1.dataorg#orderno", "半刻银行名:");
        //4.相同结构数组对比,要求字段排序
        RelEntity relEntity4 = new RelEntity("icrCreditDTO.line2.dataorg#orderno", "大道银行名2:", "001001.line2.line2_1.dataorg#orderno", "半刻银行名2:");
        RelEntity relEntity5 = new RelEntity("icrCreditDTO.line3.line3_1.dataorg#orderno", "大道银行名3:", "001001.line3.dataorg#orderno", "半刻银行名3:");



        //5.不同数组深度对比
        //6.不同数组深度对比,要求字段排序
        long start = System.currentTimeMillis();
        List<String> list = compareEntitys(str1, str2, Arrays.asList(relEntity5));
        System.out.println("耗时:" + (System.currentTimeMillis() - start));
        System.out.println("一共" + list.size() + "条差异！");
        for (String str : list) {
            System.out.println("结果:" + str);
        }





//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
//        LocalDate localDate = LocalDate.parse("2018/11/13", formatter);
//        System.out.println(localDate);


//        String str = "1986-04-08 12:30";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);



    }

}
