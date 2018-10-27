package spring.ioc.dao.impl;


import spring.ioc.dao.UserDAO;
import spring.ioc.model.User;

public class UserDAOImpl implements UserDAO {

    public void save(User user) {
        //Hibernate
        //JDBC
        //XML
        //NetWork
        System.out.println("user saved!");
    }

}
