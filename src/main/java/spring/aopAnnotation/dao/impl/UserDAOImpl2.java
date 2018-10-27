package spring.aopAnnotation.dao.impl;

import org.springframework.stereotype.Component;
import spring.aopAnnotation.dao.UserDAO;
import spring.aopAnnotation.model.User;

/**
 * @author he_guitang
 * @version [1.0 , 2018/8/31]
 */
@Component("u2")
public class UserDAOImpl2 implements UserDAO {

    @Override
    public void save(User user) {
        System.out.println("UserDAOImpl2 user saved!");
    }


}
