package spring.ioc.service;


import spring.ioc.dao.UserDAO;
import spring.ioc.model.User;

public class UserService {
    private UserDAO userDAO;

    public void add(User user) {
        userDAO.save(user);
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
}
