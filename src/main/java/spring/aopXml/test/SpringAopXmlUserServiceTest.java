package spring.aopXml.test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import spring.aopXml.model.User;
import spring.aopXml.service.UserService;


public class SpringAopXmlUserServiceTest {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring_beans_xml.xml");

		UserService service = (UserService)ctx.getBean("userService");
		System.out.println("动态代理对象:" + service.getClass());
		service.add(new User());

		ctx.destroy();
	}


}