package spring.springTest;
import spring.model.User;
import spring.mySpring.BeanFactory;
import spring.mySpring.ClassPathXmlApplicationContext;
import spring.service.UserService;


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
