package entry;

import com.fasterxml.jackson.annotation.JsonInclude;
import utils.excel.annotation.ExcelField;
import java.util.ArrayList;
import java.util.List;

public class User {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;
    private int age;
    private String password;
    private List<Role> roles = new ArrayList<>();

    @ExcelField(title="姓名**111", align=2, sort=1)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ExcelField(title="年龄**222", align=2, sort=2)
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }
}
