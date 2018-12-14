package controller;

import entry.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import utils.excel.ExportExcel;
import utils.excel.annotation.ExcelConfEnum;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
//@RestController
@RequestMapping("/")
public class TestController {

    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    @ResponseBody
    public String test(){
        logger.info("web请求测试");
        return "成功！";
    }


    @RequestMapping(value = "/excelTest", method = RequestMethod.GET)
    @ResponseBody
    public String excelTest(HttpServletResponse response) throws IOException {
        logger.info("开始导出excel！");
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setName("姓名" + i);
            user.setAge(i);
            list.add(user);
        }
        new ExportExcel(null, User.class, ExcelConfEnum.ExportData.getIndex()).setDataList(list)
                .writeWebClient(response, "用户信息.xlsx").dispose();
        logger.info("excel导出完毕！");
        return "导出完毕！";
    }

}
