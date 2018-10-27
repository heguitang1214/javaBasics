package spring.aopAnnotation.dao.impl;

import org.springframework.stereotype.Component;
import spring.aopAnnotation.dao.UserDAO;
import spring.aopAnnotation.model.User;


@Component("u")
public class UserDAOImpl implements UserDAO {

	public void save(User user) {
		//Hibernate
		//JDBC
		//XML
		//NetWork
		System.out.println("UserDAOImpl user saved!");
		//throw new RuntimeException("exeption!");
	}

}
