package spring.aopAnnotation.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import spring.aopAnnotation.dao.UserDAO;
import spring.aopAnnotation.model.User;


@Component("userService")
public class UserService {

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

    @Resource(name = "u")
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }


    public void destroy() {
        System.out.println("destroy");
    }
}
