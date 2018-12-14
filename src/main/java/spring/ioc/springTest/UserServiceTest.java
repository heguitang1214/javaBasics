package spring.ioc.springTest;
import spring.ioc.model.User;
import spring.ioc.service.UserService;
import spring.mySpring.BeanFactory;
import spring.mySpring.ClassPathXmlApplicationContext;


public class UserServiceTest {

	public static void main(String[] args) throws Exception {
		testAdd();
	}

	private static void testAdd() throws Exception {
		BeanFactory applicationContext = new ClassPathXmlApplicationContext();

		UserService service = (UserService)applicationContext.getBean("userService");

		User u = new User();
		u.setUsername("zhangsan");
		u.setPassword("zhangsan");
		service.add(u);
	}

}
