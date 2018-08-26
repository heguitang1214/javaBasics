package spring.aop.service;

import spring.ioc.dao.UserDAO;
import spring.ioc.dao.impl.UserDAOImpl;
import spring.ioc.model.User;

/**
 * 实现在UserDAOImpl的save()前添加逻辑
 * 		方式二:组合,相对于继承,更加的灵活
 */
public class UserDAOImpl3 implements UserDAO {
	
	private UserDAO userDAO = new UserDAOImpl();


	public void save(User user) {
		System.out.println("save start...");
//		new LogInterceptor().beforeMethod(null);
		userDAO.save(user);
		
		
	}

}
