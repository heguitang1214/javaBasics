package spring.aopAnnotation.service;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import spring.aopAnnotation.dao.UserDAO;
import spring.aopAnnotation.model.User;


@Component("userService")
public class UserService {

    /**
     * 构造器注入
     * @param userDAO spring容器实现userDAO的bean
     *    要先注入UserDAO,也就是相当于set方式注入进来
     *    @Qualifier 是当实现由多个的时候,指定具体的实现
     */
    public UserService(@Qualifier("u") UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private UserDAO userDAO;

    public void init() {
        System.out.println("init");
    }

    public void add(User user) {
        userDAO.save(user);
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    /**
     * set方式注入
     */
//    @Resource(name = "u")
    @Autowired
    public void setUserDAO1(@Qualifier("u2") UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    public void destroy() {
        System.out.println("destroy");
    }
}
