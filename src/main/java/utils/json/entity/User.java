package utils.json.entity;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/16]
 * 用户实体
 */
public class User implements Serializable{

    private String name;
    private String password;
    private List<Role> roles = new ArrayList<>();

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
