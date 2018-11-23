package baseDemo.ListCombination;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ReflectionsUtils;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * 利用Json比较实体的内容
 */
public class JsonCompareEntity2 {

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
        }else if (object instanceof JSONArray){
            JSONArray array = ((JSONArray)object);
            if (array.size() == 1){
                Object obj = array.get(0);
                Object o = ((JSONObject) obj).get(key);
                object = getDataByLinkedList(o, linkedList, isSort);
            }else {
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
            for (int i = 0; i < ((JSONArray) afterObj).size(); i++){
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
        //去掉空值的判断
        if (beforeObject == null){
            if (afterObject == null || "".equals(afterObject.toString())){
                return;
            }
            else if (afterObject instanceof JSONArray){
                if (((JSONArray) afterObject).size() == 0){
                    return;
                }
            }
            resultList.add(number + "\t" + relEntity.getPartyADesc() + "\t" + null + "\t" + relEntity.getPartyBDesc() + "\t" + afterObject);
            return;
        }
        if (afterObject == null){
            if ("".equals(beforeObject.toString())){
                return;
            }else if (beforeObject instanceof JSONArray){
                if (((JSONArray) beforeObject).size() == 0){
                    return;
                }
            }
            resultList.add(number + "\t" + relEntity.getPartyADesc() + "\t" + beforeObject + "\t" + relEntity.getPartyBDesc() + "\t" + null);
            return;
        }
        if (!beforeObject.equals(afterObject)) {
            resultList.add(number + "\t" + relEntity.getPartyADesc() + "\t" + beforeObject + "\t" + relEntity.getPartyBDesc() + "\t" + afterObject);
        }
    }


    /**
     * String regex = "[1-9]{1}[0-9]{3}([-./])\\d{1,2}\\1\\d{1,2}\\s?\\d{1,2}[:]\\d{1,2}[:]\\d{1,2}";
     * 2018-08 2015-03-27   2018-10-11 11:54:00
     * 2016/10 2018/10/11  2018/10/11 11:54:00
     */
    private void handleTime(String time1, String time2) {
        String regex_yM = "[1-9]{1}[0-9]{3}([-])\\d{1,2}";
        String regex_yM_ = "[1-9]{1}[0-9]{3}([/])\\d{1,2}";

        String regex_yMd = "[1-9]{1}[0-9]{3}([-])\\d{1,2}";
        String regex_yMd_ = "[1-9]{1}[0-9]{3}([/])\\d{1,2}";

        String regex_yMdHms = "[1-9]{1}[0-9]{3}([-])\\d{1,2}";
        String regex_yMdHms_ = "[1-9]{1}[0-9]{3}([/])\\d{1,2}";

        if (time1.matches(regex_yM) && time2.matches(regex_yM_)) {
            LocalDateTime localDateTime1 =
                    LocalDateTime.parse(time1, DateTimeFormatter.ofPattern("yyyy-MM"));

            LocalDateTime localDateTime2 =
                    LocalDateTime.parse(time1, DateTimeFormatter.ofPattern("yyyy-MM"));

        } else if (time1.matches(regex_yMd) && time2.matches(regex_yMd_)) {
            System.out.println("===========");


        } else if (time1.matches(regex_yMdHms) && time2.matches(regex_yMdHms_)) {

        } else {

        }
        LocalDateTime localDateTime1 =
                LocalDateTime.parse(time1, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

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


    public static void main(String[] args) throws ParseException {
        //大道数据模拟
        String str1 = "";
        //半刻数据结构
        String str2 = "";

        //半刻
        String str3 = "{\"002006\":{\"line1\":[{\"orderno\":\"1\",\"employeraddress\":\"--\",\"employer\":\"佛山康辉国际旅行社\"},{\"orderno\":\"2\",\"employeraddress\":\"佛山市禅城区佛山大道北171号5楼\",\"employer\":\"佛山康辉国际旅行社有限公司\"},{\"orderno\":\"3\",\"employeraddress\":\"广东省佛山市南海区桂城佛平三路紫金城B1001佛山市康辉国际旅行\",\"employer\":\"佛山市康辉国际旅行社有限公司\"},{\"orderno\":\"4\",\"employeraddress\":\"广东省佛山市禅城佛山大道北171号（旅游大厦旁）五楼\",\"employer\":\"佛山康辉国际旅行社有限公司业务二部\"},{\"orderno\":\"5\",\"employeraddress\":\"--\",\"employer\":\"佛山市康辉国际旅行社有限公司销售\"}]},\"002007\":{\"line2\":[{\"orderno\":\"1\",\"dataorg\":\"深圳前海微众银行股份有限公司\"},{\"orderno\":\"2\",\"dataorg\":\"浦发银行信用卡中心\"},{\"orderno\":\"3\",\"dataorg\":\"广州农村商业银行\"},{\"orderno\":\"4\",\"dataorg\":\"中国农业银行\"},{\"orderno\":\"5\",\"dataorg\":\"中信银行\"}],\"line1\":[{\"orderno\":\"1\",\"occupation\":\"--\",\"gettime\":\"2017-06-12\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"--\"},{\"orderno\":\"2\",\"occupation\":\"办事人员和有关人员\",\"gettime\":\"2016-07-04\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"居民服务和其他服务业\",\"title\":\"--\"},{\"orderno\":\"3\",\"occupation\":\"--\",\"gettime\":\"2016-04-16\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"文化、体育和娱乐业\",\"title\":\"--\"},{\"orderno\":\"4\",\"occupation\":\"商业、服务业人员\",\"gettime\":\"2015-03-27\",\"duty\":\"--\",\"startyear\":\"2013\",\"industry\":\"居民服务和其他服务业\",\"title\":\"高级\"},{\"orderno\":\"5\",\"occupation\":\"--\",\"gettime\":\"2014-12-02\",\"duty\":\"--\",\"startyear\":\"--\",\"industry\":\"租赁和商务服务业\",\"title\":\"--\"}]},\"002008\":{\"line2\":{\"postaddressorg\":\"深圳前海微众银行股份有限公司\",\"registeredaddressorg\":\"深圳前海微众银行股份有限公司\"},\"line1\":{\"postaddress\":\"广东佛山禅城区佛山大道北171号2楼\",\"registeredaddress\":\"广西壮族自治区贵港市桂平市蒙圩镇林村寻哒\"}},\"001001\":{\"line1\":{\"reportcreatetime\":\"2018-10-11 11:54:00\",\"reportno\":\"2018101100006273089534\",\"querytime\":\"2018-10-11 11:54:00\"}},\"002002\":{\"line1\":{\"certno\":\"--\",\"certtype\":\"--\",\"name\":\"--\"}},\"003003\":{\"line1\":{\"balance\":\"20524\",\"accountcount\":\"3\",\"financeorgcount\":\"2\",\"latest6monthusedavgamount\":\"2062\",\"creditlimit\":\"24500\",\"financecorpcount\":\"2\"}},\"002003\":{\"line1\":{\"telephoneno\":\"--\",\"employer\":\"--\"}},\"003004\":{\"line1\":{\"count2\":\"2\",\"highestoverdueamountpermon3\":\"--\",\"months\":\"0\",\"highestoverdueamountpermon\":\"--\",\"maxduration\":\"0\",\"months3\":\"0\",\"count3\":\"0\",\"maxduration3\":\"0\",\"maxduration2\":\"1\",\"count\":\"0\",\"months2\":\"2\",\"highestoverdueamountpermon2\":\"--\"}},\"004004\":[{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"N1NNNNNNNNNNNNNNNN******\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-09-05\",\"recentpaydate\":\"2018-02-23\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"13693\"},\"line1\":{\"financeorg\":\"广发银行佛山分行\",\"orderno\":\"1\",\"creditlimitamount\":\"18000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"48284\",\"opendate\":\"2012-12-26\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-09-05\",\"account\":\"1021627472000001SMC002156061\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNN**NNNNNNNN1NNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-08-26\",\"recentpaydate\":\"2018-08-17\",\"actualpaymentamount\":\"698\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"6991\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"6469\",\"scheduledpaymentamount\":\"698\",\"usedhighestamount\":\"6991\"},\"line1\":{\"financeorg\":\"交通银行\",\"orderno\":\"2\",\"creditlimitamount\":\"7000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"7000\",\"opendate\":\"2016-06-28\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-08-26\",\"account\":\"3710011130120048797\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-08-27\",\"recentpaydate\":\"2018-08-14\",\"actualpaymentamount\":\"2568\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"5144\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"11271\",\"scheduledpaymentamount\":\"2568\",\"usedhighestamount\":\"17842\"},\"line1\":{\"financeorg\":\"中国民生银行\",\"orderno\":\"3\",\"creditlimitamount\":\"18000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"18000\",\"opendate\":\"2014-07-25\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-08-27\",\"account\":\"15603050014398161\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-09-17\",\"recentpaydate\":\"2018-09-03\",\"actualpaymentamount\":\"396\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"7649\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"7980\",\"scheduledpaymentamount\":\"395\",\"usedhighestamount\":\"8544\"},\"line1\":{\"financeorg\":\"中信银行\",\"orderno\":\"4\",\"creditlimitamount\":\"8500\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"8500\",\"opendate\":\"2014-12-02\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-09-17\",\"account\":\"9559123388923566936\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"************************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-08-07\",\"recentpaydate\":\"2016-07-04\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"0\"},\"line1\":{\"financeorg\":\"浦发银行信用卡中心\",\"orderno\":\"5\",\"creditlimitamount\":\"9000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2015-01-20\",\"currency\":\"美元账户\",\"state\":\"\",\"stateenddate\":\"2018-08-07\",\"account\":\"84003100013799646\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-09-07\",\"recentpaydate\":\"2018-08-18\",\"actualpaymentamount\":\"555\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"9058\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"8592\",\"scheduledpaymentamount\":\"555\",\"usedhighestamount\":\"9058\"},\"line1\":{\"financeorg\":\"浦发银行信用卡中心\",\"orderno\":\"6\",\"creditlimitamount\":\"9000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"9000\",\"opendate\":\"2015-01-20\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-09-07\",\"account\":\"15603100013799646\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NN*NNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-09-07\",\"recentpaydate\":\"2018-08-18\",\"actualpaymentamount\":\"2162\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"9020\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"9964\",\"scheduledpaymentamount\":\"2162\",\"usedhighestamount\":\"19304\"},\"line1\":{\"financeorg\":\"中国农业银行\",\"orderno\":\"7\",\"creditlimitamount\":\"9000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"9000\",\"opendate\":\"2015-03-27\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-09-07\",\"account\":\"44425701660001384\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-08-20\",\"recentpaydate\":\"2018-07-28\",\"actualpaymentamount\":\"3679\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"31261\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"24571\",\"scheduledpaymentamount\":\"3679\",\"usedhighestamount\":\"31261\"},\"line1\":{\"financeorg\":\"平安银行信用卡中心\",\"orderno\":\"8\",\"creditlimitamount\":\"38000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"38000\",\"opendate\":\"2015-12-02\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-08-20\",\"account\":\"2998009820254390\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-09-05\",\"recentpaydate\":\"2018-08-18\",\"actualpaymentamount\":\"3362\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"33789\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"30887\",\"scheduledpaymentamount\":\"3362\",\"usedhighestamount\":\"33789\"},\"line1\":{\"financeorg\":\"广发银行佛山分行\",\"orderno\":\"9\",\"creditlimitamount\":\"32000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2016-03-17\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-09-05\",\"account\":\"1021627472000002SMC002156069\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-08-15\",\"recentpaydate\":\"2018-07-30\",\"actualpaymentamount\":\"1521\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"8177\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"6582\",\"scheduledpaymentamount\":\"1521\",\"usedhighestamount\":\"10661\"},\"line1\":{\"financeorg\":\"广州农村商业银行\",\"orderno\":\"10\",\"creditlimitamount\":\"11000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"11000\",\"opendate\":\"2016-04-16\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-08-15\",\"account\":\"15665050571926\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-09-11\",\"recentpaydate\":\"2018-08-27\",\"actualpaymentamount\":\"1436\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"13940\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"11375\",\"scheduledpaymentamount\":\"1436\",\"usedhighestamount\":\"13940\"},\"line1\":{\"financeorg\":\"华夏银行\",\"orderno\":\"11\",\"creditlimitamount\":\"15000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"15000\",\"opendate\":\"2016-04-22\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-09-11\",\"account\":\"15663048242818\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-09-05\",\"recentpaydate\":\"2018-08-23\",\"actualpaymentamount\":\"1446\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"19632\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"21969\",\"scheduledpaymentamount\":\"1446\",\"usedhighestamount\":\"36035\"},\"line1\":{\"financeorg\":\"广州银行\",\"orderno\":\"12\",\"creditlimitamount\":\"66000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"66000\",\"opendate\":\"2016-05-03\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-09-05\",\"account\":\"15664130654081\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"************************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-08-25\",\"recentpaydate\":\"2016-06-08\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"0\"},\"line1\":{\"financeorg\":\"招商银行\",\"orderno\":\"13\",\"creditlimitamount\":\"32000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2016-06-08\",\"currency\":\"美元账户\",\"state\":\"\",\"stateenddate\":\"2018-08-25\",\"account\":\"0000000000000000000000145176975001001840\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNN*NNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-08-25\",\"recentpaydate\":\"2018-08-06\",\"actualpaymentamount\":\"4094\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"31584\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"27115\",\"scheduledpaymentamount\":\"4094\",\"usedhighestamount\":\"36736\"},\"line1\":{\"financeorg\":\"招商银行\",\"orderno\":\"14\",\"creditlimitamount\":\"32000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"32000\",\"opendate\":\"2016-06-08\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-08-25\",\"account\":\"0000000000000000000000145176975001001156\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2018-09-13\",\"recentpaydate\":\"2018-08-25\",\"actualpaymentamount\":\"2458\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"19911\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"20062\",\"scheduledpaymentamount\":\"2458\",\"usedhighestamount\":\"20408\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"15\",\"creditlimitamount\":\"20000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"20000\",\"opendate\":\"2016-07-20\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2018-09-13\",\"account\":\"15662508603090640911\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"\",\"beginmonth\":\"\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"\",\"scheduledpaymentdate\":\"\",\"recentpaydate\":\"\",\"actualpaymentamount\":\"\",\"curroverdueamount\":\"\"},\"line2\":{\"usedcreditlimitamount\":\"\",\"loanacctstate\":\"\",\"latest6monthusedavgamount\":\"\",\"scheduledpaymentamount\":\"\",\"usedhighestamount\":\"\"},\"line1\":{\"financeorg\":\"浦发银行信用卡中心\",\"orderno\":\"16\",\"creditlimitamount\":\"9000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2016-07-04\",\"currency\":\"人民币账户\",\"state\":\"销户\",\"stateenddate\":\"2016-10-07\",\"account\":\"15603100022010480\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"\",\"beginmonth\":\"\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"\",\"scheduledpaymentdate\":\"\",\"recentpaydate\":\"\",\"actualpaymentamount\":\"\",\"curroverdueamount\":\"\"},\"line2\":{\"usedcreditlimitamount\":\"\",\"loanacctstate\":\"\",\"latest6monthusedavgamount\":\"\",\"scheduledpaymentamount\":\"\",\"usedhighestamount\":\"\"},\"line1\":{\"financeorg\":\"浦发银行信用卡中心\",\"orderno\":\"17\",\"creditlimitamount\":\"9000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2016-07-04\",\"currency\":\"美元账户\",\"state\":\"销户\",\"stateenddate\":\"2016-10-07\",\"account\":\"84003100022010480\",\"badbalance\":\"\"}}],\"001002\":{\"line1\":{\"customername\":\"韦江\",\"certtype\":\"1\",\"certno\":\"45252319780805145X\"}},\"001003\":{\"line1\":{\"operateuser\":\"PBC440605_u*e*007\",\"queryreason\":\"本人查询（临柜）\",\"operateorg\":\"中国人民银行佛山市中心支行\"}},\"002004\":{\"line1\":{\"dataorg\":\"浦发银行信用卡中心\"}},\"003005\":{\"line1\":{\"accountcount\":\"15\",\"mincreditlimitperorg\":\"7000\",\"usedcreditlimit\":\"196156\",\"financeorgcount\":\"12\",\"maxcreditlimitperorg\":\"66000\",\"latest6monthusedavgamount\":\"186831\",\"creditlimit\":\"281784\",\"financecorpcount\":\"12\"}},\"002005\":{\"line2\":[{\"orderno\":\"1\",\"dataorg\":\"深圳前海微众银行股份有限公司\"},{\"orderno\":\"2\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"3\",\"dataorg\":\"浦发银行信用卡中心\"},{\"orderno\":\"4\",\"dataorg\":\"交通银行\"},{\"orderno\":\"5\",\"dataorg\":\"招商银行\"}],\"line1\":[{\"orderno\":\"1\",\"address\":\"广东佛山禅城区新虹三街12号乐丰养生美容会所\",\"gettime\":\"2017-06-12\",\"residencetype\":\"未知\"},{\"orderno\":\"2\",\"address\":\"广东佛山禅城区新虹三街12号乐丰养生美容会所\",\"gettime\":\"2016-07-20\",\"residencetype\":\"未知\"},{\"orderno\":\"3\",\"address\":\"佛山市禅城区佛山大道北171号康辉旅游公司宿舍501室\",\"gettime\":\"2016-07-04\",\"residencetype\":\"未知\"},{\"orderno\":\"4\",\"address\":\"东方广场明珠城A座908房\",\"gettime\":\"2016-06-28\",\"residencetype\":\"未知\"},{\"orderno\":\"5\",\"address\":\"广东省佛山市禅城区东方广场明珠城A座90\",\"gettime\":\"2016-06-08\",\"residencetype\":\"未知\"}]},\"003001\":{\"line2\":{\"digital\":\"\",\"todescribe\":\"\",\"reposition\":\"\"},\"line1\":{\"loancardcount\":\"17\",\"otherloancount\":\"3\",\"firstloancardopenmonth\":\"2012-12\",\"standardloancardcount\":\"0\",\"firststandardloancardopenmonth\":\"--\",\"dissentcount\":\"0\",\"houseloancount\":\"0\",\"houseloan2count\":\"0\",\"firstloanopenmonth\":\"2017-06\",\"announcecount\":\"0\"}},\"004003\":[{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"////////*NNNNNN*NNNNNN*N\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-09\",\"beginmonth\":\"2016-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"orderno\":\"1\",\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"overdueover180amount\":\"0\",\"overdue61to90amount\":\"0\",\"overdue31to60amount\":\"0\",\"curroverdueamount\":\"0\",\"overdue91to180amount\":\"0\"},\"line2\":{\"loanacctstate\":\"正常\",\"balance\":\"10550\",\"scheduledpaymentdate\":\"2018-09-12\",\"scheduledpaymentamount\":\"725\",\"recentpaydate\":\"2018-09-12\",\"remainpaymentcyc\":\"19\",\"class5state\":\"正常\",\"actualpaymentamount\":\"725\"},\"line1\":{\"financeorg\":\"深圳前海微众银行股份有限公司\",\"orderno\":\"1\",\"paymentcyc\":\"20\",\"creditlimitamount\":\"11000\",\"guaranteetype\":\"信用/免担保\",\"opendate\":\"2017-06-12\",\"paymentrating\":\"按月归还\",\"type\":\"个人消费贷款\",\"stateenddate\":\"2018-09-12\",\"enddate\":\"2020-04-12\",\"currency\":\"人民币\",\"state\":\"\",\"account\":\"1129606001437579\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"/////////////////////*NN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"orderno\":\"1\",\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"overdueover180amount\":\"0\",\"overdue61to90amount\":\"0\",\"overdue31to60amount\":\"0\",\"curroverdueamount\":\"0\",\"overdue91to180amount\":\"0\"},\"line2\":{\"loanacctstate\":\"正常\",\"balance\":\"7474\",\"scheduledpaymentdate\":\"2018-08-23\",\"scheduledpaymentamount\":\"1955\",\"recentpaydate\":\"2018-08-23\",\"remainpaymentcyc\":\"4\",\"class5state\":\"正常\",\"actualpaymentamount\":\"1955\"},\"line1\":{\"financeorg\":\"招联消费金融有限公司\",\"orderno\":\"2\",\"paymentcyc\":\"6\",\"creditlimitamount\":\"11000\",\"guaranteetype\":\"信用/免担保\",\"opendate\":\"2018-06-23\",\"paymentrating\":\"按月归还\",\"type\":\"个人消费贷款\",\"stateenddate\":\"2018-08-23\",\"enddate\":\"2018-12-23\",\"currency\":\"人民币\",\"state\":\"\",\"account\":\"180623008846727100\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"///////////////////////*\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2018-08\",\"beginmonth\":\"2016-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"orderno\":\"1\",\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"overdueover180amount\":\"0\",\"overdue61to90amount\":\"0\",\"overdue31to60amount\":\"0\",\"curroverdueamount\":\"0\",\"overdue91to180amount\":\"0\"},\"line2\":{\"loanacctstate\":\"正常\",\"balance\":\"2500\",\"scheduledpaymentdate\":\"2018-08-18\",\"scheduledpaymentamount\":\"0\",\"recentpaydate\":\"2018-08-18\",\"remainpaymentcyc\":\"6\",\"class5state\":\"正常\",\"actualpaymentamount\":\"0\"},\"line1\":{\"financeorg\":\"招联消费金融有限公司\",\"orderno\":\"3\",\"paymentcyc\":\"6\",\"creditlimitamount\":\"2500\",\"guaranteetype\":\"信用/免担保\",\"opendate\":\"2018-08-18\",\"paymentrating\":\"按月归还\",\"type\":\"个人消费贷款\",\"stateenddate\":\"2018-08-18\",\"enddate\":\"2019-02-23\",\"currency\":\"人民币\",\"state\":\"\",\"account\":\"180818009926074500\",\"badbalance\":\"\"}}],\"002001\":{\"line4\":{\"hometelephonenoorg\":\"深圳前海微众银行股份有限公司\",\"edulevelorg\":\"兴业银行\",\"officetelephonenoorg\":\"深圳前海微众银行股份有限公司\",\"edudegreeorg\":\"深圳前海微众银行股份有限公司\"},\"line3\":{\"edulevel\":\"大学专科和专科学校（简称“大专”）\",\"edudegree\":\"其他\",\"officetelephoneno\":\"81062131\",\"hometelephoneno\":\"00000000000\"},\"line2\":{\"mobileorg\":\"招联消费金融有限公司\",\"genderorg\":\"招联消费金融有限公司\",\"maritalstateorg\":\"浦发银行信用卡中心\",\"birthdayorg\":\"招联消费金融有限公司\"},\"line1\":{\"birthday\":\"1978-08-05\",\"gender\":\"男性\",\"maritalstate\":\"未婚\",\"mobile\":\"13929943893\"}},\"008003\":{\"line1\":[{\"querydate\":\"2018-07-18\",\"orderno\":\"1\",\"queryreason\":\"本人查询（临柜）\",\"querier\":\"中国人民银行佛山市中心支行\"},{\"querydate\":\"2018-03-26\",\"orderno\":\"2\",\"queryreason\":\"本人查询（临柜）\",\"querier\":\"中国人民银行佛山市中心支行\"}]},\"008002\":{\"line1\":[{\"querydate\":\"2018-09-25\",\"orderno\":\"1\",\"queryreason\":\"贷后管理\",\"querier\":\"交通银行太平洋信用卡中心\"},{\"querydate\":\"2018-09-19\",\"orderno\":\"2\",\"queryreason\":\"贷款审批\",\"querier\":\"平安普惠融资担保有限公司\"},{\"querydate\":\"2018-09-16\",\"orderno\":\"3\",\"queryreason\":\"贷后管理\",\"querier\":\"深圳前海微众银行股份有限公司\"},{\"querydate\":\"2018-09-02\",\"orderno\":\"4\",\"queryreason\":\"贷后管理\",\"querier\":\"浦发银行信用卡中心\"},{\"querydate\":\"2018-08-14\",\"orderno\":\"5\",\"queryreason\":\"保前审查\",\"querier\":\"中国平安财产保险股份有限公司\"},{\"querydate\":\"2018-08-13\",\"orderno\":\"6\",\"queryreason\":\"保前审查\",\"querier\":\"中国人民财产保险股份有限公司\"},{\"querydate\":\"2018-08-13\",\"orderno\":\"7\",\"queryreason\":\"保前审查\",\"querier\":\"阳光保险集团股份有限公司\"},{\"querydate\":\"2018-08-01\",\"orderno\":\"8\",\"queryreason\":\"贷后管理\",\"querier\":\"平安银行信用卡中心\"},{\"querydate\":\"2018-07-24\",\"orderno\":\"9\",\"queryreason\":\"保前审查\",\"querier\":\"中国平安财产保险股份有限公司\"},{\"querydate\":\"2018-07-18\",\"orderno\":\"10\",\"queryreason\":\"贷款审批\",\"querier\":\"重庆百度小额贷款有限公司\"},{\"querydate\":\"2018-06-15\",\"orderno\":\"11\",\"queryreason\":\"贷款审批\",\"querier\":\"河南中原消费金融股份有限公司\"},{\"querydate\":\"2018-06-15\",\"orderno\":\"12\",\"queryreason\":\"贷款审批\",\"querier\":\"上海银行福民支行\"},{\"querydate\":\"2018-06-06\",\"orderno\":\"13\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2018-06-05\",\"orderno\":\"14\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2018-05-29\",\"orderno\":\"15\",\"queryreason\":\"贷后管理\",\"querier\":\"浦发银行信用卡中心\"},{\"querydate\":\"2018-03-17\",\"orderno\":\"16\",\"queryreason\":\"贷后管理\",\"querier\":\"招商银行\"},{\"querydate\":\"2018-02-27\",\"orderno\":\"17\",\"queryreason\":\"贷后管理\",\"querier\":\"交通银行太平洋信用卡中心\"},{\"querydate\":\"2018-02-25\",\"orderno\":\"18\",\"queryreason\":\"贷后管理\",\"querier\":\"浦发银行信用卡中心\"},{\"querydate\":\"2017-12-30\",\"orderno\":\"19\",\"queryreason\":\"贷后管理\",\"querier\":\"中国农业银行\"},{\"querydate\":\"2017-12-26\",\"orderno\":\"20\",\"queryreason\":\"贷后管理\",\"querier\":\"深圳前海微众银行股份有限公司\"},{\"querydate\":\"2017-12-23\",\"orderno\":\"21\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2017-12-17\",\"orderno\":\"22\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2017-12-09\",\"orderno\":\"23\",\"queryreason\":\"贷后管理\",\"querier\":\"浦发银行信用卡中心\"},{\"querydate\":\"2017-12-01\",\"orderno\":\"24\",\"queryreason\":\"贷后管理\",\"querier\":\"广州银行\"},{\"querydate\":\"2017-09-30\",\"orderno\":\"25\",\"queryreason\":\"贷后管理\",\"querier\":\"中国农业银行\"},{\"querydate\":\"2017-09-29\",\"orderno\":\"26\",\"queryreason\":\"贷后管理\",\"querier\":\"浦发银行信用卡中心\"},{\"querydate\":\"2017-09-10\",\"orderno\":\"27\",\"queryreason\":\"贷后管理\",\"querier\":\"广州银行\"},{\"querydate\":\"2017-08-17\",\"orderno\":\"28\",\"queryreason\":\"贷后管理\",\"querier\":\"招商银行\"},{\"querydate\":\"2017-08-08\",\"orderno\":\"29\",\"queryreason\":\"贷款审批\",\"querier\":\"南京银行股份有限公司\"},{\"querydate\":\"2017-08-01\",\"orderno\":\"30\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2017-07-18\",\"orderno\":\"31\",\"queryreason\":\"贷后管理\",\"querier\":\"平安银行信用卡中心\"},{\"querydate\":\"2017-07-11\",\"orderno\":\"32\",\"queryreason\":\"贷后管理\",\"querier\":\"广州银行\"},{\"querydate\":\"2017-07-02\",\"orderno\":\"33\",\"queryreason\":\"贷后管理\",\"querier\":\"兴业银行\"},{\"querydate\":\"2017-06-30\",\"orderno\":\"34\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2017-06-28\",\"orderno\":\"35\",\"queryreason\":\"贷后管理\",\"querier\":\"中国农业银行\"},{\"querydate\":\"2017-06-12\",\"orderno\":\"36\",\"queryreason\":\"贷后管理\",\"querier\":\"深圳前海微众银行股份有限公司\"},{\"querydate\":\"2017-06-02\",\"orderno\":\"37\",\"queryreason\":\"贷后管理\",\"querier\":\"浦发银行信用卡中心\"},{\"querydate\":\"2017-04-07\",\"orderno\":\"38\",\"queryreason\":\"贷后管理\",\"querier\":\"交通银行太平洋信用卡中心\"},{\"querydate\":\"2017-03-26\",\"orderno\":\"39\",\"queryreason\":\"贷后管理\",\"querier\":\"中国农业银行\"},{\"querydate\":\"2017-03-12\",\"orderno\":\"40\",\"queryreason\":\"贷后管理\",\"querier\":\"浦发银行信用卡中心\"},{\"querydate\":\"2017-02-17\",\"orderno\":\"41\",\"queryreason\":\"贷后管理\",\"querier\":\"兴业银行\"},{\"querydate\":\"2017-02-06\",\"orderno\":\"42\",\"queryreason\":\"保前审查\",\"querier\":\"中国平安财产保险股份有限公司\"},{\"querydate\":\"2017-01-24\",\"orderno\":\"43\",\"queryreason\":\"贷后管理\",\"querier\":\"招商银行\"},{\"querydate\":\"2017-01-16\",\"orderno\":\"44\",\"queryreason\":\"保前审查\",\"querier\":\"中国平安财产保险股份有限公司\"},{\"querydate\":\"2017-01-14\",\"orderno\":\"45\",\"queryreason\":\"贷后管理\",\"querier\":\"招联消费金融有限公司\"},{\"querydate\":\"2016-12-28\",\"orderno\":\"46\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2016-12-26\",\"orderno\":\"47\",\"queryreason\":\"信用卡审批\",\"querier\":\"广州银行\"},{\"querydate\":\"2016-12-23\",\"orderno\":\"48\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2016-12-20\",\"orderno\":\"49\",\"queryreason\":\"贷后管理\",\"querier\":\"中国农业银行\"},{\"querydate\":\"2016-12-20\",\"orderno\":\"50\",\"queryreason\":\"贷后管理\",\"querier\":\"招商银行\"},{\"querydate\":\"2016-12-11\",\"orderno\":\"51\",\"queryreason\":\"贷后管理\",\"querier\":\"浦发银行信用卡中心\"},{\"querydate\":\"2016-11-01\",\"orderno\":\"52\",\"queryreason\":\"贷款审批\",\"querier\":\"深圳前海微众银行股份有限公司\"}]},\"008001\":{\"line1\":{\"recordsum2\":\"0\",\"orgsum2\":\"0\",\"recordsumself\":\"0\",\"orgsum1\":\"1\",\"recordsum1\":\"1\",\"towyearrecordsum3\":\"0\",\"towyearrecordsum2\":\"0\",\"towyearrecordsum1\":\"39\"}}}";

        //大道
        String str4 = "{\"reportBaseInfo\":{\"reportNo\":\"2018101100006273089534\",\"operateUser\":\"中国人民银行佛山市中心支行\",\"queryReason\":\"本人查询（临柜）\",\"name\":\"测宁颖\",\"certType\":\"身份证\",\"certNo\":\"44030519960609236X\",\"queryTime\":\"2018/10/1111:54:00\",\"reportCreateTime\":\"2018/10/1111:54:00\"},\"icrIdentity\":{\"gender\":\"男性\",\"birthday\":\"1978/08/05\",\"maritalState\":\"未婚\",\"mobile\":\"13929943893\",\"officetelePhoneno\":\"81062131\",\"hometelePhoneno\":\"00000000000\",\"edulevel\":\"大学专科和专科学校（简称“大专”）\",\"edudegree\":\"其他\",\"postAddress\":\"广东佛山禅城区佛山大道北171号2楼\",\"registeredAddress\":\"广西壮族自治区贵港市桂平市蒙圩镇林村寻挞\"},\"icrSpouse\":[{\"name\":\"--\",\"certType\":\"--\",\"certNo\":\"--\",\"employer\":\"--\",\"telephoneNo\":\"--\"}],\"icrProfessional\":[{\"serialNo\":\"0\",\"employer\":\"佛山康辉国际旅行社\",\"employerAddress\":\"--\",\"occupation\":\"--\",\"industry\":\"--\",\"duty\":\"其他\",\"title\":\"--\",\"startYear\":\"--\",\"getTime\":\"2017/06/12\"},{\"serialNo\":\"1\"," +
                "\"employer\":\"佛山康辉国际旅行社有限公司\",\"employerAddress\":\"佛山市禅城区佛山大道北171号5楼\",\"occupation\":\"办事人员和有关人员\",\"industry\":\"居民服务和其他服务业\",\"duty\":\"其他\",\"title\":\"--\",\"startYear\":\"--\",\"getTime\":\"2016/07/04\"},{\"serialNo\":\"2\",\"employer\":\"佛山市康辉国际旅行社有限公司\",\"employerAddress\":\"广东省佛山市南海区桂城佛平三路紫金城B1001佛山市康辉国际旅行\",\"occupation\":\"--\",\"industry\":\"文化、体育和娱乐业\",\"duty\":\"其他\",\"title\":\"--\",\"startYear\":\"--\",\"getTime\":\"2016/04/16\"},{\"serialNo\":\"3\",\"employer\":\"佛山康辉国际旅行社有限公司业务二部\",\"employerAddress\":\"广东省佛山市禅城佛山大道北171号(旅游大厦旁)五楼\",\"occupation\":\"商业、服务业人员\",\"industry\":\"居民服务和其他服务业\",\"duty\":\"--\",\"title\":\"高级\",\"startYear\":\"2013\",\"getTime\":\"2015/03/27\"},{\"serialNo\":\"4\",\"employer\":\"佛山市康辉国际旅行社有限公司销售\",\"employerAddress\":\"--\",\"occupation\":\"--\",\"industry\":\"租赁和商务服务业\",\"duty\":\"--\",\"title\":\"--\",\"startYear\":\"--\",\"getTime\":\"2014/12/02\"}],\"icrResidence\":[{\"serialNo\":\"0\",\"address\":\"广东佛山禅城区新虹三街12号乐丰养生美容会所\",\"residenceType\":\"未知\",\"getTime\":\"2017/06/12\"},{\"serialNo\":\"1\",\"address\":\"广东佛山禅城区新虹三街12号乐丰养生美容会所\",\"residenceType\":\"未知\",\"getTime\":\"2016/07/20\"},{\"serialNo\":\"2\",\"address\":\"佛山市禅城区佛山大道北171号康辉旅游公司宿舍501室\",\"residenceType\":\"未知\",\"getTime\":\"2016/07/04\"},{\"serialNo\":\"3\",\"address\":\"东方广场明珠城A座908房\",\"residenceType\":\"未知\",\"getTime\":\"2016/06/28\"},{\"serialNo\":\"4\",\"address\":\"广东省佛山市禅城区东方广场明珠城A座90\",\"residenceType\":\"未知\",\"getTime\":\"2016/06/08\"}],\"icrCreditCue\":{\"houseLoanCount\":\"0\",\"otherLoanCount\":\"3\",\"firstLoanOpenMonth\":\"2017/06\",\"loanCardCount\":\"17\",\"firstLoanCardOpenMonth\":\"2012/12\",\"standardLoanCardCount\":\"0\",\"announceCount\":\"0\",\"dissentCount\":\"0\",\"houseLoan2Count\":\"0\"},\"icrUnpaidLoan\":{\"financeCorpCount\":\"2\",\"financeOrgCount\":\"2\",\"accountCount\":\"3\",\"creditLimit\":\"24500\",\"balance\":\"20524\",\"latest6MonthUseDavgAmount\":\"2062\"},\"icrUndestoryLoanCard\":{\"financeCorpCount\":\"12\",\"financeOrgCount\":\"12\",\"accountCount\":\"15\",\"creditLimit\":\"281784\",\"maxCreditLimitPerOrg\":\"66000\",\"minCreditLimitPerOrg\":\"7000\",\"usedCreditLimit\":\"196156\",\"latest6MonthUseDavgAmount\":\"186831\"},\"icrUndestoryStandardLoanCard\":{},\"icrOverdueSummary\":{\"count\":\"0\",\"months\":\"0\",\"maxDuration\":\"0\",\"count2\":\"2\",\"months2\":\"2\",\"maxDuration2\":\"1\",\"count3\":\"0\",\"months3\":\"0\",\"maxDuration3\":\"0\"},\"icrLoanInfo\":[{\"class5State\":\"正常\",\"balance\":\"10550\",\"remainPayMentcyc\":\"19\",\"scheduledPayMentAmount\":\"725\",\"scheduledPayMentDate\":\"2018/09/12\",\"actualPayMentAmount\":\"725\",\"recentPayDate\":\"2018/09/12\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"overdue31To60Amount\":\"0\",\"overdue61To90Amount\":\"0\",\"overdue91To180Amount\":\"0\",\"overdueOver180Amount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"1\",\"bizType\":\"贷款\",\"cue\":\"\",\"financeorg\":\"深圳前海微众银行股份有限公司\",\"account\":\"\",\"type\":\"个人消费贷款\",\"currency\":\"人民币\",\"openDate\":\"2017/06/12\",\"endDate\":\"2020/04/12\",\"creditLimitAmount\":\"11000\",\"guaranteeType\":\"信用/免担保\",\"payMentRating\":\"按月归还\",\"payMentCyc\":\"20\",\"state\":\"正常\",\"stateendDate\":\"2018/09/12\",\"stateendMonth\":\"\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"////////*NNNNNN*NNNNNN*N\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"class5State\":\"正常\",\"balance\":\"7474\",\"remainPayMentcyc\":\"4\",\"scheduledPayMentAmount\":\"1955\",\"scheduledPayMentDate\":\"2018/08/23\",\"actualPayMentAmount\":\"1955\",\"recentPayDate\":\"2018/08/23\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"overdue31To60Amount\":\"0\",\"overdue61To90Amount\":\"0\",\"overdue91To180Amount\":\"0\",\"overdueOver180Amount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"LOAN1\",\"bizType\":\"贷款\",\"cue\":\"1.2017年06月12日机构“深圳前海微众银行股份有限公司”发放的11000元(人民币)个人消费贷款,业务号12960469信用免担保,20期,按月还,2020年0月12日到期,截至2018年0月12日\",\"financeorg\":\"招联消费金融有限公司\",\"account\":\"12960469\",\"type\":\"个人消费贷款\",\"currency\":\"人民币\",\"openDate\":\"2018/06/23\",\"endDate\":\"2018/12/23\",\"creditLimitAmount\":\"11000\",\"guaranteeType\":\"信用/免担保\",\"payMentRating\":\"按月归还\",\"payMentCyc\":\"6\",\"state\":\"正常\",\"stateendDate\":\"2018/08/23\",\"stateendMonth\":\"\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"/////////////////////*NN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"class5State\":\"正常\",\"balance\":\"2500\",\"remainPayMentcyc\":\"6\",\"scheduledPayMentAmount\":\"0\",\"scheduledPayMentDate\":\"2018/08/18\",\"actualPayMentAmount\":\"0\",\"recentPayDate\":\"2018/08/18\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"overdue31To60Amount\":\"0\",\"overdue61To90Amount\":\"0\",\"overdue91To180Amount\":\"0\",\"overdueOver180Amount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"LOAN2\",\"bizType\":\"贷款\",\"cue\":\"2.2018年06月23日机构“招联消费金融有限公司”发放的1100(民币)个人消费货款,业务号8023008462710信用/免担保,6朋,按月归还,2018年12月23日到期、截至2018年08月23日,\",\"financeorg\":\"招联消费金融有限公司\",\"account\":\"8023008462710\",\"type\":\"个人消费贷款\",\"currency\":\"人民币\",\"openDate\":\"2018/08/18\",\"endDate\":\"2019/02/23\",\"creditLimitAmount\":\"2500\",\"guaranteeType\":\"信用/免担保\",\"payMentRating\":\"按月归还\",\"payMentCyc\":\"6\",\"state\":\"正常\",\"stateendDate\":\"2018/08/18\",\"stateendMonth\":\"\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"///////////////////////*\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"}],\"icrLoanCardInfo\":[{\"shareCreditLimitAmount\":\"48284\",\"usedCreditLimitAmount\":\"0\",\"latest6MonthUsedAvgAmount\":\"0\",\"usedHighestAmount\":\"13693\",\"scheduledPaymentAmount\":\"0\",\"scheduledPaymentDate\":\"2018/09/05\",\"actualPaymentAmount\":\"0\",\"recentPayDate\":\"2018/02/23\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD0\",\"bizType\":\"贷记卡\",\"account\":\"1021627472000000256061\",\"cue\":\"1.2012年12月26日机构“广发银行佛山分行”发放的货记卡(人民币账户),业务号1021627472000000256061,授信额度18.000元,共享授信额度48,284元,信用/免担保。截至2018年0月05\",\"financeOrg\":\"广发银行佛山分行\",\"currency\":\"人民币账户\",\"openDate\":\"2012/12/26\",\"creditLimitAmount\":\"18000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/09/05\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"N1NNNNNNNNNNNNNNNN******\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"7000\",\"usedCreditLimitAmount\":\"6991\",\"latest6MonthUsedAvgAmount\":\"6469\",\"usedHighestAmount\":\"6991\",\"scheduledPaymentAmount\":\"698\",\"scheduledPaymentDate\":\"2018/08/26\",\"actualPaymentAmount\":\"698\",\"recentPayDate\":\"2018/08/17\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD1\",\"bizType\":\"贷记卡\",\"account\":\"371001130120048797\",\"cue\":\"22016年06月28日机构“交通银行”发放的货记卡(人民币账户),业务号371001130120048797,投信额度7000元,共享授信额度7.000元,信用/免担保。截至2018年08月26日,\",\"financeOrg\":\"交通银行\",\"currency\":\"人民币账户\",\"openDate\":\"2016/06/28\",\"creditLimitAmount\":\"7000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/08/26\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"NNN**NNNNNNNN1NNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"18000\",\"usedCreditLimitAmount\":\"5144\",\"latest6MonthUsedAvgAmount\":\"11271\",\"usedHighestAmount\":\"17842\",\"scheduledPaymentAmount\":\"2568\",\"scheduledPaymentDate\":\"2018/08/27\",\"actualPaymentAmount\":\"2568\",\"recentPayDate\":\"2018/08/14\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD2\",\"bizType\":\"贷记卡\",\"account\":\"15603050014398161\",\"cue\":\"3.2014年07月25日机构“中国民生银行”发放的货记卡(人民币账户),业务号15603050014398161,授信额度18.00元,共享授信额度18.000,信用/免担保,截至2018年08月27日\",\"financeOrg\":\"中国民生银行\",\"currency\":\"人民币账户\",\"openDate\":\"2014/07/25\",\"creditLimitAmount\":\"18000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/08/27\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"8500\",\"usedCreditLimitAmount\":\"7649\",\"latest6MonthUsedAvgAmount\":\"7980\",\"usedHighestAmount\":\"8544\",\"scheduledPaymentAmount\":\"395\",\"scheduledPaymentDate\":\"2018/09/17\",\"actualPaymentAmount\":\"396\",\"recentPayDate\":\"2018/09/03\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD3\",\"bizType\":\"贷记卡\",\"account\":\"9559123388923566936\",\"cue\":\"42014年12月02日机构“中信银行”发放的贷记卡(人民币账户),业务号9559123388923566936,授信额度8.500元,共享授信额度8.500元,信用/免担保,截至2018年09月17日,\",\"financeOrg\":\"中信银行\",\"currency\":\"人民币账户\",\"openDate\":\"2014/12/02\",\"creditLimitAmount\":\"8500\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/09/17\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"0\",\"usedCreditLimitAmount\":\"0\",\"latest6MonthUsedAvgAmount\":\"0\",\"usedHighestAmount\":\"0\",\"scheduledPaymentAmount\":\"0\",\"scheduledPaymentDate\":\"2018/08/07\",\"actualPaymentAmount\":\"0\",\"recentPayDate\":\"2016/07/04\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD4\",\"bizType\":\"贷记卡\",\"account\":\"8400310001379646\",\"cue\":\"5.2015年01月20日机构“浦发银行信用卡中心”发放的货记卡(美元账户),业务号8400310001379646,授信额度折合人民币9.00元,共享授信额度折合人民币0元,信用/免担保。截至2018年08月07日\",\"financeOrg\":\"浦发银行信用卡中心\",\"currency\":\"美元账户\",\"openDate\":\"2015/01/20\",\"creditLimitAmount\":\"9000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/08/07\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"************************\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"9000\",\"usedCreditLimitAmount\":\"9058\",\"latest6MonthUsedAvgAmount\":\"8592\",\"usedHighestAmount\":\"9058\",\"scheduledPaymentAmount\":\"555\",\"scheduledPaymentDate\":\"2018/09/07\",\"actualPaymentAmount\":\"555\",\"recentPayDate\":\"2018/08/18\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD5\",\"bizType\":\"贷记卡\",\"account\":\"15603100013799646\",\"cue\":\"62015年0月20日机构浦发银行信用卡中心”发放的货记卡(人民币账户),业务号15603100013799646,授信额度9.000元,共享投信额度9.000元,信用/免担保,截至2018年09月07日,\",\"financeOrg\":\"浦发银行信用卡中心\",\"currency\":\"人民币账户\",\"openDate\":\"2015/01/20\",\"creditLimitAmount\":\"9000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/09/07\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"9000\",\"usedCreditLimitAmount\":\"9020\",\"latest6MonthUsedAvgAmount\":\"9964\",\"usedHighestAmount\":\"19304\",\"scheduledPaymentAmount\":\"2162\",\"scheduledPaymentDate\":\"2018/09/07\",\"actualPaymentAmount\":\"2162\",\"recentPayDate\":\"2018/08/18\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD6\",\"bizType\":\"贷记卡\",\"account\":\"44257016000384\",\"cue\":\"72015年0月27日机构“中国农业行”发放的货记卡《人民币户),业务号44257016000384,授信频度9.00元,共享授信额度9.00元,信用/免担保,截至2018年0月07日,\",\"financeOrg\":\"中国农业银行\",\"currency\":\"人民币账户\",\"openDate\":\"2015/03/27\",\"creditLimitAmount\":\"9000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/09/07\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"NN*NNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"38000\",\"usedCreditLimitAmount\":\"31261\",\"latest6MonthUsedAvgAmount\":\"24571\",\"usedHighestAmount\":\"31261\",\"scheduledPaymentAmount\":\"3679\",\"scheduledPaymentDate\":\"2018/08/20\",\"actualPaymentAmount\":\"3679\",\"recentPayDate\":\"2018/07/28\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD7\",\"bizType\":\"贷记卡\",\"account\":\"298005130\",\"cue\":\"82015年12月02日机构“平安银行信用卡中心”发放的货记卡(人民币户),业务号298005130,.授信额度3.0元,共享授信额度38.00,值用/免担保,截至2018年08月20日,\",\"financeOrg\":\"平安银行信用卡中心\",\"currency\":\"人民币账户\",\"openDate\":\"2015/12/02\",\"creditLimitAmount\":\"38000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/08/20\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"0\",\"usedCreditLimitAmount\":\"33789\",\"latest6MonthUsedAvgAmount\":\"30887\",\"usedHighestAmount\":\"33789\",\"scheduledPaymentAmount\":\"3362\",\"scheduledPaymentDate\":\"2018/09/05\",\"actualPaymentAmount\":\"3362\",\"recentPayDate\":\"2018/08/18\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD8\",\"bizType\":\"贷记卡\",\"account\":\"10216272472000001506\",\"cue\":\"9.2016年03月17日机构“广发银行佛山分行”发放的贷记卡(人民币账户),业务号10216272472000001506授信额度32.000元,共享授信额度0元,信用/免担保。截至2018年0月05日,\",\"financeOrg\":\"广发银行佛山分行\",\"currency\":\"人民币账户\",\"openDate\":\"2016/03/17\",\"creditLimitAmount\":\"32000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/09/05\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"11000\",\"usedCreditLimitAmount\":\"8177\",\"latest6MonthUsedAvgAmount\":\"6582\",\"usedHighestAmount\":\"10661\",\"scheduledPaymentAmount\":\"1521\",\"scheduledPaymentDate\":\"2018/08/15\",\"actualPaymentAmount\":\"1521\",\"recentPayDate\":\"2018/07/30\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD9\",\"bizType\":\"贷记卡\",\"account\":\"15665050571926\",\"cue\":\"10.2016年04月16日机构“广州衣村商业银行”发放的货记卡(人民币账户),业务号15665050571926,授信额度1.000元,共享授信额度11,000元,信用/免担保,截至2018年08月15日,\",\"financeOrg\":\"广州农村商业银行\",\"currency\":\"人民币账户\",\"openDate\":\"2016/04/16\",\"creditLimitAmount\":\"11000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/08/15\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"15000\",\"usedCreditLimitAmount\":\"13940\",\"latest6MonthUsedAvgAmount\":\"11375\",\"usedHighestAmount\":\"13940\",\"scheduledPaymentAmount\":\"1436\",\"scheduledPaymentDate\":\"2018/09/11\",\"actualPaymentAmount\":\"1436\",\"recentPayDate\":\"2018/08/27\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD10\",\"bizType\":\"贷记卡\",\"account\":\"15663048242818\",\"cue\":\"112016年04月22日机构“华夏银行”发放的货记卡(人民币账户),业务号15663048242818,授信额度15.00c共享授信额度15.000元,信用/免担保,截至2018年09月11日,\",\"financeOrg\":\"华夏银行\",\"currency\":\"人民币账户\",\"openDate\":\"2016/04/22\",\"creditLimitAmount\":\"15000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/09/11\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"66000\",\"usedCreditLimitAmount\":\"19632\",\"latest6MonthUsedAvgAmount\":\"21969\",\"usedHighestAmount\":\"36035\",\"scheduledPaymentAmount\":\"1446\",\"scheduledPaymentDate\":\"2018/09/05\",\"actualPaymentAmount\":\"1446\",\"recentPayDate\":\"2018/08/23\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD11\",\"bizType\":\"贷记卡\",\"account\":\"1566130654081\",\"cue\":\"12.2016年05月03日机构“广州银行”发放的货记卡(人民币账户),业务号1566130654081,授信额度66.000元,共享授信额度6600元,信用/免担保,截至2018年09月065日\",\"financeOrg\":\"广州银行\",\"currency\":\"人民币账户\",\"openDate\":\"2016/05/03\",\"creditLimitAmount\":\"66000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/09/05\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"0\",\"usedCreditLimitAmount\":\"0\",\"latest6MonthUsedAvgAmount\":\"0\",\"usedHighestAmount\":\"0\",\"scheduledPaymentAmount\":\"0\",\"scheduledPaymentDate\":\"2018/08/25\",\"actualPaymentAmount\":\"0\",\"recentPayDate\":\"2016/06/08\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD12\",\"bizType\":\"贷记卡\",\"account\":\"000517675001001840\",\"cue\":\"13.2016年06月08日机构“招商银行”发放的货记卡(美元账户),业务号000517675001001840,授信额度折合人民币32.000元,共享授信额度折合人民币0元,信用/免担保,截至2018年08月25日,\",\"financeOrg\":\"招商银行\",\"currency\":\"美元账户\",\"openDate\":\"2016/06/08\",\"creditLimitAmount\":\"32000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/08/25\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"************************\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"32000\",\"usedCreditLimitAmount\":\"31584\",\"latest6MonthUsedAvgAmount\":\"27115\",\"usedHighestAmount\":\"36736\",\"scheduledPaymentAmount\":\"4094\",\"scheduledPaymentDate\":\"2018/08/25\",\"actualPaymentAmount\":\"4094\",\"recentPayDate\":\"2018/08/06\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD13\",\"bizType\":\"贷记卡\",\"account\":\"000000000145176975001001156\",\"cue\":\"14.2016年06月08日机构“招商银行”发放的货记卡(人民币账户),业务号000000000145176975001001156,授信额度32.000元,共享授信额度32.000元,信用/免担保。截至2018年08月25日,\",\"financeOrg\":\"招商银行\",\"currency\":\"人民币账户\",\"openDate\":\"2016/06/08\",\"creditLimitAmount\":\"32000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/08/25\",\"beginMonth\":\"2016/09\",\"endMonth\":\"2018/08\",\"latest24State\":\"NNNNN*NNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"20000\",\"usedCreditLimitAmount\":\"19911\",\"latest6MonthUsedAvgAmount\":\"20062\",\"usedHighestAmount\":\"20408\",\"scheduledPaymentAmount\":\"2458\",\"scheduledPaymentDate\":\"2018/09/13\",\"actualPaymentAmount\":\"2458\",\"recentPayDate\":\"2018/08/25\",\"currOverdueCyc\":0,\"currOverdueAmount\":\"0\",\"reportNo\":\"\",\"serialNo\":\"CARD14\",\"bizType\":\"贷记卡\",\"account\":\"1566508603090640911\",\"cue\":\"15206年07月20日机构“兴业银行”发放的货记卡(人民币账户),业务号1566508603090640911授信额度2.00元,共享授信额度2.00元,信用/免担保,截至2018年09月13日,\",\"financeOrg\":\"兴业银行\",\"currency\":\"人民币账户\",\"openDate\":\"2016/07/20\",\"creditLimitAmount\":\"20000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"正常\",\"stateEndDate\":\"2018/09/13\",\"beginMonth\":\"2016/10\",\"endMonth\":\"2018/09\",\"latest24State\":\"NNNNNNNNNNNNNNNNNNNNNNNN\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"0\",\"scheduledPaymentDate\":\"\",\"recentPayDate\":\"\",\"reportNo\":\"\",\"serialNo\":\"CARD15\",\"bizType\":\"贷记卡\",\"account\":\"156031001410\",\"cue\":\"6.2016年07月04日机构“清发银行信用卡中心”发放的货记卡(人民币账户),业务号156031001410,.授信度9.00元,共享授信额度0元,信用/免担保,截至2016年10月07日,户状态为“镇户”\",\"financeOrg\":\"浦发银行信用卡中心\",\"currency\":\"人民币账户\",\"openDate\":\"2016/07/04\",\"creditLimitAmount\":\"9000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"销户\",\"stateEndDate\":\"2016/10/07\",\"beginMonth\":\"\",\"endMonth\":\"\",\"latest24State\":\"\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"},{\"shareCreditLimitAmount\":\"0\",\"scheduledPaymentDate\":\"\",\"recentPayDate\":\"\",\"reportNo\":\"\",\"serialNo\":\"CARD16\",\"bizType\":\"贷记卡\",\"account\":\"84003100010480\",\"cue\":\"172016年07月04日机构“清发银行信用卡中心”发放的货记卡(美元账户),业务号84003100010480,授信额度折合人民币9.00元,其享授信膜度折合人民币0元,信用/免担保,截至216年10月07日,账户状态为“销户\",\"financeOrg\":\"浦发银行信用卡中心\",\"currency\":\"美元账户\",\"openDate\":\"2016/07/04\",\"creditLimitAmount\":\"9000\",\"guaranteeType\":\"信用/免担保\",\"state\":\"销户\",\"stateEndDate\":\"2016/10/07\",\"beginMonth\":\"\",\"endMonth\":\"\",\"latest24State\":\"\",\"badBalance\":\"0\",\"loanAcctState\":\"\",\"overdueStartDate\":\"\",\"overdueEndDate\":\"\"}],\"icrGuaranteeSummary\":{},\"icrGuarantee\":[],\"icrCardGuarantee\":[],\"icrRecordSummary\":{\"orgSum1\":\"1\",\"orgSum2\":\"0\",\"recordSum1\":\"1\",\"recordSum2\":\"0\",\"recordSumSelf\":\"0\",\"towYearRecordSum1\":\"39\",\"towYearRecordSum2\":\"0\",\"towYearRecordSum3\":\"0\"},\"icrRecordDetail\":[{\"serialNo\":\"0\",\"queryDate\":\"2018/09/25\",\"querier\":\"交通银行太平洋信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"1\",\"queryDate\":\"2018/09/19\",\"querier\":\"平安普惠融资担保有限公司\",\"queryReason\":\"贷款审批\"},{\"serialNo\":\"2\",\"queryDate\":\"2018/09/16\",\"querier\":\"深圳前海微众银行股份有限公司\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"3\",\"queryDate\":\"2018/09/02\",\"querier\":\"浦发银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"4\",\"queryDate\":\"2018/08/14\",\"querier\":\"中国平安财产保险股份有限公司\",\"queryReason\":\"保前审查\"},{\"serialNo\":\"5\",\"queryDate\":\"2018/08/13\",\"querier\":\"中国人民财产保险股份有限公司\",\"queryReason\":\"保前审查\"},{\"serialNo\":\"6\",\"queryDate\":\"2018/08/13\",\"querier\":\"阳光保险集团股份有限公司\",\"queryReason\":\"保前审查\"},{\"serialNo\":\"7\",\"queryDate\":\"2018/08/01\",\"querier\":\"平安银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"8\",\"queryDate\":\"2018/07/24\",\"querier\":\"中国平安财产保险股份有限公司\",\"queryReason\":\"保前审查\"},{\"serialNo\":\"9\",\"queryDate\":\"2018/07/18\",\"querier\":\"重庆百度小额贷款有限公司\",\"queryReason\":\"贷款审批\"},{\"serialNo\":\"10\",\"queryDate\":\"2018/06/15\",\"querier\":\"河南中原消费金融股份有限公司\",\"queryReason\":\"贷款审批\"},{\"serialNo\":\"11\",\"queryDate\":\"2018/06/15\",\"querier\":\"上海银行福民支行\",\"queryReason\":\"贷款审批\"},{\"serialNo\":\"12\",\"queryDate\":\"2018/06/06\",\"querier\":\"华夏银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"13\",\"queryDate\":\"2018/06/05\",\"querier\":\"广发银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"14\",\"queryDate\":\"2018/05/29\",\"querier\":\"浦发银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"15\",\"queryDate\":\"2018/03/17\",\"querier\":\"招商银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"16\",\"queryDate\":\"2018/02/27\",\"querier\":\"交通银行太平洋信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"17\",\"queryDate\":\"2018/02/25\",\"querier\":\"浦发银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"18\",\"queryDate\":\"2017/12/30\",\"querier\":\"中国农业银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"19\",\"queryDate\":\"2017/12/26\",\"querier\":\"深圳前海微众银行股份有限公司\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"20\",\"queryDate\":\"2017/12/23\",\"querier\":\"广发银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"21\",\"queryDate\":\"2017/12/17\",\"querier\":\"华夏银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"22\",\"queryDate\":\"2017/12/09\",\"querier\":\"浦发银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"23\",\"queryDate\":\"2017/12/01\",\"querier\":\"广州银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"24\",\"queryDate\":\"2017/09/30\",\"querier\":\"中国农业银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"25\",\"queryDate\":\"2017/09/29\",\"querier\":\"浦发银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"26\",\"queryDate\":\"2017/09/10\",\"querier\":\"广州银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"27\",\"queryDate\":\"2017/08/17\",\"querier\":\"招商银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"28\",\"queryDate\":\"2017/08/08\",\"querier\":\"南京银行股份有限公司\",\"queryReason\":\"贷款审批\"},{\"serialNo\":\"29\",\"queryDate\":\"2017/08/01\",\"querier\":\"广发银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"30\",\"queryDate\":\"2017/07/18\",\"querier\":\"平安银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"31\",\"queryDate\":\"2017/07/11\",\"querier\":\"广州银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"32\",\"queryDate\":\"2017/07/02\",\"querier\":\"兴业银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"33\",\"queryDate\":\"2017/06/30\",\"querier\":\"华夏银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"34\",\"queryDate\":\"2017/06/28\",\"querier\":\"中国农业银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"35\",\"queryDate\":\"2017/06/12\",\"querier\":\"深圳前海微众银行股份有限公司\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"36\",\"queryDate\":\"2017/06/02\",\"querier\":\"浦发银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"37\",\"queryDate\":\"2017/04/07\",\"querier\":\"交通银行太平洋信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"38\",\"queryDate\":\"2017/03/26\",\"querier\":\"中国农业银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"39\",\"queryDate\":\"2017/03/12\",\"querier\":\"浦发银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"40\",\"queryDate\":\"2017/02/17\",\"querier\":\"兴业银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"41\",\"queryDate\":\"2017/02/06\",\"querier\":\"中国平安财产保险股份有限公司\",\"queryReason\":\"保前审查\"},{\"serialNo\":\"42\",\"queryDate\":\"2017/01/24\",\"querier\":\"招商银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"43\",\"queryDate\":\"2017/01/16\",\"querier\":\"中国平安财产保险股份有限公司\",\"queryReason\":\"保前审查\"},{\"serialNo\":\"44\",\"queryDate\":\"2017/01/14\",\"querier\":\"招联消费金融有限公司\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"45\",\"queryDate\":\"2016/12/28\",\"querier\":\"广发银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"46\",\"queryDate\":\"2016/12/26\",\"querier\":\"广州银行\",\"queryReason\":\"信用卡审批\"},{\"serialNo\":\"47\",\"queryDate\":\"2016/12/23\",\"querier\":\"华夏银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"48\",\"queryDate\":\"2016/12/20\",\"querier\":\"中国农业银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"49\",\"queryDate\":\"2016/12/20\",\"querier\":\"招商银行\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"50\",\"queryDate\":\"2016/12/11\",\"querier\":\"浦发银行信用卡中心\",\"queryReason\":\"贷后管理\"},{\"serialNo\":\"51\",\"queryDate\":\"2016/11/01\",\"querier\":\"深圳前海微众银行股份有限公司\",\"queryReason\":\"贷款审批\"}],\"icrPersonRecordDetail\":[{\"serialNo\":\"0\",\"queryDate\":\"2018/07/18\",\"querier\":\"中国人民银行佛山市中心支行\",\"queryReason\":\"本人查询（临柜）\"},{\"serialNo\":\"1\",\"queryDate\":\"2018/03/26\",\"querier\":\"中国人民银行佛山市中心支行\",\"queryReason\":\"本人查询（临柜）\"}],\"icrAssetDisposition\":[],\"icrFellBackSummary\":{},\"icrAssurerRepay\":[],\"icrVehicle\":[],\"icrCompetence\":[],\"icrForceExecution\":[],\"icrAccFund\":[],\"icrEndowmentInsuranceDeposit\":[],\"icrTelPayment\":[],\"icrSalvation\":[],\"icrAdminAward\":[],\"icrAdminPunishment\":[],\"icrTaxArrear\":[],\"icrCivilJudgement\":[],\"icrEndowmentInsuranceDeliver\":[]}";


        //映射关系


//        long start1 = System.currentTimeMillis();
//        List<String> list1 = compareEntitys(str1, str2, Arrays.asList(relEntity11,relEntity21,relEntity31,relEntity41,relEntity51,relEntity61));
//        System.out.println("耗时:" + (System.currentTimeMillis() - start1));
//        System.out.println("一共" + list1.size() + "条差异！");
//        for (String str : list1) {
//            System.out.println("结果:" + str);
//        }

        //映射关系
        RelEntity relEntity_1 = new RelEntity("001001.line1.reportno", "报告编号", "reportBaseInfo.reportNo", "报告的基本信息.报告编号");
        RelEntity relEntity_2 = new RelEntity("001001.line1.querytime", "查询请求时间", "reportBaseInfo.queryTime", "报告的基本信息.查询请求时间");
        RelEntity relEntity_3 = new RelEntity("001001.line1.reportcreatetime", "报告时间", "reportBaseInfo.reportCreateTime", "报告的基本信息.报告获取时间");
        RelEntity relEntity_4 = new RelEntity("001002.line1.customername", "被查询者姓名", "reportBaseInfo.name", "报告的基本信息.被查询人名称");
        RelEntity relEntity_5 = new RelEntity("001002.line1.certtype", "被查询者证件类型", "reportBaseInfo.certType", "报告的基本信息.被查询人证件类型");
        RelEntity relEntity_6 = new RelEntity("001002.line1.certno", "被查询者证件号码", "reportBaseInfo.certNo", "报告的基本信息.被查询人证件号码");
        RelEntity relEntity_7 = new RelEntity("001003.line1.operateuser", "查询操作员", "reportBaseInfo.operateUser", "报告的基本信息.查询操作人");
        RelEntity relEntity_8 = new RelEntity("001003.line1.operateorg", "查询操作机构", "reportBaseInfo.operateOrg", "报告的基本信息.查询操作机构");
        RelEntity relEntity_9 = new RelEntity("001003.line1.queryreason", "查询原因", "reportBaseInfo.queryReason", "报告的基本信息.查询原因");
        RelEntity relEntity_10 = new RelEntity("002001.line1.gender", "性别", "icrIdentity.gender", "身份信息.性别");
        RelEntity relEntity_11 = new RelEntity("002001.line1.birthday", "出生日期", "icrIdentity.birthday", "身份信息.出生日期");
        RelEntity relEntity_12 = new RelEntity("002001.line1.maritalstate", "婚姻状况", "icrIdentity.maritalState", "身份信息.婚姻状况");
        RelEntity relEntity_13 = new RelEntity("002001.line1.mobile", "手机号码", "icrIdentity.mobile", "身份信息.手机号码");
        RelEntity relEntity_14 = new RelEntity("002001.line3.officetelephoneno", "单位电话", "icrIdentity.officetelePhoneno", "身份信息.单位电话");
        RelEntity relEntity_15 = new RelEntity("002001.line3.hometelephoneno", "住宅电话", "icrIdentity.hometelePhoneno", "身份信息.住宅电话");
        RelEntity relEntity_16 = new RelEntity("002001.line3.edulevel", "学历", "icrIdentity.edulevel", "身份信息.学历");
        RelEntity relEntity_17 = new RelEntity("002001.line3.edudegree", "学位", "icrIdentity.edudegree", "身份信息.学位");
        RelEntity relEntity_18 = new RelEntity("002002.line1.name", "姓名", "icrSpouse.name#serialNo", "配偶信息.姓名");
        RelEntity relEntity_19 = new RelEntity("002002.line1.certtype", "证件类型", "icrSpouse.certType#serialNo", "配偶信息.证件类型");
        RelEntity relEntity_20 = new RelEntity("002002.line1.certno", "证件号码", "icrSpouse.certNo#serialNo", "配偶信息.证件号码");
        RelEntity relEntity_21 = new RelEntity("002003.line1.employer", "工作单位", "icrSpouse.employer#serialNo", "配偶信息.工作单位");
        RelEntity relEntity_22 = new RelEntity("002003.line1.telephoneno", "联系电话", "icrSpouse.telephoneNo#serialNo", "配偶信息.联系电话");
        RelEntity relEntity_23 = new RelEntity("002005.line1.address#orderno", "居住地址", "icrResidence.address#serialNo", "居住信息.居住地址");
        RelEntity relEntity_24 = new RelEntity("002005.line1.residencetype#orderno", "居住状况", "icrResidence.residenceType#serialNo", "居住信息.居住状况");
        RelEntity relEntity_25 = new RelEntity("002005.line1.gettime#orderno", "信息更新日期", "icrResidence.getTime#serialNo", "居住信息.信息更新日期");
        RelEntity relEntity_26 = new RelEntity("003001.line1.houseloancount", "个人住房贷款笔数", "icrCreditCue.houseLoanCount#serialNo", "信用提示.住房贷款笔数");
        RelEntity relEntity_27 = new RelEntity("003001.line1.houseloan2count", "个人商用房（包括商住两用）贷款笔数", "icrCreditCue.houseLoan2Count#serialNo", "信用提示.个人商用房（包括商住两用）贷款笔数");
        RelEntity relEntity_28 = new RelEntity("003001.line1.otherloancount", "其他贷款笔数", "icrCreditCue.otherLoanCount#serialNo", "信用提示.其他贷款笔数");
        RelEntity relEntity_29 = new RelEntity("003001.line1.firstloanopenmonth", "首笔贷款月份", "icrCreditCue.firstLoanOpenMonth#serialNo", "信用提示.首笔贷款发放月份");
        RelEntity relEntity_30 = new RelEntity("003001.line1.loancardcount", "贷记卡账户数", "icrCreditCue.loanCardCount#serialNo", "信用提示.贷记卡账户数");
        RelEntity relEntity_31 = new RelEntity("003001.line1.firstloancardopenmonth", "首张贷记卡发卡月份", "icrCreditCue.firstLoanCardOpenMonth#serialNo", "信用提示.首张贷记卡发卡月份");
        RelEntity relEntity_32 = new RelEntity("003001.line1.standardloancardcount", "准贷卡账户数", "icrCreditCue.standardLoanCardCount#serialNo", "信用提示.准贷记卡账户数");
        RelEntity relEntity_33 = new RelEntity("003001.line1.firststandardloancardopenmonth", "首张准贷记卡发卡月份", "icrCreditCue.firstStandardLoanCardOpenMonth#serialNo", "信用提示.首张准贷记卡发卡月份");
        RelEntity relEntity_34 = new RelEntity("003001.line1.announcecount", "本人声明数目", "icrCreditCue.announceCount#serialNo", "信用提示.本人声明数目");
        RelEntity relEntity_35 = new RelEntity("003001.line1.dissentcount", "异议标注数目", "icrCreditCue.dissentCount#serialNo", "信用提示.异议标注数目");
        RelEntity relEntity_36 = new RelEntity("003002.line1.count", "贷款逾期-笔数", "icrOverdueSummary.count", "逾期(透支)信息汇总.贷款逾期笔数");
        RelEntity relEntity_37 = new RelEntity("003002.line1.count2", "贷记卡逾期-账户数", "icrOverdueSummary.count2", "逾期(透支)信息汇总.贷记卡逾期笔数");
        RelEntity relEntity_38 = new RelEntity("003002.line1.count3", "准贷记卡60天以上透支-账户数", "icrOverdueSummary.count3", "逾期(透支)信息汇总.准贷记卡60天以上透支笔数");
        RelEntity relEntity_39 = new RelEntity("003003.line1.financecorpcount", "发卡法人机构数", "icrUnpaidLoan.financeCorpCount", "未结清贷款信息汇总.贷款法人机构数");
        RelEntity relEntity_40 = new RelEntity("003003.line1.financeorgcount", "发卡机构数", "icrUnpaidLoan.financeOrgCount", "未结清贷款信息汇总.贷款机构数");
        RelEntity relEntity_41 = new RelEntity("003003.line1.accountcount", "账户数", "icrUnpaidLoan.accountCount", "未结清贷款信息汇总.笔数");
        RelEntity relEntity_42 = new RelEntity("003003.line1.creditlimit", "授信总额", "icrUnpaidLoan.creditLimit", "未结清贷款信息汇总.合同总额");
        RelEntity relEntity_43 = new RelEntity("003003.line1.balance", "担保本金余额", "icrUnpaidLoan.balance", "未结清贷款信息汇总.余额");
        RelEntity relEntity_44 = new RelEntity("003003.line1.latest6monthusedavgamount", "最近6个月平均透支额度", "icrUnpaidLoan.latest6MonthUseDavgAmount", "未结清贷款信息汇总.最近6个月平均应还款");
        RelEntity relEntity_45 = new RelEntity("004001.line1.latestrepaydate#orderno", "最近一次还款日期", "icrAssurerRepay.latestRepayDate#serialNo", "保证人代偿信息.最近一次还款日期");
        RelEntity relEntity_46 = new RelEntity("004002.line1.orderno#orderno", "编号", "icrAssurerRepay.serialNo#serialNo", "保证人代偿信息.流水号");
        RelEntity relEntity_47 = new RelEntity("004002.line1.organname#orderno", "代偿机构", "icrAssurerRepay.organName#serialNo", "保证人代偿信息.代偿机构");
        RelEntity relEntity_48 = new RelEntity("004002.line1.latestassurerrepaydate#orderno", "最近一次代偿日期", "icrAssurerRepay.latestAssurerRepayDate#serialNo", "保证人代偿信息.最近一次代偿日期");
        RelEntity relEntity_49 = new RelEntity("004002.line1.money#orderno", "累计代偿金额", "icrAssurerRepay.money#serialNo", "保证人代偿信息.累计代偿金额");
        RelEntity relEntity_50 = new RelEntity("004002.line1.latestrepaydate#orderno", "最近一次还款日期", "icrAssurerRepay.latestRepayDate#serialNo", "保证人代偿信息.最近一次还款日期");
        RelEntity relEntity_51 = new RelEntity("004002.line1.balance#orderno", "余额", "icrAssurerRepay.balance#serialNo", "保证人代偿信息.余额");
        RelEntity relEntity_52 = new RelEntity("004003.line1.orderno#orderno", "序号", "icrLoanInfo.serialNo#serialNo", "贷款信息.流水号");
        RelEntity relEntity_53 = new RelEntity("004003.line1.opendate#orderno", "发放日期", "icrLoanInfo.openDate#serialNo", "贷款信息.发放日期");
        RelEntity relEntity_54 = new RelEntity("004003.line1.financeorg#orderno", "发放机构", "icrLoanInfo.financeorg#serialNo", "贷款信息.贷款机构");
        RelEntity relEntity_55 = new RelEntity("004003.line1.creditlimitamount#orderno", "发放金额", "icrLoanInfo.creditLimitAmount#serialNo", "贷款信息.合同金额");
        RelEntity relEntity_56 = new RelEntity("004003.line1.currency#orderno", "币种", "icrLoanInfo.currency#serialNo", "贷款信息.币种");
        RelEntity relEntity_57 = new RelEntity("004003.line1.type#orderno", "特殊交易类型", "icrLoanInfo.type#serialNo", "贷款信息.贷款种类细分");
        RelEntity relEntity_58 = new RelEntity("004003.line1.account#orderno", "业务号", "icrLoanInfo.account#serialNo", "贷款信息.业务号");
        RelEntity relEntity_59 = new RelEntity("004003.line1.guaranteetype#orderno", "担保方式", "icrLoanInfo.guaranteeType#serialNo", "贷款信息.担保方式");
        RelEntity relEntity_60 = new RelEntity("004003.line1.paymentcyc#orderno", "期数<或有>", "icrLoanInfo.payMentCyc#serialNo", "贷款信息.还款期数");
        RelEntity relEntity_61 = new RelEntity("004003.line1.paymentrating#orderno", "还款方式", "icrLoanInfo.payMentRating#serialNo", "贷款信息.还款频率");
        RelEntity relEntity_62 = new RelEntity("004003.line1.enddate#orderno", "到期时间", "icrLoanInfo.endDate#serialNo", "贷款信息.到期日期");
        RelEntity relEntity_63 = new RelEntity("004003.line1.stateenddate#orderno", "报告截止日期", "icrLoanInfo.stateendDate#serialNo", "贷款信息.状态截止日");
        RelEntity relEntity_64 = new RelEntity("004003.line1.state#orderno", "账户状态<或有>", "icrLoanInfo.state#serialNo", "贷款信息.账户状态");
        RelEntity relEntity_65 = new RelEntity("004003.line1.badbalance#orderno", "余额<或有>", "icrLoanInfo.badBalance#serialNo", "贷款信息.坏账");
        RelEntity relEntity_66 = new RelEntity("004003.line2.loanacctstate#orderno", "账户状态", "icrLoanInfo.loanAcctState#serialNo", "贷款信息.帐户状态");
        RelEntity relEntity_67 = new RelEntity("004003.line2.class5state#orderno", "五级分类", "icrLoanInfo.class5State#serialNo", "贷款信息.五级分类");
        RelEntity relEntity_68 = new RelEntity("004003.line2.balance#orderno", "本金余额", "icrLoanInfo.balance#serialNo", "贷款信息.本金余额");
        RelEntity relEntity_69 = new RelEntity("004003.line2.remainpaymentcyc#orderno", "剩余还款期数", "icrLoanInfo.remainPayMentcyc#serialNo", "贷款信息.剩余还款期数");
        RelEntity relEntity_70 = new RelEntity("004003.line2.scheduledpaymentamount#orderno", "本月应还款", "icrLoanInfo.scheduledPayMentAmount#serialNo", "贷款信息.本月应还款");
        RelEntity relEntity_71 = new RelEntity("004003.line2.scheduledpaymentdate#orderno", "应还款日", "icrLoanInfo.scheduledPayMentDate#serialNo", "贷款信息.应还款日");
        RelEntity relEntity_72 = new RelEntity("004003.line2.actualpaymentamount#orderno", "本月实还款", "icrLoanInfo.actualPayMentAmount#serialNo", "贷款信息.本月实还款");
        RelEntity relEntity_73 = new RelEntity("004003.line2.recentpaydate#orderno", "最近一次还款日", "icrLoanInfo.recentPayDate#serialNo", "贷款信息.最近一次还款日期");
        RelEntity relEntity_74 = new RelEntity("004003.line3.curroverduecyc#orderno", "当前逾期期数", "icrLoanInfo.currOverdueCyc#serialNo", "贷款信息.当前逾期期数");
        RelEntity relEntity_75 = new RelEntity("004003.line3.curroverdueamount#orderno", "当前逾期金额", "icrLoanInfo.currOverdueAmount#serialNo", "贷款信息.当前逾期金额");
        RelEntity relEntity_76 = new RelEntity("004003.line3.overdue31to60amount#orderno", "逾期31-60天未还本金", "icrLoanInfo.overdue31To60Amount#serialNo", "贷款信息.逾期31—60天未还本金");
        RelEntity relEntity_77 = new RelEntity("004003.line3.overdue61to90amount#orderno", "逾期61-90天未还本金", "icrLoanInfo.overdue61To90Amount#serialNo", "贷款信息.逾期61－90天未还本金");
        RelEntity relEntity_78 = new RelEntity("004003.line3.overdue91to180amount#orderno", "逾期91-180天未还本金", "icrLoanInfo.overdue91To180Amount#serialNo", "贷款信息.逾期91－180天未还本金");
        RelEntity relEntity_79 = new RelEntity("004003.line3.overdueover180amount#orderno", "逾期180天以上未还本金", "icrLoanInfo.overdueOver180Amount#serialNo", "贷款信息.逾期180天以上未还本金");
        RelEntity relEntity_80 = new RelEntity("004003.line4.beginmonth#orderno", "还款记录起始日期", "icrLoanInfo.beginMonth#serialNo", "贷款信息.还款起始月");
        RelEntity relEntity_81 = new RelEntity("004003.line4.endmonth#orderno", "还款记录截止日期", "icrLoanInfo.endMonth#serialNo", "贷款信息.还款截止月");
        RelEntity relEntity_82 = new RelEntity("004003.line5.latest24state#orderno", "还款记录（24格）", "icrLoanInfo.latest24State#serialNo", "贷款信息.24个月还款状态");
        RelEntity relEntity_83 = new RelEntity("004003.line7.month#orderno", "逾期月份", "icrLatest5yearOverdueDetail.month#serialNo", "逾期记录明细.逾期月份");
        RelEntity relEntity_84 = new RelEntity("004003.line7.lastmonths#orderno", "逾期持续月份", "icrLatest5yearOverdueDetail.lastMonths#serialNo", "逾期记录明细.逾期持续月数");
        RelEntity relEntity_85 = new RelEntity("004003.line7.amount#orderno", "逾期金额", "icrLatest5yearOverdueDetail.amount#serialNo", "逾期记录明细.逾期金额");
        RelEntity relEntity_86 = new RelEntity("004003.line8.orderno#orderno", "序号", "icrLatest5yearOverdueDetail.serialNo#serialNo", "逾期记录明细.流水号");
        RelEntity relEntity_87 = new RelEntity("004003.line8.type#orderno", "特殊交易类型", "icrSpecialTrade.type#serialNo", "贷款特殊信息.特殊交易类型");
        RelEntity relEntity_88 = new RelEntity("004003.line8.gettime#orderno", "特殊交易-发生日期", "icrSpecialTrade.getTime#serialNo", "贷款特殊信息.发生日期");
        RelEntity relEntity_89 = new RelEntity("004003.line8.changingmonths#orderno", "特殊交易-变更月数", "icrSpecialTrade.changingMonths#serialNo", "贷款特殊信息.变更月数");
        RelEntity relEntity_90 = new RelEntity("004003.line8.changingamount#orderno", "特殊交易-发生金额", "icrSpecialTrade.changingAmount#serialNo", "贷款特殊信息.发生金额");
        RelEntity relEntity_91 = new RelEntity("004003.line8.content#orderno", "特殊交易-明细记录", "icrSpecialTrade.content#serialNo", "贷款特殊信息.明细记录");
        RelEntity relEntity_92 = new RelEntity("004004.line1.currency#orderno", "币种", "icrLoanCardInfo.currency#serialNo", "信用卡信息.币种");
        RelEntity relEntity_93 = new RelEntity("004004.line1.account#orderno", "业务号", "icrLoanCardInfo.account#serialNo", "信用卡信息.业务号");
        RelEntity relEntity_94 = new RelEntity("004004.line1.creditlimitamount#orderno", "授信额度", "icrLoanCardInfo.creditLimitAmount#serialNo", "信用卡信息.授信额度");
        RelEntity relEntity_95 = new RelEntity("004004.line1.sharecreditlimitamount#orderno", "共享授信额度", "icrLoanCardInfo.shareCreditLimitAmount#serialNo", "信用卡信息.共享额度");
        RelEntity relEntity_96 = new RelEntity("004004.line1.guaranteetype#orderno", "担保方式", "icrLoanCardInfo.guaranteeType#serialNo", "信用卡信息.担保方式");
        RelEntity relEntity_97 = new RelEntity("004004.line1.stateenddate#orderno", "报告截止日期", "icrLoanCardInfo.stateEndDate#serialNo", "信用卡信息.状态截止日");
        RelEntity relEntity_98 = new RelEntity("004004.line1.state#orderno", "账户状态<或有>", "icrLoanCardInfo.state#serialNo", "信用卡信息.帐户状态");
        RelEntity relEntity_99 = new RelEntity("004004.line1.badbalance#orderno", "余额<或有>", "icrLoanCardInfo.balance#serialNo", "信用卡信息.余额");
        RelEntity relEntity_100 = new RelEntity("004004.line2.loanacctstate#orderno", "账户状态", "icrLoanCardInfo.loanAcctState#serialNo", "信用卡信息.帐户状态");
        RelEntity relEntity_101 = new RelEntity("004004.line2.usedcreditlimitamount#orderno", "已用额度", "icrLoanCardInfo.usedCreditLimitAmount#serialNo", "信用卡信息.已用额度");
        RelEntity relEntity_102 = new RelEntity("004004.line2.latest6monthusedavgamount#orderno", "最近6个月平均使用额度", "icrLoanCardInfo.latest6MonthUsedAvgAmount#serialNo", "信用卡信息.最近6个月平均使用额度");
        RelEntity relEntity_103 = new RelEntity("004004.line2.usedhighestamount#orderno", "最大使用额度", "icrLoanCardInfo.usedHighestAmount#serialNo", "信用卡信息.最大使用额度");
        RelEntity relEntity_104 = new RelEntity("004004.line2.scheduledpaymentamount#orderno", "本月应还款", "icrLoanCardInfo.scheduledPaymentAmount#serialNo", "信用卡信息.本月应还款");
        RelEntity relEntity_105 = new RelEntity("004004.line3.scheduledpaymentdate#orderno", "账单日", "icrLoanCardInfo.scheduledPaymentDate#serialNo", "信用卡信息.账单日");
        RelEntity relEntity_106 = new RelEntity("004004.line3.actualpaymentamount#orderno", "本月实还款", "icrLoanCardInfo.actualPaymentAmount#serialNo", "信用卡信息.本月实还款");
        RelEntity relEntity_107 = new RelEntity("004004.line3.recentpaydate#orderno", "最近一次还款日期", "icrLoanCardInfo.recentPayDate#serialNo", "信用卡信息.最近一次还款日期");
        RelEntity relEntity_108 = new RelEntity("004004.line3.curroverduecyc#orderno", "当前逾期期数", "icrLoanCardInfo.currOverdueCyc#serialNo", "信用卡信息.当前逾期期数");
        RelEntity relEntity_109 = new RelEntity("004004.line3.curroverdueamount#orderno", "当前逾期金额", "icrLoanCardInfo.currOverdueAmount#serialNo", "信用卡信息.当前逾期金额");
        RelEntity relEntity_110 = new RelEntity("004004.line4.beginmonth#orderno", "还款记录起始日期", "icrLoanCardInfo.beginMonth#serialNo", "信用卡信息.还款起始月");
        RelEntity relEntity_111 = new RelEntity("004004.line4.endmonth#orderno", "还款记录截止日期", "icrLoanCardInfo.endMonth#serialNo", "信用卡信息.还款截止月");
        RelEntity relEntity_112 = new RelEntity("004004.line5.latest24state#ordern", "还款记录（24格）", "icrLoanCardInfo.latest24State#serialNo", "信用卡信息.24个月还款状态");
        RelEntity relEntity_113 = new RelEntity("004004.line7.lastmonths", "逾期持续月数", "icrLatest5yearOverdueDetail.lastMonths#serialNo", "逾期记录明细.逾期持续月数");
        RelEntity relEntity_114 = new RelEntity("004004.line8.changingmonths", "变更月数", "icrSpecialTrade.changingMonths#serialNo", "贷款特殊信息.变更月数");
        RelEntity relEntity_115 = new RelEntity("004004.line8.changingamount", "发生金额", "icrSpecialTrade.changingAmount#serialNo", "贷款特殊信息.发生金额");
        RelEntity relEntity_116 = new RelEntity("004005.line2.usedcreditlimitamount", "透支余额", "icrLoanCardInfo.usedCreditLimitAmount#serialNo", "信用卡信息.已用额度");
        RelEntity relEntity_117 = new RelEntity("004005.line2.usedhighestamount", "最大透支余额", "icrLoanCardInfo.usedHighestAmount#serialNo", "信用卡信息.最大使用额度");
        RelEntity relEntity_118 = new RelEntity("004005.line6.lastmonths", "透支持续月数", "icrLatest5yearOverdueDetail.lastMonths#serialNo", "逾期记录明细.逾期持续月数");
        RelEntity relEntity_119 = new RelEntity("004006.line1.organname", "担保信用卡发放机构", "icrCardGuarantee.organName#serialNo", "对外信用卡担保信息.担保信用卡发放机构");
        RelEntity relEntity_120 = new RelEntity("004006.line1.contractmoney", "担保信用卡授信额度", "icrGuarantee.contractMoney#serialNo", "对外贷款担保信息.担保贷款合同金额");
        RelEntity relEntity_121 = new RelEntity("004006.line1.begindate", "担保信用卡发卡日期", "icrCardGuarantee.beginDate#serialNo", "对外信用卡担保信息.担保信用卡发卡日期");
        RelEntity relEntity_122 = new RelEntity("004006.line1.guananteemoney", "担保金额", "icrCardGuarantee.guananteeMoney#serialNo", "对外信用卡担保信息.担保金额");
        RelEntity relEntity_123 = new RelEntity("004006.line1.guaranteebalance", "担保信用卡已用额度", "icrGuarantee.guaranteeBalance#serialNo", "对外贷款担保信息.担保贷款本金余额");
        RelEntity relEntity_124 = new RelEntity("004006.line1.class5state", "担保贷款五级分类", "icrGuarantee.class5State#serialNo", "对外贷款担保信息.担保贷款五级分类");
        RelEntity relEntity_125 = new RelEntity("004006.line1.billingdate", "账单日", "icrCardGuarantee.billingDate#serialNo", "对外信用卡担保信息.账单日");
        RelEntity relEntity_126 = new RelEntity("005001.line1.orderno#orderno", "编号", "icrTaxArrear.serialNo#serialNo", "欠税记录.流水号");
        RelEntity relEntity_127 = new RelEntity("005001.line1.organname#orderno", "主管税务机关", "icrTaxArrear.organName#serialNo", "欠税记录.主管税务机关");
        RelEntity relEntity_128 = new RelEntity("005001.line1.revenuedate#orderno", "欠税统计时间", "icrTaxArrear.revenueDate#serialNo", "欠税记录.欠税统计日期");
        RelEntity relEntity_129 = new RelEntity("005001.line1.taxarreaamount#orderno", "欠税总额", "icrTaxArrear.taxArreaAmount#serialNo", "欠税记录.欠税总额");
        RelEntity relEntity_130 = new RelEntity("005002.line1.orderno#orderno", "编号", "icrCivilJudgement.serialNo#serialNo", "民事判决记录.流水号");
        RelEntity relEntity_131 = new RelEntity("005002.line1.court#orderno", "立案法院", "icrCivilJudgement.court#serialNo", "民事判决记录.立案法院");
        RelEntity relEntity_132 = new RelEntity("005002.line1.casereason#orderno", "案由", "icrCivilJudgement.caseReason#serialNo", "民事判决记录.案由");
        RelEntity relEntity_133 = new RelEntity("005002.line1.registerdate#orderno", "立案日期", "icrCivilJudgement.registerDate#serialNo", "民事判决记录.立案日期");
        RelEntity relEntity_134 = new RelEntity("005002.line1.closedtype#orderno", "结案方式", "icrCivilJudgement.closedType#serialNo", "民事判决记录.结案方式");
        RelEntity relEntity_135 = new RelEntity("005002.line2.orderno#orderno", "编号", "icrCivilJudgement.serialNo#serialNo", "民事判决记录.流水号");
        RelEntity relEntity_136 = new RelEntity("005002.line2.caseresult#orderno", "判决_调解结果", "icrCivilJudgement.caseResult#serialNo", "民事判决记录.判决_调解结果");
        RelEntity relEntity_137 = new RelEntity("005002.line2.casevalidatedate#orderno", "判决_调解生效日期", "icrCivilJudgement.caseValidateDate#serialNo", "民事判决记录.判决_调解生效日期");
        RelEntity relEntity_138 = new RelEntity("005002.line2.suitobject#orderno", "诉讼标的", "icrCivilJudgement.suitObject#serialNo", "民事判决记录.诉讼标的");
        RelEntity relEntity_139 = new RelEntity("005002.line2.suitobjectmoney#orderno", "诉讼标的金额", "icrCivilJudgement.suitObjectMoney#serialNo", "民事判决记录.诉讼标的金额");
        RelEntity relEntity_140 = new RelEntity("005003.line1.orderno#orderno", "编号", "icrForceExecution.serialNo#serialNo", "强制执行记录.流水号");
        RelEntity relEntity_141 = new RelEntity("005003.line1.court#orderno", "执行法院", "icrForceExecution.court#serialNo", "强制执行记录.执行法院");
        RelEntity relEntity_142 = new RelEntity("005003.line1.casereason#orderno", "执行案由", "icrForceExecution.caseReason#serialNo", "强制执行记录.执行案由");
        RelEntity relEntity_143 = new RelEntity("005003.line1.registerdate#orderno", "立案日期", "icrForceExecution.registerDate#serialNo", "强制执行记录.立案日期");
        RelEntity relEntity_144 = new RelEntity("005003.line1.closedtype#orderno", "结案方式", "icrForceExecution.closedType#serialNo", "强制执行记录.结案方式");
        RelEntity relEntity_145 = new RelEntity("005003.line2.orderno#orderno", "编号", "icrForceExecution.serialNo#serialNo", "强制执行记录.流水号");
        RelEntity relEntity_146 = new RelEntity("005003.line2.casestate#orderno", "案件状态", "icrForceExecution.casesTate#serialNo", "强制执行记录.案件状态");
        RelEntity relEntity_147 = new RelEntity("005003.line2.closeddate#orderno", "结案日期", "icrForceExecution.closedDate#serialNo", "强制执行记录.结案日期");
        RelEntity relEntity_148 = new RelEntity("005003.line2.enforceobject#orderno", "申请执行标的", "icrForceExecution.enforceBigDecimal#serialNo", "强制执行记录.申请执行标的");
        RelEntity relEntity_149 = new RelEntity("005003.line2.enforceobjectmoney#orderno", "申请执行标的价值", "icrForceExecution.enforceBigDecimalMoney#serialNo", "强制执行记录.申请执行标的价值");
        RelEntity relEntity_150 = new RelEntity("005003.line2.alreadyenforceobject#orderno", "已执行标的", "icrForceExecution.alreadyEnforceBigDecimal#serialNo", "强制执行记录.已执行标的");
        RelEntity relEntity_151 = new RelEntity("005003.line2.alreadyenforceobjectmoney#orderno", "已执行标的金额", "icrForceExecution.alreadyEnforceBigDecimalMoney#serialNo", "强制执行记录.已执行标的金额");
        RelEntity relEntity_152 = new RelEntity("005004.line1.orderno#orderno", "null", "icrAdminPunishment.serialNo#serialNo", "行政处罚记录.流水号");
        RelEntity relEntity_153 = new RelEntity("005004.line1.organname#orderno", "处罚机构", "icrAdminPunishment.organName#serialNo", "行政处罚记录.处罚机构");
        RelEntity relEntity_154 = new RelEntity("005004.line1.content#orderno", "处罚内容", "icrAdminPunishment.content#serialNo", "行政处罚记录.处罚内容");
        RelEntity relEntity_155 = new RelEntity("005004.line1.money#orderno", "处罚金额", "icrAdminPunishment.money#serialNo", "行政处罚记录.处罚金额");
        RelEntity relEntity_156 = new RelEntity("005004.line1.begindate#orderno", "生效时间", "icrAdminAward.beginDate#serialNo", "行政奖励记录.生效日期");
        RelEntity relEntity_157 = new RelEntity("005004.line1.enddate#orderno", "截止日期", "icrAdminPunishment.endDate#serialNo", "行政处罚记录.截止日期");
        RelEntity relEntity_158 = new RelEntity("005004.line1.result#orderno", "行政复议结果", "icrAdminPunishment.result#serialNo", "行政处罚记录.行政复议结果");
        RelEntity relEntity_159 = new RelEntity("005005.line1.orderno#orderno", "编号", "icrAccFund.serialNo#serialNo", "住房公积金参缴记录.流水号");
        RelEntity relEntity_160 = new RelEntity("005005.line1.area#orderno", "参缴地", "icrAccFund.area#serialNo", "住房公积金参缴记录.参缴地");
        RelEntity relEntity_161 = new RelEntity("005005.line1.registerdate#orderno", "参缴日期", "icrAccFund.registerDate#serialNo", "住房公积金参缴记录.参缴日期");
        RelEntity relEntity_162 = new RelEntity("005005.line1.firstmonth#orderno", "初缴月份", "icrAccFund.firstMonth#serialNo", "住房公积金参缴记录.初缴月份");
        RelEntity relEntity_163 = new RelEntity("005005.line1.tomonth#orderno", "缴至月份", "icrAccFund.toMonth#serialNo", "住房公积金参缴记录.缴至月份");
        RelEntity relEntity_164 = new RelEntity("005005.line1.state#orderno", "缴费状态", "icrAccFund.state#serialNo", "住房公积金参缴记录.缴费状态");
        RelEntity relEntity_165 = new RelEntity("005005.line1.pay#orderno", "月缴存额", "icrAccFund.pay#serialNo", "住房公积金参缴记录.月缴存额");
        RelEntity relEntity_166 = new RelEntity("005005.line1.ownpercent#orderno", "个人缴存比例", "icrAccFund.ownPercent#serialNo", "住房公积金参缴记录.个人缴存比例");
        RelEntity relEntity_167 = new RelEntity("005005.line1.compercent#orderno", "单位缴存比例", "icrAccFund.comPercent#serialNo", "住房公积金参缴记录.单位缴存比例");
        RelEntity relEntity_168 = new RelEntity("005005.line2.orderno#orderno", "编号", "icrAccFund.serialNo#serialNo", "住房公积金参缴记录.流水号");
        RelEntity relEntity_169 = new RelEntity("005005.line2.organname#orderno", "缴费单位", "icrAccFund.organName#serialNo", "住房公积金参缴记录.缴费单位");
        RelEntity relEntity_170 = new RelEntity("005005.line2.gettime#orderno", "信息更新日期", "icrAccFund.getTime#serialNo", "住房公积金参缴记录.信息更新日期");
        RelEntity relEntity_171 = new RelEntity("005006.line1.orderno#orderno", "编号", "icrEndowmentInsuranceDeposit.serialNo#serialNo", "养老保险金缴存记录.流水号");
        RelEntity relEntity_172 = new RelEntity("005006.line1.area#orderno", "参保地", "icrEndowmentInsuranceDeposit.area#serialNo", "养老保险金缴存记录.参保地");
        RelEntity relEntity_173 = new RelEntity("005006.line1.registerdate#orderno", "参保日期", "icrEndowmentInsuranceDeposit.registerDate#serialNo", "养老保险金缴存记录.参保日期");
        RelEntity relEntity_174 = new RelEntity("005006.line1.monthduration#orderno", "累计缴费月数", "icrEndowmentInsuranceDeposit.BigDecimalmonthDuration#serialNo", "养老保险金缴存记录.累计缴费月数");
        RelEntity relEntity_175 = new RelEntity("005006.line1.workdate#orderno", "参加工作年份", "icrEndowmentInsuranceDeposit.workDate#serialNo", "养老保险金缴存记录.参加工作月份");
        RelEntity relEntity_176 = new RelEntity("005006.line1.state#orderno", "缴费状态", "icrEndowmentInsuranceDeposit.state#serialNo", "养老保险金缴存记录.缴费状态");
        RelEntity relEntity_177 = new RelEntity("005006.line1.ownbasicmoney#orderno", "个人缴费基数", "icrEndowmentInsuranceDeposit.BigDecimalownBasicMoney#serialNo", "养老保险金缴存记录.个人缴费基数");
        RelEntity relEntity_178 = new RelEntity("005006.line1.money#orderno", "本月缴费金额", "icrEndowmentInsuranceDeposit.BigDecimalmoney#serialNo", "养老保险金缴存记录.本月缴费金额");
        RelEntity relEntity_179 = new RelEntity("005006.line1.gettime#orderno", "信息更新日期", "icrEndowmentInsuranceDeposit.getTime#serialNo", "养老保险金缴存记录.信息更新日期");
        RelEntity relEntity_180 = new RelEntity("005006.line2.orderno#orderno", "编号", "icrEndowmentInsuranceDeposit.serialNo#serialNo", "养老保险金缴存记录.流水号");
        RelEntity relEntity_181 = new RelEntity("005006.line2.organname#orderno", "缴费单位", "icrEndowmentInsuranceDeposit.organName#serialNo", "养老保险金缴存记录.缴费单位");
        RelEntity relEntity_182 = new RelEntity("005006.line2.reason#orderno", "中断或终止缴费原因", "icrEndowmentInsuranceDeposit.pauseReason#serialNo", "养老保险金缴存记录.中断或终止缴费原因");
        RelEntity relEntity_183 = new RelEntity("005007.line1.orderno#orderno", "null", "icrEndowmentInsuranceDeliver.serialNo#serialNo", "养老保险金发放记录.流水号");
        RelEntity relEntity_184 = new RelEntity("005007.line1.area#orderno", "发放地", "icrEndowmentInsuranceDeliver.area#serialNo", "养老保险金发放记录.发放地");
        RelEntity relEntity_185 = new RelEntity("005007.line1.retiretype#orderno", "离退休类别", "icrEndowmentInsuranceDeliver.retireType#serialNo", "养老保险金发放记录.离退休类别");
        RelEntity relEntity_186 = new RelEntity("005007.line1.retireddate#orderno", "离退休月份", "icrEndowmentInsuranceDeliver.retiredDate#serialNo", "养老保险金发放记录.离退休月份");
        RelEntity relEntity_187 = new RelEntity("005007.line1.workdate#orderno", "参加工作月份", "icrEndowmentInsuranceDeliver.workDate#serialNo", "养老保险金发放记录.参加工作月份");
        RelEntity relEntity_188 = new RelEntity("005007.line1.money#orderno", "本月实发养老金", "icrEndowmentInsuranceDeliver.money#serialNo", "养老保险金发放记录.本月实发养老金");
        RelEntity relEntity_189 = new RelEntity("005007.line1.pausereason#orderno", "停发原因", "icrEndowmentInsuranceDeliver.pauseReason#serialNo", "养老保险金发放记录.停发原因");
        RelEntity relEntity_190 = new RelEntity("005007.line1.gettime1#orderno", "信息更新日期", "icrEndowmentInsuranceDeliver.getTime#serialNo", "养老保险金发放记录.信息更新日期");
        RelEntity relEntity_191 = new RelEntity("005008.line1.orderno#orderno", "编号", "icrSalvation.serialNo#serialNo", "低保救助记录.流水号");
        RelEntity relEntity_192 = new RelEntity("005008.line1.personneltype#orderno", "人员类别", "icrSalvation.personnelType#serialNo", "低保救助记录.人员类别");
        RelEntity relEntity_193 = new RelEntity("005008.line1.area#orderno", "所在地", "icrSalvation.area#serialNo", "低保救助记录.所在地");
        RelEntity relEntity_194 = new RelEntity("005008.line1.organname#orderno", "工作单位", "icrSalvation.organName#serialNo", "低保救助记录.工作单位");
        RelEntity relEntity_195 = new RelEntity("005008.line1.money#orderno", "家庭月收入", "icrSalvation.money#serialNo", "低保救助记录.家庭月收入");
        RelEntity relEntity_196 = new RelEntity("005008.line1.registerdate#orderno", "申请日期", "icrSalvation.registerDate#serialNo", "低保救助记录.申请日期");
        RelEntity relEntity_197 = new RelEntity("005008.line1.passdate#orderno", "批准日期", "icrSalvation.passDate#serialNo", "低保救助记录.批准日期");
        RelEntity relEntity_198 = new RelEntity("005008.line1.gettime#orderno", "信息更新日期", "icrSalvation.getTime#serialNo", "低保救助记录.信息更新日期");
        RelEntity relEntity_199 = new RelEntity("005009.line1.orderno#orderno", "编号", "icrCompetence.serialNo#serialNo", "执业资格记录.流水号");
        RelEntity relEntity_200 = new RelEntity("005009.line1.competencyname#orderno", "职业资格名称", "icrCompetence.competencyName#serialNo", "执业资格记录.执业资格名称");
        RelEntity relEntity_201 = new RelEntity("005009.line1.grade#orderno", "等级", "icrCompetence.grade#serialNo", "执业资格记录.等级");
        RelEntity relEntity_202 = new RelEntity("005009.line1.awarddate#orderno", "获得日期", "icrCompetence.awardDate#serialNo", "执业资格记录.获得日期");
        RelEntity relEntity_203 = new RelEntity("005009.line1.enddate#orderno", "到期日期", "icrCompetence.endDate#serialNo", "执业资格记录.到期日期");
        RelEntity relEntity_204 = new RelEntity("005009.line1.revokedate#orderno", "吊销日期", "icrCompetence.revokeDate#serialNo", "执业资格记录.吊销日期");
        RelEntity relEntity_205 = new RelEntity("005009.line1.organname#orderno", "颁发机构", "icrCompetence.organName#serialNo", "执业资格记录.颁发机构");
        RelEntity relEntity_206 = new RelEntity("005009.line1.area#orderno", "机构所在地", "icrCompetence.area#serialNo", "执业资格记录.机构所在地");
        RelEntity relEntity_207 = new RelEntity("005010.line1.orderno#orderno", "编号", "icrAdminAward.serialNo#serialNo", "行政奖励记录.序号");
        RelEntity relEntity_208 = new RelEntity("005010.line1.organname#orderno", "奖励机构", "icrAdminAward.organName#serialNo", "行政奖励记录.奖励机构");
        RelEntity relEntity_209 = new RelEntity("005010.line1.content#orderno", "奖励内容", "icrAdminAward.content#serialNo", "行政奖励记录.奖励内容");
        RelEntity relEntity_210 = new RelEntity("005010.line1.begindate#orderno", "生效日期", "icrAdminPunishment.beginDate#serialNo", "行政处罚记录.生效日期");
        RelEntity relEntity_211 = new RelEntity("005010.line1.enddate#orderno", "截止日期", "icrAdminAward.endDate#serialNo", "行政奖励记录.截止日期");
        RelEntity relEntity_212 = new RelEntity("005011.line1.orderno#orderno", "编号", "icrVehicle.serialNo#serialNo", "车辆交易和抵押记录.流水号");
        RelEntity relEntity_213 = new RelEntity("005011.line1.licensecode#orderno", "车牌号码", "icrVehicle.licenseCode#serialNo", "车辆交易和抵押记录.车牌号码");
        RelEntity relEntity_214 = new RelEntity("005011.line1.enginecode#orderno", "发动机号", "icrVehicle.engineCode#serialNo", "车辆交易和抵押记录.发动机号");
        RelEntity relEntity_215 = new RelEntity("005011.line1.brand#orderno", "品牌", "icrVehicle.brand#serialNo", "车辆交易和抵押记录.品牌");
        RelEntity relEntity_216 = new RelEntity("005011.line1.cartype#orderno", "车辆类型", "icrVehicle.carType#serialNo", "车辆交易和抵押记录.车辆类型");
        RelEntity relEntity_217 = new RelEntity("005011.line1.usecharacter#orderno", "使用性质", "icrVehicle.useCharacter#serialNo", "车辆交易和抵押记录.使用性质");
        RelEntity relEntity_218 = new RelEntity("005011.line1.state#orderno", "车辆状态", "icrVehicle.state#serialNo", "车辆交易和抵押记录.车辆状态");
        RelEntity relEntity_219 = new RelEntity("005011.line1.pledgeflag#orderno", "抵押标记", "icrVehicle.pledgeFlag#serialNo", "车辆交易和抵押记录.抵押标记");
        RelEntity relEntity_220 = new RelEntity("005011.line1.gettime#orderno", "信息更新日期", "icrVehicle.getTime#serialNo", "车辆交易和抵押记录.信息更新日期");
        RelEntity relEntity_221 = new RelEntity("005012.line1.orderno#orderno", "编号", "icrTelPayment.serialNo#serialNo", "电信缴费记录.流水号");
        RelEntity relEntity_222 = new RelEntity("005012.line1.companyno#orderno", "电信运营商", "icrTelPayment.organName#serialNo", "电信缴费记录.电信运营商");
        RelEntity relEntity_223 = new RelEntity("005012.line1.busitype#orderno", "业务类型", "icrTelPayment.type#serialNo", "电信缴费记录.业务类型");
        RelEntity relEntity_224 = new RelEntity("005012.line1.opendate#orderno", "业务开通时间", "icrTelPayment.registerDate#serialNo", "电信缴费记录.业务开通日期");
        RelEntity relEntity_225 = new RelEntity("005012.line1.status#orderno", "当前缴费状态", "icrTelPayment.state#serialNo", "电信缴费记录.当前缴费状态");
        RelEntity relEntity_226 = new RelEntity("005012.line1.owemoney#orderno", "当前欠费金额", "icrTelPayment.arrearMoney#serialNo", "电信缴费记录.当前欠费金额");
        RelEntity relEntity_227 = new RelEntity("005012.line1.owenum#orderno", "当前欠费月数", "icrTelPayment.arrearMonths#serialNo", "电信缴费记录.当前欠费月数");
        RelEntity relEntity_228 = new RelEntity("005012.line1.acctdate#orderno", "记账年月", "icrTelPayment.getTime#serialNo", "电信缴费记录.记账年月");
        RelEntity relEntity_229 = new RelEntity("005012.line2.orderno#orderno", "编号", "icrTelPayment.serialNo#serialNo", "电信缴费记录.流水号");
        RelEntity relEntity_230 = new RelEntity("005012.line2.pay24months#orderno", "最近24个月缴费记录", "icrTelPayment.status24#serialNo", "电信缴费记录.24个月缴费状态");
        RelEntity relEntity_231 = new RelEntity("006001.line1.orderno#orderno", "编号", "icrAnnounceInfo.serialNo#serialNo", "贷款本人声明.流水号");
        RelEntity relEntity_232 = new RelEntity("006001.line1.content#orderno", "声明内容", "icrAnnounceInfo.content#serialNo", "贷款本人声明.本人声明");
        RelEntity relEntity_233 = new RelEntity("006001.line1.gettime#orderno", "添加日期", "icrAnnounceInfo.getTime#serialNo", "贷款本人声明.添加日期");
        RelEntity relEntity_234 = new RelEntity("007001.line1.orderno#orderno", "编号", "icrDissentInfo.serialNo#serialNo", "贷款异议标注.流水号");
        RelEntity relEntity_235 = new RelEntity("007001.line1.content#orderno", "标注内容", "icrDissentInfo.content#serialNo", "贷款异议标注.异议标注");
        RelEntity relEntity_236 = new RelEntity("007001.line1.gettime#orderno", "添加日期", "icrDissentInfo.getTime#serialNo", "贷款异议标注.添加日期");
        RelEntity relEntity_237 = new RelEntity("008001.line1.orgsum1", "最近1个月内的查询机构数-贷款审批", "icrRecordSummary.orgSum1#serialNo", "查询记录汇总.最近1个月内的查询机构数(贷款审批)");
        RelEntity relEntity_238 = new RelEntity("008001.line1.orgsum2", "最近1个月内的查询机构数-信用卡审批", "icrRecordSummary.orgSum2#serialNo", "查询记录汇总.最近1个月内的查询机构数(信用卡审批)");
        RelEntity relEntity_239 = new RelEntity("008001.line1.recordsum1", "最近1个月内的查询次数-贷款审批", "icrRecordSummary.recordSum1#serialNo", "查询记录汇总.最近1个月内的查询次数(贷款审批)");
        RelEntity relEntity_240 = new RelEntity("008001.line1.recordsum2", "最近1个月内的查询次数-信用卡审批", "icrRecordSummary.recordSum2#serialNo", "查询记录汇总.最近1个月内的查询次数(信用卡审批)");
        RelEntity relEntity_241 = new RelEntity("008001.line1.recordsumself", "最近1个月内的查询次数-本人查询", "icrRecordSummary.recordSumSelf#serialNo", "查询记录汇总.最近1个月内的查询次数(本人查询次数)");
        RelEntity relEntity_242 = new RelEntity("008001.line1.towyearrecordsum1", "最近2年内的查询次数-贷后管理", "icrRecordSummary.towYearRecordSum1#serialNo", "查询记录汇总.最近2年内的查询次数(贷后管理)");
        RelEntity relEntity_243 = new RelEntity("008001.line1.towyearrecordsum2", "最近2年内的查询次数-担保资格审查", "icrRecordSummary.towYearRecordSum2#serialNo", "查询记录汇总.最近2年内的查询次数(担保资格审查)");
        RelEntity relEntity_244 = new RelEntity("008001.line1.towyearrecordsum3", "最近2年内的查询次数-特约商户实名审查", "icrRecordSummary.towYearRecordSum3#serialNo", "查询记录汇总.最近2年内的查询次数(特约商户实名审查)");
        RelEntity relEntity_245 = new RelEntity("008003.line1.orderno#orderno", "编号", "icrPersonRecordDetail.serialNo#serialNo", "个人查询记录明细.流水号");
        RelEntity relEntity_246 = new RelEntity("008003.line1.querydate#orderno", "查询日期", "icrPersonRecordDetail.queryDate#serialNo", "个人查询记录明细.查询日期");
        RelEntity relEntity_247 = new RelEntity("008003.line1.querier#orderno", "查询操作员", "icrPersonRecordDetail.querier#serialNo", "个人查询记录明细.查询操作员");
        RelEntity relEntity_248 = new RelEntity("008003.line1.queryreason#orderno", "查询原因", "icrPersonRecordDetail.queryReason#serialNo", "个人查询记录明细.查询原因");


        //5.不同数组深度对比
        //6.不同数组深度对比,要求字段排序
        long start = System.currentTimeMillis();
        List<String> list = compareEntitys(str3, str4, Arrays.asList(relEntity_1, relEntity_2, relEntity_3, relEntity_4, relEntity_5, relEntity_6, relEntity_7, relEntity_8, relEntity_9, relEntity_10, relEntity_11, relEntity_12, relEntity_13, relEntity_14, relEntity_15, relEntity_16, relEntity_17, relEntity_18, relEntity_19, relEntity_20, relEntity_21, relEntity_22, relEntity_23, relEntity_24, relEntity_25, relEntity_26, relEntity_27, relEntity_28, relEntity_29, relEntity_30, relEntity_31, relEntity_32, relEntity_33, relEntity_34, relEntity_35, relEntity_36, relEntity_37, relEntity_38, relEntity_39, relEntity_40, relEntity_41, relEntity_42, relEntity_43, relEntity_44, relEntity_45, relEntity_46, relEntity_47, relEntity_48, relEntity_49, relEntity_50, relEntity_51, relEntity_52, relEntity_53, relEntity_54, relEntity_55, relEntity_56, relEntity_57, relEntity_58, relEntity_59, relEntity_60, relEntity_61, relEntity_62, relEntity_63, relEntity_64, relEntity_65, relEntity_66, relEntity_67, relEntity_68, relEntity_69, relEntity_70, relEntity_71, relEntity_72, relEntity_73, relEntity_74, relEntity_75, relEntity_76, relEntity_77, relEntity_78, relEntity_79, relEntity_80, relEntity_81, relEntity_82, relEntity_83, relEntity_84, relEntity_85, relEntity_86, relEntity_87, relEntity_88, relEntity_89, relEntity_90, relEntity_91, relEntity_92, relEntity_93, relEntity_94, relEntity_95, relEntity_96, relEntity_97, relEntity_98, relEntity_99, relEntity_100, relEntity_101, relEntity_102, relEntity_103, relEntity_104, relEntity_105, relEntity_106, relEntity_107, relEntity_108, relEntity_109, relEntity_110, relEntity_111, relEntity_112, relEntity_113, relEntity_114, relEntity_115, relEntity_116, relEntity_117, relEntity_118, relEntity_119, relEntity_120, relEntity_121, relEntity_122, relEntity_123, relEntity_124, relEntity_125, relEntity_126, relEntity_127, relEntity_128, relEntity_129, relEntity_130, relEntity_131, relEntity_132, relEntity_133, relEntity_134, relEntity_135, relEntity_136, relEntity_137, relEntity_138, relEntity_139, relEntity_140, relEntity_141, relEntity_142, relEntity_143, relEntity_144, relEntity_145, relEntity_146, relEntity_147, relEntity_148, relEntity_149, relEntity_150, relEntity_151, relEntity_152, relEntity_153, relEntity_154, relEntity_155, relEntity_156, relEntity_157, relEntity_158, relEntity_159, relEntity_160, relEntity_161, relEntity_162, relEntity_163, relEntity_164, relEntity_165, relEntity_166, relEntity_167, relEntity_168, relEntity_169, relEntity_170, relEntity_171, relEntity_172, relEntity_173, relEntity_174, relEntity_175, relEntity_176, relEntity_177, relEntity_178, relEntity_179, relEntity_180, relEntity_181, relEntity_182, relEntity_183, relEntity_184, relEntity_185, relEntity_186, relEntity_187, relEntity_188, relEntity_189, relEntity_190, relEntity_191, relEntity_192, relEntity_193, relEntity_194, relEntity_195, relEntity_196, relEntity_197, relEntity_198, relEntity_199, relEntity_200, relEntity_201, relEntity_202, relEntity_203, relEntity_204, relEntity_205, relEntity_206, relEntity_207, relEntity_208, relEntity_209, relEntity_210, relEntity_211, relEntity_212, relEntity_213, relEntity_214, relEntity_215, relEntity_216, relEntity_217, relEntity_218, relEntity_219, relEntity_220, relEntity_221, relEntity_222, relEntity_223, relEntity_224, relEntity_225, relEntity_226, relEntity_227, relEntity_228, relEntity_229, relEntity_230, relEntity_231, relEntity_232, relEntity_233, relEntity_234, relEntity_235, relEntity_236, relEntity_237, relEntity_238, relEntity_239, relEntity_240, relEntity_241, relEntity_242, relEntity_243, relEntity_244, relEntity_245, relEntity_246, relEntity_247, relEntity_248));
        System.out.println("耗时:" + (System.currentTimeMillis() - start));
        System.out.println("一共" + list.size() + "条差异！");
        for (String str : list) {
            System.out.println(str);
        }


//        RelEntity relEntity_89_ = new RelEntity("004003.line8.changingmonths#orderno", "特殊交易-变更月数", "icrSpecialTrade.changingMonths#serialNo", "贷款特殊信息.变更月数");
//        List<String> list1 = compareEntitys(str3, str4, Arrays.asList(relEntity_89_));
//        System.out.println("耗时:" + (System.currentTimeMillis() - start));
//        System.out.println("一共" + list1.size() + "条差异！");
//        for (String str : list1) {
//            System.out.println(str);
//        }


    }


    //todo 1.时间的对比
/*//        yyyy-MM-dd HH:mm:ss
    LocalDateTime localDateTime =
            LocalDateTime.parse("2018/10/1111:54:00", DateTimeFormatter.ofPattern("yyyy/MM/ddHH:mm:ss"));//授信时间
    LocalDateTime localDateTime1 =
            LocalDateTime.parse("2018-10-11 11:54:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));//授信时间
        System.out.println(localDateTime);
        System.out.println(localDateTime1);
        if (localDateTime.equals(localDateTime1)) {
        System.out.println("=====");
    }
    String regex = "[1-9]{1}[0-9]{3}([-./])\\d{1,2}\\1\\d{1,2}\\s?\\d{1,2}[:]\\d{1,2}[:]\\d{1,2}";
    String regex1 = "[1-9]{1}[0-9]{3}([-./])(\\d{1,2})\\1\\2\\s?\\2([:])\\2\\3\\2";
        System.out.println("2018-10-11 11:54:00".matches(regex1));
        System.out.println("1234\t234567\tqwerty");*/


}
