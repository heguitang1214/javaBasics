package utilsTest;

import entry.User;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.excel.ExportExcel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelTest {

    private static Logger log = LoggerFactory.getLogger(ExcelTest.class);

    @Test
    public void exportExcelTest() throws IOException {
//        String fileName = "用户数据导入模板.xlsx";
//        List<User> list = Lists.newArrayList(); list.add(UserUtils.getUser());
//        new ExportExcel("用户数据", User.class, 2).setDataList(list).write(response, fileName).dispose();
        List<User> list = new ArrayList<>();
        User user = new User();
        for (int i = 0; i < 10; i++){
            user.setName("姓名" + i);
            user.setAge(i);
            list.add(user);
        }
        List<User> list1 = new ArrayList<>();
        for (int i = 100; i < 110; i++){
            user.setName("姓名" + i);
            user.setAge(i);
            list1.add(user);
        }
        new ExportExcel(null, User.class, 2).setDataList(list).writeFile("D:/用户信息.xlsx").dispose();
        System.out.println("完成......");
    }







}
