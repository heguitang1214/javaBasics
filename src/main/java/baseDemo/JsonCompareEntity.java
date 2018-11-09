package baseDemo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.math.BigInteger;
import java.util.*;


/**
 * 利用Json比较实体的内容
 */
public class JsonCompareEntity {

    /**
     *  比较json内容
     * @param beforeJson json1
     * @param afterJson json1
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
                                  LinkedList<String> befLinkedList, LinkedList<String> aftLinkedList,
                                  RelEntity relEntity, List<String> resultList) {
        String beforeKey = befLinkedList.getFirst();
        String afterKey = aftLinkedList.getFirst();
        if (beforeData instanceof JSONObject) {
            if (afterData instanceof JSONObject) {
                //todo 层级不相等的情况
                befLinkedList.removeFirst();
                aftLinkedList.removeFirst();
                if (befLinkedList.size() == 0) {
                    Object beforeObject = ((JSONObject) beforeData).get(beforeKey);
                    Object o1 = ((JSONObject) afterData).get(afterKey);
                    if (beforeObject instanceof BigInteger || o1 instanceof BigInteger){
                        System.out.println("BigInteger" + beforeObject);
                    }else if (beforeObject instanceof String && o1 instanceof String){
                        if (!beforeObject.equals(o1)) {
                            resultList.add(relEntity.getPartyADesc() + "是[" + beforeObject + "]," + relEntity.getPartyBDesc() + "是[" + o1 + "]");
                        }
                    }else {
                        resultList.add(relEntity.getPartyADesc() + "是[" + beforeObject + "]," + relEntity.getPartyBDesc() + "是[" + o1 + "]");
                    }
//                    System.out.println(beforeData);
//                    JSONObject jsonObject = ((JSONObject) beforeData).getJSONObject(beforeKey);
//                    String str1 = ((JSONObject) beforeData).getString(beforeKey);
//                    String str2 = ((JSONObject) afterData).getString(beforeKey);
                } else {
                    //todo
                    Object o = ((JSONObject) beforeData).get(beforeKey);
                    Object o1 = ((JSONObject) afterData).get(afterKey);
                    if (o instanceof JSONObject){
                        if (o1 instanceof JSONObject) {
                            analysisJson(o, o1, befLinkedList, aftLinkedList, relEntity, resultList);
                        }else {
                            resultList.add("类型不匹配:" + relEntity.getPartyADesc() + "是[JSONObject]," + relEntity.getPartyBDesc() + "非[JSONObject]");
                        }
                    }else if (o instanceof JSONArray){
                        if (o1 instanceof JSONArray){
                            analysisJson(o, o1, befLinkedList, aftLinkedList, relEntity, resultList);
                        }else {
                            resultList.add("类型不匹配:" + relEntity.getPartyADesc() + "是[JSONArray]," + relEntity.getPartyBDesc() + "非[JSONArray]");
                        }
                    }else {
                        System.out.println("什么鬼玩意儿......");
                    }
                }
            } else {
                resultList.add("类型不匹配:" + relEntity.getPartyADesc() + "是[JSONObject]," + relEntity.getPartyBDesc() + "非[JSONObject]");
            }
        } else if (beforeData instanceof JSONArray) {
            if (afterData instanceof JSONArray) {
                //todo 数组的处理
                JSONArray jsonArray_b = (JSONArray)beforeData;
                JSONArray jsonArray_a = (JSONArray)afterData;
                befLinkedList.removeFirst();
                aftLinkedList.removeFirst();
                for (int i = 0; i < jsonArray_b.size(); i++){
                    befLinkedList.addFirst(beforeKey);
                    aftLinkedList.addFirst(afterKey);
                    Object a = jsonArray_b.get(i);
                    Object bb = jsonArray_a.get(i);
                    analysisJson(a, bb, befLinkedList, aftLinkedList, relEntity, resultList);
                }
            } else {
                resultList.add("类型不匹配:" + relEntity.getPartyADesc() + "是[JSONArray]," + relEntity.getPartyBDesc() + "非[JSONArray]");
            }
        } else {
            System.out.println("啥玩意儿b?" + beforeData);
            System.out.println("啥玩意儿a?" + afterData);
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
        RelEntity relEntity2 = new RelEntity("002006.line3.dataorg", "a的年龄", "002006.line3.dataorg", "b的名字");
        RelEntity relEntity3 = new RelEntity("a_id", "a的id", "b_id", "b的id");

        String str1 = "{\"002006\":{\"line3\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line2\":[{\"orderno\":\"1\",\"occupation\":\"--\",\"gettime\":\"2017-06-07\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"--\"},{\"orderno\":\"2\",\"occupation\":\"专业技术人员\",\"gettime\":\"2016-08-05\",\"duty\":\"中级领导（行政级别局级以下领导或大公司中级管理人员）\",\"startyear\":\"--\",\"industry\":\"信息传输、软件和信息技术服务业\",\"title\":\"--\"},{\"orderno\":\"3\",\"occupation\":\"--\",\"gettime\":\"2012-07-30\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"--\"},{\"orderno\":\"4\",\"occupation\":\"--\",\"gettime\":\"2010-08-19\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"--\"},{\"orderno\":\"5\",\"occupation\":\"--\",\"gettime\":\"2005-12-07\",\"duty\":\"一般员工\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"无\"},{\"orderno\":\"6\",\"occupation\":\"\",\"gettime\":\"\",\"duty\":\"\",\"startyear\":\"\",\"industry\":\"\",\"title\":\"\"}],\"line1\":[{\"orderno\":\"1\",\"employeraddress\":\"江苏省南京市玄武区孝陵卫街道顾家营公交场站（已详）\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"2\",\"employeraddress\":\"江苏省南京玄武区中央路258号江南大厦\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"3\",\"employeraddress\":\"江苏省南京市玄武区顾家营公交场厂站\",\"employer\":\"南京公交总公司\"},{\"orderno\":\"4\",\"employeraddress\":\"--\",\"employer\":\"南京市公共交通总公司\"},{\"orderno\":\"5\",\"employeraddress\":\"南京马群第四修理厂南京公交总公司第四修理厂\",\"employer\":\"南京公交总公司第四修理厂\"}]},\"001001\":{\"line1\":{\"reportcreatetime\":\"2017-09-25 12:59:01\",\"reportno\":\"2017092500004503334354\",\"querytime\":\"2017-09-25 12:59:00\"}},\"002002\":{\"line1\":{\"certno\":\"--\",\"certtype\":\"--\",\"name\":\"--\"}},\"003003\":{\"line4\":{\"amount\":\"\",\"balance\":\"\",\"count\":\"\"},\"line3\":{\"accountcount\":\"\",\"mincreditlimitperorg\":\"\",\"usedcreditlimit\":\"\",\"financeorgcount\":\"\",\"maxcreditlimitperorg\":\"\",\"latest6monthusedavgamount\":\"\",\"creditlimit\":\"\",\"financecorpcount\":\"\"},\"line2\":{\"accountcount\":\"8\",\"mincreditlimitperorg\":\"9000\",\"usedcreditlimit\":\"65308\",\"financeorgcount\":\"4\",\"maxcreditlimitperorg\":\"48000\",\"latest6monthusedavgamount\":\"60923\",\"creditlimit\":\"139000\",\"financecorpcount\":\"4\"},\"line1\":{\"balance\":\"68336\",\"accountcount\":\"1\",\"financeorgcount\":\"1\",\"latest6monthusedavgamount\":\"3379\",\"creditlimit\":\"100000\",\"financecorpcount\":\"1\"}},\"002003\":{\"line1\":{\"telephoneno\":\"--\",\"employer\":\"--\"}},\"004004\":[{\"line7\":[{\"amount\":\"1006\",\"month\":\"2013-06\",\"lastmonths\":\"6\"},{\"amount\":\"705\",\"month\":\"2013-05\",\"lastmonths\":\"5\"},{\"amount\":\"217\",\"month\":\"2013-04\",\"lastmonths\":\"4\"},{\"amount\":\"1655\",\"month\":\"2013-03\",\"lastmonths\":\"3\"},{\"amount\":\"1077\",\"month\":\"2012-02\",\"lastmonths\":\"2\"},{\"amount\":\"537\",\"month\":\"2013-01\",\"lastmonths\":\"1\"}],\"line6\":{\"overduebegindate\":\"2012-10\",\"overdueenddate\":\"2015-09\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-09\",\"beginmonth\":\"2015-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-09-11\",\"recentpaydate\":\"2017-08-26\",\"actualpaymentamount\":\"900\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"13125\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"13992\",\"scheduledpaymentamount\":\"899\",\"usedhighestamount\":\"16287\"},\"line1\":{\"financeorg\":\"华夏银行\",\"orderno\":\"1\",\"creditlimitamount\":\"36000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"36000\",\"opendate\":\"2012-07-30\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-09-11\",\"account\":\"15663042157256\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"279\",\"month\":\"2013-07\",\"lastmonths\":\"1\"}],\"line6\":{\"overduebegindate\":\"2012-10\",\"overdueenddate\":\"2015-08\"},\"line5\":{\"latest24state\":\"NNNNNNNNNN**************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-23\",\"recentpaydate\":\"2016-06-08\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"14299\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"2\",\"creditlimitamount\":\"25000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"25000\",\"opendate\":\"2010-08-19\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-08-23\",\"account\":\"15645128903090592524\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-17\",\"recentpaydate\":\"2017-08-10\",\"actualpaymentamount\":\"2500\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"47358\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"42102\",\"scheduledpaymentamount\":\"2358\",\"usedhighestamount\":\"48807\"},\"line1\":{\"financeorg\":\"广发银行南京分行营业部\",\"orderno\":\"3\",\"creditlimitamount\":\"48000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"69000\",\"opendate\":\"2005-12-07\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-08-17\",\"account\":\"1005738255000001SMC002156014\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"************************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-17\",\"recentpaydate\":\"2005-12-07\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"0\"},\"line1\":{\"financeorg\":\"广发银行南京分行营业部\",\"orderno\":\"4\",\"creditlimitamount\":\"45270\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2005-12-07\",\"currency\":\"美元账户\",\"state\":\"\",\"stateenddate\":\"2017-08-17\",\"account\":\"1005738255000001SMC002840014\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"************************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-23\",\"recentpaydate\":\"2007-05-23\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"0\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"5\",\"creditlimitamount\":\"25000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2007-05-23\",\"currency\":\"美元账户\",\"state\":\"\",\"stateenddate\":\"2017-08-23\",\"account\":\"84045129003090705249\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNN**************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-23\",\"recentpaydate\":\"2016-06-08\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"11398\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"6\",\"creditlimitamount\":\"25000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2007-05-23\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-08-23\",\"account\":\"15645129003090705249\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"************************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-23\",\"recentpaydate\":\"2010-08-19\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"0\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"7\",\"creditlimitamount\":\"25000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2010-08-19\",\"currency\":\"美元账户\",\"state\":\"\",\"stateenddate\":\"2017-08-23\",\"account\":\"84045128903090592524\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"/////////////////////NNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-15\",\"recentpaydate\":\"2017-08-07\",\"actualpaymentamount\":\"4832\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"4825\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"4830\",\"scheduledpaymentamount\":\"4832\",\"usedhighestamount\":\"4833\"},\"line1\":{\"financeorg\":\"广州银行\",\"orderno\":\"8\",\"creditlimitamount\":\"9000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"9000\",\"opendate\":\"2017-06-07\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-08-15\",\"account\":\"15664131346444\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"\",\"beginmonth\":\"\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"\",\"scheduledpaymentdate\":\"\",\"recentpaydate\":\"\",\"actualpaymentamount\":\"\",\"curroverdueamount\":\"\"},\"line2\":{\"usedcreditlimitamount\":\"\",\"loanacctstate\":\"\",\"latest6monthusedavgamount\":\"\",\"scheduledpaymentamount\":\"\",\"usedhighestamount\":\"\"},\"line1\":{\"financeorg\":\"平安银行信用卡中心\",\"orderno\":\"9\",\"creditlimitamount\":\"5000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"5000\",\"opendate\":\"2004-11-26\",\"currency\":\"人民币账户\",\"state\":\"销户\",\"stateenddate\":\"2005-04-08\",\"account\":\"2998010006734247\",\"badbalance\":\"\"}}],\"005005\":{\"line2\":[{\"orderno\":\"1\",\"organname\":\"南京江南公交客运有限公司\",\"gettime\":\"2017-08-01\"}],\"line1\":[{\"area\":\"江苏省南京市\",\"orderno\":\"1\",\"tomonth\":\"2017-07\",\"pay\":\"1786\",\"state\":\"缴交\",\"registerdate\":\"1992-04-01\",\"firstmonth\":\"2003-07\",\"ownpercent\":\"10\",\"compercent\":\"10\"}]},\"001002\":{\"line1\":{\"customername\":\"季东\",\"certtype\":\"1\",\"certno\":\"320113196711174875\"}},\"001003\":{\"line1\":{\"operateuser\":\"ZZ*syg*d1_gdyh001\",\"queryreason\":\"本人查询（临柜）\",\"operateorg\":\"中国人民银行南京分行营业管理部\"}},\"002004\":{\"line1\":{\"dataorg\":\"广州银行\"}},\"002005\":{\"line2\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line1\":[{\"orderno\":\"1\",\"address\":\"江苏省南京市玄武区孝陵卫街200号5幢101室\",\"gettime\":\"2017-06-07\",\"residencetype\":\"亲属楼宇\"},{\"orderno\":\"2\",\"address\":\"江苏省南京玄武区南京理工大学小区5座101室\",\"gettime\":\"2016-08-05\",\"residencetype\":\"自置\"},{\"orderno\":\"3\",\"address\":\"江苏省南京市玄武区200号5栋101室\",\"gettime\":\"2012-07-30\",\"residencetype\":\"其他\"},{\"orderno\":\"4\",\"address\":\"南京市玄武区孝陵卫街200号5幢101室\",\"gettime\":\"2010-08-19\",\"residencetype\":\"未知\"},{\"orderno\":\"5\",\"address\":\"江苏南京南京理工大学5栋101室\",\"gettime\":\"2005-12-07\",\"residencetype\":\"自置\"}]},\"003001\":{\"line2\":{\"digital\":\"\",\"todescribe\":\"\",\"reposition\":\"\"},\"line1\":{\"loancardcount\":\"9\",\"otherloancount\":\"1\",\"firstloancardopenmonth\":\"2004-11\",\"standardloancardcount\":\"0\",\"firststandardloancardopenmonth\":\"--\",\"dissentcount\":\"0\",\"houseloancount\":\"0\",\"houseloan2count\":\"0\",\"firstloanopenmonth\":\"2016-08\",\"announcecount\":\"0\"}},\"004003\":[{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"////////////*NNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-09\",\"beginmonth\":\"2015-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"overdueover180amount\":\"0\",\"overdue61to90amount\":\"0\",\"overdue31to60amount\":\"0\",\"curroverdueamount\":\"0\",\"overdue91to180amount\":\"0\"},\"line2\":{\"loanacctstate\":\"正常\",\"balance\":\"68336\",\"scheduledpaymentdate\":\"2017-09-05\",\"scheduledpaymentamount\":\"3379\",\"recentpaydate\":\"2017-09-05\",\"remainpaymentcyc\":\"23\",\"class5state\":\"正常\",\"actualpaymentamount\":\"3379\"},\"line1\":{\"financeorg\":\"平安银行南京城中支行\",\"orderno\":\"1\",\"paymentcyc\":\"36\",\"creditlimitamount\":\"100000\",\"guaranteetype\":\"信用/免担保\",\"opendate\":\"2016-08-05\",\"paymentrating\":\"按月归还\",\"type\":\"其他贷款\",\"stateenddate\":\"2017-09-05\",\"enddate\":\"2019-08-05\",\"currency\":\"人民币\",\"state\":\"\",\"account\":\"RL20160805000845\",\"badbalance\":\"\"}}],\"002001\":{\"line6\":{\"postaddressorg\":\"广州银行\",\"registeredaddressorg\":\"--\"},\"line5\":{\"postaddress\":\"江苏省南京市玄武区孝陵卫街道顾家营公交场站[已祥]\",\"registeredaddress\":\"--\"},\"line4\":{\"hometelephonenoorg\":\"兴业银行\",\"edulevelorg\":\"广州银行\",\"officetelephonenoorg\":\"广州银行\",\"edudegreeorg\":\"广发银行南京分行营业部\"},\"line3\":{\"edulevel\":\"大学专科和专科学校（简称“大专”）\",\"edudegree\":\"其他\",\"officetelephoneno\":\"02569839520\",\"hometelephoneno\":\"02584317266\"},\"line2\":{\"mobileorg\":\"广州银行\",\"genderorg\":\"广州银行\",\"maritalstateorg\":\"广州银行\",\"birthdayorg\":\"广州银行\"},\"line1\":{\"birthday\":\"1967-11-17\",\"gender\":\"男性\",\"maritalstate\":\"已婚\",\"mobile\":\"13655174503\"}},\"003002\":{\"line2\":{\"count2\":\"2\",\"highestoverdueamountpermon3\":\"--\",\"months\":\"0\",\"highestoverdueamountpermon\":\"--\",\"maxduration\":\"0\",\"months3\":\"0\",\"count3\":\"0\",\"maxduration3\":\"0\",\"maxduration2\":\"6\",\"count\":\"0\",\"months2\":\"7\",\"highestoverdueamountpermon2\":\"1655\"},\"line1\":{\"count2\":\"\",\"balance3\":\"\",\"balance2\":\"\",\"balance\":\"\",\"count3\":\"\",\"count\":\"\"}},\"008003\":{\"line1\":[{\"querydate\":\"2017-06-02\",\"orderno\":\"1\",\"queryreason\":\"本人查询（临柜）\",\"querier\":\"中国人民银行南京分行营业管理部\"}]},\"008002\":{\"line1\":[{\"querydate\":\"2017-08-23\",\"orderno\":\"1\",\"queryreason\":\"贷款审批\",\"querier\":\"南京银行股份有限公司\"},{\"querydate\":\"2017-08-15\",\"orderno\":\"2\",\"queryreason\":\"贷后管理\",\"querier\":\"平安银行\"},{\"querydate\":\"2017-08-10\",\"orderno\":\"3\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2017-06-14\",\"orderno\":\"4\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2017-06-06\",\"orderno\":\"5\",\"queryreason\":\"贷款审批\",\"querier\":\"广州银行\"},{\"querydate\":\"2017-06-06\",\"orderno\":\"6\",\"queryreason\":\"贷款审批\",\"querier\":\"南京银行股份有限公司\"},{\"querydate\":\"2017-05-12\",\"orderno\":\"7\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2017-01-10\",\"orderno\":\"8\",\"queryreason\":\"保前审查\",\"querier\":\"中国平安财产保险股份有限公司\"},{\"querydate\":\"2016-12-10\",\"orderno\":\"9\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2016-08-14\",\"orderno\":\"10\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2016-08-01\",\"orderno\":\"11\",\"queryreason\":\"保前审查\",\"querier\":\"中国平安财产保险股份有限公司\"},{\"querydate\":\"2016-08-01\",\"orderno\":\"12\",\"queryreason\":\"信用卡审批\",\"querier\":\"平安银行\"},{\"querydate\":\"2016-06-09\",\"orderno\":\"13\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2016-02-20\",\"orderno\":\"14\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2016-02-02\",\"orderno\":\"15\",\"queryreason\":\"贷款审批\",\"querier\":\"中国银行江苏省分行\"},{\"querydate\":\"2015-12-10\",\"orderno\":\"16\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"}]},\"008001\":{\"line1\":{\"recordsum2\":\"0\",\"orgsum2\":\"0\",\"recordsumself\":\"0\",\"orgsum1\":\"0\",\"recordsum1\":\"0\",\"towyearrecordsum3\":\"0\",\"towyearrecordsum2\":\"0\",\"towyearrecordsum1\":\"9\"}}}";
        String str2 = "{\"002006\":{\"line3\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行hgt\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行hgt\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line2\":[{\"orderno\":\"1\",\"occupation\":\"--\",\"gettime\":\"2017-06-07\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"--\"},{\"orderno\":\"2\",\"occupation\":\"专业技术人员\",\"gettime\":\"2016-08-05\",\"duty\":\"中级领导（行政级别局级以下领导或大公司中级管理人员）\",\"startyear\":\"--\",\"industry\":\"信息传输、软件和信息技术服务业\",\"title\":\"--\"},{\"orderno\":\"3\",\"occupation\":\"--\",\"gettime\":\"2012-07-30\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"--\"},{\"orderno\":\"4\",\"occupation\":\"--\",\"gettime\":\"2010-08-19\",\"duty\":\"其他\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"--\"},{\"orderno\":\"5\",\"occupation\":\"--\",\"gettime\":\"2005-12-07\",\"duty\":\"一般员工\",\"startyear\":\"--\",\"industry\":\"--\",\"title\":\"无\"},{\"orderno\":\"6\",\"occupation\":\"\",\"gettime\":\"\",\"duty\":\"\",\"startyear\":\"\",\"industry\":\"\",\"title\":\"\"}],\"line1\":[{\"orderno\":\"1\",\"employeraddress\":\"江苏省南京市玄武区孝陵卫街道顾家营公交场站（已详）\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"2\",\"employeraddress\":\"江苏省南京玄武区中央路258号江南大厦\",\"employer\":\"南京江南公交客运有限公司\"},{\"orderno\":\"3\",\"employeraddress\":\"江苏省南京市玄武区顾家营公交场厂站\",\"employer\":\"南京公交总公司\"},{\"orderno\":\"4\",\"employeraddress\":\"--\",\"employer\":\"南京市公共交通总公司\"},{\"orderno\":\"5\",\"employeraddress\":\"南京马群第四修理厂南京公交总公司第四修理厂\",\"employer\":\"南京公交总公司第四修理厂\"}]},\"001001\":{\"line1\":{\"reportcreatetime\":\"2017-09-25 12:59:01\",\"reportno\":\"2017092500004503334354hgt\",\"querytime\":\"2017-09-25 12:59:00\"}},\"002002\":{\"line1\":{\"certno\":\"--\",\"certtype\":\"--\",\"name\":\"--\"}},\"003003\":{\"line4\":{\"amount\":\"\",\"balance\":\"\",\"count\":\"\"},\"line3\":{\"accountcount\":\"\",\"mincreditlimitperorg\":\"\",\"usedcreditlimit\":\"\",\"financeorgcount\":\"\",\"maxcreditlimitperorg\":\"\",\"latest6monthusedavgamount\":\"\",\"creditlimit\":\"\",\"financecorpcount\":\"\"},\"line2\":{\"accountcount\":\"8\",\"mincreditlimitperorg\":\"9000\",\"usedcreditlimit\":\"65308\",\"financeorgcount\":\"4\",\"maxcreditlimitperorg\":\"48000\",\"latest6monthusedavgamount\":\"60923\",\"creditlimit\":\"139000\",\"financecorpcount\":\"4\"},\"line1\":{\"balance\":\"68336\",\"accountcount\":\"1\",\"financeorgcount\":\"1\",\"latest6monthusedavgamount\":\"3379\",\"creditlimit\":\"100000\",\"financecorpcount\":\"1\"}},\"002003\":{\"line1\":{\"telephoneno\":\"--\",\"employer\":\"--\"}},\"004004\":[{\"line7\":[{\"amount\":\"1006\",\"month\":\"2013-06\",\"lastmonths\":\"6\"},{\"amount\":\"705\",\"month\":\"2013-05\",\"lastmonths\":\"5\"},{\"amount\":\"217\",\"month\":\"2013-04\",\"lastmonths\":\"4\"},{\"amount\":\"1655\",\"month\":\"2013-03\",\"lastmonths\":\"3\"},{\"amount\":\"1077\",\"month\":\"2012-02\",\"lastmonths\":\"2\"},{\"amount\":\"537\",\"month\":\"2013-01\",\"lastmonths\":\"1\"}],\"line6\":{\"overduebegindate\":\"2012-10\",\"overdueenddate\":\"2015-09\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-09\",\"beginmonth\":\"2015-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-09-11\",\"recentpaydate\":\"2017-08-26\",\"actualpaymentamount\":\"900\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"13125\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"13992\",\"scheduledpaymentamount\":\"899\",\"usedhighestamount\":\"16287\"},\"line1\":{\"financeorg\":\"华夏银行\",\"orderno\":\"1\",\"creditlimitamount\":\"36000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"36000\",\"opendate\":\"2012-07-30\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-09-11\",\"account\":\"15663042157256\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"279\",\"month\":\"2013-07\",\"lastmonths\":\"1\"}],\"line6\":{\"overduebegindate\":\"2012-10\",\"overdueenddate\":\"2015-08\"},\"line5\":{\"latest24state\":\"NNNNNNNNNN**************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-23\",\"recentpaydate\":\"2016-06-08\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"14299\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"2\",\"creditlimitamount\":\"25000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"25000\",\"opendate\":\"2010-08-19\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-08-23\",\"account\":\"15645128903090592524\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNNNNNNNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-17\",\"recentpaydate\":\"2017-08-10\",\"actualpaymentamount\":\"2500\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"47358\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"42102\",\"scheduledpaymentamount\":\"2358\",\"usedhighestamount\":\"48807\"},\"line1\":{\"financeorg\":\"广发银行南京分行营业部\",\"orderno\":\"3\",\"creditlimitamount\":\"48000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"69000\",\"opendate\":\"2005-12-07\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-08-17\",\"account\":\"1005738255000001SMC002156014\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"************************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-17\",\"recentpaydate\":\"2005-12-07\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"0\"},\"line1\":{\"financeorg\":\"广发银行南京分行营业部\",\"orderno\":\"4\",\"creditlimitamount\":\"45270\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2005-12-07\",\"currency\":\"美元账户\",\"state\":\"\",\"stateenddate\":\"2017-08-17\",\"account\":\"1005738255000001SMC002840014\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"************************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-23\",\"recentpaydate\":\"2007-05-23\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"0\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"5\",\"creditlimitamount\":\"25000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2007-05-23\",\"currency\":\"美元账户\",\"state\":\"\",\"stateenddate\":\"2017-08-23\",\"account\":\"84045129003090705249\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"NNNNNNNNNN**************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-23\",\"recentpaydate\":\"2016-06-08\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"11398\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"6\",\"creditlimitamount\":\"25000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2007-05-23\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-08-23\",\"account\":\"15645129003090705249\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"************************\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-23\",\"recentpaydate\":\"2010-08-19\",\"actualpaymentamount\":\"0\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"0\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"0\",\"scheduledpaymentamount\":\"0\",\"usedhighestamount\":\"0\"},\"line1\":{\"financeorg\":\"兴业银行\",\"orderno\":\"7\",\"creditlimitamount\":\"25000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"0\",\"opendate\":\"2010-08-19\",\"currency\":\"美元账户\",\"state\":\"\",\"stateenddate\":\"2017-08-23\",\"account\":\"84045128903090592524\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"/////////////////////NNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-08\",\"beginmonth\":\"2015-09\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"scheduledpaymentdate\":\"2017-08-15\",\"recentpaydate\":\"2017-08-07\",\"actualpaymentamount\":\"4832\",\"curroverdueamount\":\"0\"},\"line2\":{\"usedcreditlimitamount\":\"4825\",\"loanacctstate\":\"正常\",\"latest6monthusedavgamount\":\"4830\",\"scheduledpaymentamount\":\"4832\",\"usedhighestamount\":\"4833\"},\"line1\":{\"financeorg\":\"广州银行\",\"orderno\":\"8\",\"creditlimitamount\":\"9000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"9000\",\"opendate\":\"2017-06-07\",\"currency\":\"人民币账户\",\"state\":\"\",\"stateenddate\":\"2017-08-15\",\"account\":\"15664131346444\",\"badbalance\":\"\"}},{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"\",\"beginmonth\":\"\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"\",\"scheduledpaymentdate\":\"\",\"recentpaydate\":\"\",\"actualpaymentamount\":\"\",\"curroverdueamount\":\"\"},\"line2\":{\"usedcreditlimitamount\":\"\",\"loanacctstate\":\"\",\"latest6monthusedavgamount\":\"\",\"scheduledpaymentamount\":\"\",\"usedhighestamount\":\"\"},\"line1\":{\"financeorg\":\"平安银行信用卡中心\",\"orderno\":\"9\",\"creditlimitamount\":\"5000\",\"guaranteetype\":\"信用/免担保\",\"sharecreditlimitamount\":\"5000\",\"opendate\":\"2004-11-26\",\"currency\":\"人民币账户\",\"state\":\"销户\",\"stateenddate\":\"2005-04-08\",\"account\":\"2998010006734247\",\"badbalance\":\"\"}}],\"005005\":{\"line2\":[{\"orderno\":\"1\",\"organname\":\"南京江南公交客运有限公司\",\"gettime\":\"2017-08-01\"}],\"line1\":[{\"area\":\"江苏省南京市\",\"orderno\":\"1\",\"tomonth\":\"2017-07\",\"pay\":\"1786\",\"state\":\"缴交\",\"registerdate\":\"1992-04-01\",\"firstmonth\":\"2003-07\",\"ownpercent\":\"10\",\"compercent\":\"10\"}]},\"001002\":{\"line1\":{\"customername\":\"季东\",\"certtype\":\"1\",\"certno\":\"320113196711174875\"}},\"001003\":{\"line1\":{\"operateuser\":\"ZZ*syg*d1_gdyh001\",\"queryreason\":\"本人查询（临柜）\",\"operateorg\":\"中国人民银行南京分行营业管理部\"}},\"002004\":{\"line1\":{\"dataorg\":\"广州银行\"}},\"002005\":{\"line2\":[{\"orderno\":\"1\",\"dataorg\":\"广州银行\"},{\"orderno\":\"2\",\"dataorg\":\"平安银行南京城中支行\"},{\"orderno\":\"3\",\"dataorg\":\"华夏银行\"},{\"orderno\":\"4\",\"dataorg\":\"兴业银行\"},{\"orderno\":\"5\",\"dataorg\":\"广发银行南京分行营业部\"}],\"line1\":[{\"orderno\":\"1\",\"address\":\"江苏省南京市玄武区孝陵卫街200号5幢101室\",\"gettime\":\"2017-06-07\",\"residencetype\":\"亲属楼宇\"},{\"orderno\":\"2\",\"address\":\"江苏省南京玄武区南京理工大学小区5座101室\",\"gettime\":\"2016-08-05\",\"residencetype\":\"自置\"},{\"orderno\":\"3\",\"address\":\"江苏省南京市玄武区200号5栋101室\",\"gettime\":\"2012-07-30\",\"residencetype\":\"其他\"},{\"orderno\":\"4\",\"address\":\"南京市玄武区孝陵卫街200号5幢101室\",\"gettime\":\"2010-08-19\",\"residencetype\":\"未知\"},{\"orderno\":\"5\",\"address\":\"江苏南京南京理工大学5栋101室\",\"gettime\":\"2005-12-07\",\"residencetype\":\"自置\"}]},\"003001\":{\"line2\":{\"digital\":\"\",\"todescribe\":\"\",\"reposition\":\"\"},\"line1\":{\"loancardcount\":\"9\",\"otherloancount\":\"1\",\"firstloancardopenmonth\":\"2004-11\",\"standardloancardcount\":\"0\",\"firststandardloancardopenmonth\":\"--\",\"dissentcount\":\"0\",\"houseloancount\":\"0\",\"houseloan2count\":\"0\",\"firstloanopenmonth\":\"2016-08\",\"announcecount\":\"0\"}},\"004003\":[{\"line7\":[{\"amount\":\"\",\"month\":\"\",\"lastmonths\":\"\"}],\"line6\":{\"overduebegindate\":\"\",\"overdueenddate\":\"\"},\"line5\":{\"latest24state\":\"////////////*NNNNNNNNNNN\"},\"line11\":[{\"adddate\":\"\",\"objection\":\"\"}],\"line4\":{\"endmonth\":\"2017-09\",\"beginmonth\":\"2015-10\"},\"line10\":[{\"ideclare\":\"\",\"adddate\":\"\"}],\"line9\":[{\"loanorg\":\"\",\"adddate\":\"\"}],\"line8\":[{\"changingmonths\":\"\",\"gettime\":\"\",\"changingamount\":\"\",\"type\":\"\",\"content\":\"\"}],\"line3\":{\"curroverduecyc\":\"0\",\"overdueover180amount\":\"0\",\"overdue61to90amount\":\"0\",\"overdue31to60amount\":\"0\",\"curroverdueamount\":\"0\",\"overdue91to180amount\":\"0\"},\"line2\":{\"loanacctstate\":\"正常\",\"balance\":\"68336\",\"scheduledpaymentdate\":\"2017-09-05\",\"scheduledpaymentamount\":\"3379\",\"recentpaydate\":\"2017-09-05\",\"remainpaymentcyc\":\"23\",\"class5state\":\"正常\",\"actualpaymentamount\":\"3379\"},\"line1\":{\"financeorg\":\"平安银行南京城中支行\",\"orderno\":\"1\",\"paymentcyc\":\"36\",\"creditlimitamount\":\"100000\",\"guaranteetype\":\"信用/免担保\",\"opendate\":\"2016-08-05\",\"paymentrating\":\"按月归还\",\"type\":\"其他贷款\",\"stateenddate\":\"2017-09-05\",\"enddate\":\"2019-08-05\",\"currency\":\"人民币\",\"state\":\"\",\"account\":\"RL20160805000845\",\"badbalance\":\"\"}}],\"002001\":{\"line6\":{\"postaddressorg\":\"广州银行\",\"registeredaddressorg\":\"--\"},\"line5\":{\"postaddress\":\"江苏省南京市玄武区孝陵卫街道顾家营公交场站[已祥]\",\"registeredaddress\":\"--\"},\"line4\":{\"hometelephonenoorg\":\"兴业银行\",\"edulevelorg\":\"广州银行\",\"officetelephonenoorg\":\"广州银行\",\"edudegreeorg\":\"广发银行南京分行营业部\"},\"line3\":{\"edulevel\":\"大学专科和专科学校（简称“大专”）\",\"edudegree\":\"其他\",\"officetelephoneno\":\"02569839520\",\"hometelephoneno\":\"02584317266\"},\"line2\":{\"mobileorg\":\"广州银行\",\"genderorg\":\"广州银行\",\"maritalstateorg\":\"广州银行\",\"birthdayorg\":\"广州银行\"},\"line1\":{\"birthday\":\"1967-11-17\",\"gender\":\"男性\",\"maritalstate\":\"已婚\",\"mobile\":\"13655174503\"}},\"003002\":{\"line2\":{\"count2\":\"2\",\"highestoverdueamountpermon3\":\"--\",\"months\":\"0\",\"highestoverdueamountpermon\":\"--\",\"maxduration\":\"0\",\"months3\":\"0\",\"count3\":\"0\",\"maxduration3\":\"0\",\"maxduration2\":\"6\",\"count\":\"0\",\"months2\":\"7\",\"highestoverdueamountpermon2\":\"1655\"},\"line1\":{\"count2\":\"\",\"balance3\":\"\",\"balance2\":\"\",\"balance\":\"\",\"count3\":\"\",\"count\":\"\"}},\"008003\":{\"line1\":[{\"querydate\":\"2017-06-02\",\"orderno\":\"1\",\"queryreason\":\"本人查询（临柜）\",\"querier\":\"中国人民银行南京分行营业管理部\"}]},\"008002\":{\"line1\":[{\"querydate\":\"2017-08-23\",\"orderno\":\"1\",\"queryreason\":\"贷款审批\",\"querier\":\"南京银行股份有限公司\"},{\"querydate\":\"2017-08-15\",\"orderno\":\"2\",\"queryreason\":\"贷后管理\",\"querier\":\"平安银行\"},{\"querydate\":\"2017-08-10\",\"orderno\":\"3\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2017-06-14\",\"orderno\":\"4\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2017-06-06\",\"orderno\":\"5\",\"queryreason\":\"贷款审批\",\"querier\":\"广州银行\"},{\"querydate\":\"2017-06-06\",\"orderno\":\"6\",\"queryreason\":\"贷款审批\",\"querier\":\"南京银行股份有限公司\"},{\"querydate\":\"2017-05-12\",\"orderno\":\"7\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2017-01-10\",\"orderno\":\"8\",\"queryreason\":\"保前审查\",\"querier\":\"中国平安财产保险股份有限公司\"},{\"querydate\":\"2016-12-10\",\"orderno\":\"9\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2016-08-14\",\"orderno\":\"10\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2016-08-01\",\"orderno\":\"11\",\"queryreason\":\"保前审查\",\"querier\":\"中国平安财产保险股份有限公司\"},{\"querydate\":\"2016-08-01\",\"orderno\":\"12\",\"queryreason\":\"信用卡审批\",\"querier\":\"平安银行\"},{\"querydate\":\"2016-06-09\",\"orderno\":\"13\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"},{\"querydate\":\"2016-02-20\",\"orderno\":\"14\",\"queryreason\":\"贷后管理\",\"querier\":\"广发银行\"},{\"querydate\":\"2016-02-02\",\"orderno\":\"15\",\"queryreason\":\"贷款审批\",\"querier\":\"中国银行江苏省分行\"},{\"querydate\":\"2015-12-10\",\"orderno\":\"16\",\"queryreason\":\"贷后管理\",\"querier\":\"华夏银行\"}]},\"008001\":{\"line1\":{\"recordsum2\":\"0\",\"orgsum2\":\"0\",\"recordsumself\":\"0\",\"orgsum1\":\"0\",\"recordsum1\":\"0\",\"towyearrecordsum3\":\"0\",\"towyearrecordsum2\":\"0\",\"towyearrecordsum1\":\"9\"}}}";

        List<String> list = compareEntitys(str1, str2, Arrays.asList(relEntity1, relEntity2));
        for (String str : list) {
            System.out.println("结果:" + str);
        }
    }

}
