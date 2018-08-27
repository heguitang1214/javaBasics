package utils.json.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.json.entity.Job;
import utils.json.entity.Role;
import utils.json.entity.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/16]
 *          ObjectMapper的使用测试
 */
public class ObjectMapperTest {

    public static void main(String[] args) {

        ObjectMapper objectMapper = new ObjectMapper();
        String param = "[{\"name\":\"张三\",\"password\":\"123456\", \"roles\":[{\"name\":\"角色1\",\"jobs\":[{\"name\":\"职位1\"}," +
                "{\"name\":\"职位11\"}]},{\"name\":\"角色2\",\"jobs\":[{\"name\":\"职位1\"},{\"name\":\"职位11\"}]}]},{\"name\":\"李四\"," +
                "\"password\":\"123456\", \"roles\":[{\"name\":\"角色3\",\"jobs\":[{\"name\":\"职位3\"},{\"name\":\"职位4\"}]},{\"name\":\"角色4\"}]}]";
        List<User> users = new ArrayList<>();
        try {
            users = objectMapper.readValue(param, new TypeReference<List<User>>() {

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (User user : users){
            System.out.println("=============================================================");
            for (Role role : user.getRoles()){
                for (Job job : role.getJobs()){
                    System.out.println("姓名:" + user.getName() + ",角色:" + role.getName() + ",职位:" + job.getName());
                }
            }
            System.out.println("=============================================================");
        }
    }


}
