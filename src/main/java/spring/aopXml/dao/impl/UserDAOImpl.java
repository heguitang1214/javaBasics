package spring.aopXml.dao.impl;

import org.springframework.stereotype.Component;
import spring.aopXml.dao.UserDAO;
import spring.aopXml.model.User;


@Component("u") 
public class UserDAOImpl implements UserDAO {

	public void save(User user) {
		//Hibernate
		//JDBC
		//XML
		//NetWork
		System.out.println("user saved!");
		//throw new RuntimeException("exeption!");
	}

}
