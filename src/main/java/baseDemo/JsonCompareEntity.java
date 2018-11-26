package baseDemo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ReflectionsUtils;
import java.util.*;


/**
 * 利用Json比较实体的内容
 */
public class JsonCompareEntity {

    private static Logger logger = LoggerFactory.getLogger(ReflectionsUtils.class);

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
            String[] beforeStr = relEntity.getPartyA().split("#");
            String[] afterStr = relEntity.getPartyB().split("#");
            LinkedList<String> beforeList = strTurnLinkedList(beforeStr[0]);
            LinkedList<String> afterList = strTurnLinkedList(afterStr[0]);
            LinkedList<String> beforeSort = new LinkedList<>();
            LinkedList<String> afterSort = new LinkedList<>();
            if (beforeStr.length > 1) {
                beforeSort = strTurnLinkedList(beforeStr[1]);
            }
            if (afterStr.length > 1) {
                afterSort = strTurnLinkedList(afterStr[1]);
            }
            analysisJson(beforeSrc, afterSrc,
                    beforeList, afterList, beforeSort, afterSort, relEntity, resultList);
        }
    }

    /**
     * 获取数据
     *
     * @param object     数据源
     * @param linkedList 路径
     * @param isSort     是否排序
     * @return 获取的数据
     */
    //todo 在isSort为true的条件下，不能保证排序链条的顺序没有发生改变
    private static Object getDataByLinkedList(Object object, LinkedList<String> linkedList, boolean isSort) {
        if (linkedList == null || linkedList.size() == 0) {
            return object;
        }
        String key = linkedList.getFirst();
        if (object instanceof JSONObject) {
            Object obj = ((JSONObject) object).get(key);
            if (isSort) {
                if (linkedList.size() > 1) {
                    linkedList.addLast(linkedList.removeFirst());
                }
            } else {
                linkedList.removeFirst();
            }
            object = getDataByLinkedList(obj, linkedList, isSort);
        } else if (object instanceof JSONArray) {
            JSONArray array = ((JSONArray) object);
            if (array.size() == 1) {
                Object obj = array.get(0);
                Object o = ((JSONObject) obj).get(key);
                linkedList.addLast(linkedList.removeFirst());
                object = getDataByLinkedList(o, linkedList, isSort);
            } else {
                return object;
            }
        }
        return object;
    }

    /**
     * JSON数据比较
     */
    private static void analysisJson(Object beforeData, Object afterData,
                                     LinkedList<String> beforeLinkedList, LinkedList<String> afterLinkedList,
                                     LinkedList<String> beforeSort, LinkedList<String> afterSort,
                                     RelEntity relEntity, List<String> resultList) {

        Object beforeObj = getDataByLinkedList(beforeData, beforeLinkedList, false);
        Object afterObj = getDataByLinkedList(afterData, afterLinkedList, false);
        if (beforeObj instanceof JSONArray) {
            for (int i = 0; i < ((JSONArray) beforeObj).size(); i++) {
                Object beforeObject = ((JSONArray) beforeObj).get(i);
                Object o1 = getDataByLinkedList(beforeObject, beforeLinkedList, true);
//                1.前一个是数组，后一个也是数组
                if (afterObj instanceof JSONArray) {
//                    看是否有排序字段
                    Object sort1 = getDataByLinkedList(beforeObject, beforeSort, true);
                    Object obj = null;
                    for (int j = 0; j < ((JSONArray) afterObj).size(); j++) {
                        Object afterObject = ((JSONArray) afterObj).get(j);
                        Object sort2 = getDataByLinkedList(afterObject, afterSort, true);
//                        sort2 = getNumberByRegd(sort2.toString());//大道字段处理
//                        sort2 = getNumber(sort2.toString());//大道字段处理
                        if (sort1 != null && sort2 != null) {
                            if (sort1.equals(sort2)) {
                                obj = afterObject;
                            }
                        } else {
                            if (i == j) {
                                obj = ((JSONArray) beforeObj).get(i);
                            }
                        }
                    }
                    if (obj == null) {
                        objectTypeCompare(sort1 == null ? null : sort1.toString(), o1, null, relEntity, resultList);
                    } else {
                        Object o = getDataByLinkedList(obj, afterLinkedList, true);
                        objectTypeCompare(sort1 == null ? null : sort1.toString(), o1, o, relEntity, resultList);
                    }
                } else {
//                    2.前一个是数组，后一个数对象
                    objectTypeCompare(null, o1, afterObj, relEntity, resultList);
                }
            }
            return;
        } else if (afterObj instanceof JSONArray) {
//            3.前一个是对象，后一个是集合，直接循环比较
            for (int i = 0; i < ((JSONArray) afterObj).size(); i++) {
                Object afterObject = ((JSONArray) afterObj).get(i);
                Object o1 = getDataByLinkedList(afterObject, beforeLinkedList, true);
                objectTypeCompare(null, beforeObj, o1, relEntity, resultList);
            }
            return;
        }
//        4.前一个和后一个都是对象
        objectTypeCompare(null, beforeObj, afterObj, relEntity, resultList);
    }


    /**
     * 对象类型匹配比对
     */
    private static void objectTypeCompare(String number, Object beforeObject, Object afterObject,
                                          RelEntity relEntity, List<String> resultList) {
        if (org.apache.commons.lang3.StringUtils.isBlank(number)){
            number = relEntity.getPartyBDesc().split("\\.")[0];
        }else {
            number = relEntity.getPartyBDesc().split("\\.")[0] + number;
        }
        //去掉空值的判断
        if (beforeObject == null) {
            if (afterObject == null || "".equals(afterObject.toString())) {
                return;
            } else if (afterObject instanceof JSONArray) {
                if (((JSONArray) afterObject).size() == 0) {
                    return;
                }
            }
            resultList.add(number + "\t" + relEntity.getPartyADesc() + "\t" + null + "\t" + relEntity.getPartyBDesc() + "\t" + afterObject);
            return;
        }
        if (afterObject == null) {
            if ("".equals(beforeObject.toString())) {
                return;
            } else if (beforeObject instanceof JSONArray) {
                if (((JSONArray) beforeObject).size() == 0) {
                    return;
                }
            }
            resultList.add(number + "\t" + relEntity.getPartyADesc() + "\t" + beforeObject + "\t" + relEntity.getPartyBDesc() + "\t" + null);
            return;
        }
//        时间的处理
        String regex_yM = "[1-9]{1}[0-9]{3}([-])\\d{1,2}";
        String regex_yM_ = "[1-9]{1}[0-9]{3}([./])\\d{1,2}";
        String regex_yMd = "[1-9]{1}[0-9]{3}([-])\\d{1,2}\\1\\d{1,2}";
        String regex_yMd_ = "[1-9]{1}[0-9]{3}([./])\\d{1,2}\\1\\d{1,2}";
        String regex_yMdHms = "[1-9]{1}[0-9]{3}([-])\\d{1,2}\\1\\d{1,2}\\s?\\d{1,2}[:]\\d{1,2}[:]\\d{1,2}";
        String regex_yMdHms_ = "[1-9]{1}[0-9]{3}([./])\\d{1,2}\\1\\d{1,2}\\s?\\d{1,2}[:]\\d{1,2}[:]\\d{1,2}";
        if ((beforeObject.toString().matches(regex_yM) && afterObject.toString().matches(regex_yM_) ||
                (beforeObject.toString().matches(regex_yMd) && afterObject.toString().matches(regex_yMd_)) ||
                (beforeObject.toString().matches(regex_yMdHms) && afterObject.toString().matches(regex_yMdHms_)))) {
            afterObject = afterObject.toString().replaceAll("[./]", "-");
        }

        if (!beforeObject.toString().equals(afterObject.toString())) {
            resultList.add(number + "\t" + relEntity.getPartyADesc() + "\t" + beforeObject + "\t" + relEntity.getPartyBDesc() + "\t" + afterObject);
        }
    }

    /**
     * 将字符串转为LinkedList，根据.拆分
     */
    private static LinkedList<String> strTurnLinkedList(String str) {
        LinkedList<String> linkedList = new LinkedList<>();
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return linkedList;
        }
        String[] strArr = str.split("\\.");
        for (String arr : strArr) {
            linkedList.addLast(arr);
        }
        return linkedList;
    }


    static class RelEntity {
        private String partyA;
        private String partyADesc;
        private String partyB;
        private String partyBDesc;

        RelEntity(String partyA, String partyADesc, String partyB, String partyBDesc) {
            this.partyA = partyA;
            this.partyADesc = partyADesc;
            this.partyB = partyB;
            this.partyBDesc = partyBDesc;
        }

        String getPartyA() {
            return partyA;
        }

        String getPartyADesc() {
            return partyADesc;
        }


        String getPartyB() {
            return partyB;
        }


        String getPartyBDesc() {
            return partyBDesc;
        }

    }

    public static void main(String[] args) {



    }

}
