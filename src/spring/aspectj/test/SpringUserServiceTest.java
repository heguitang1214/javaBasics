package spring.aspectj.test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import spring.aspectj.model.User;
import spring.aspectj.service.UserService;


//Dependency Injection
//Inverse of Control
public class SpringUserServiceTest {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");

		UserService service = (UserService)ctx.getBean("userService");
		System.out.println(service.getClass());
		service.add(new User());

		ctx.destroy();
	}


}
