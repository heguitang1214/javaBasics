package run;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//指定扫描的包，springBooboot默认的扫描包为：com.example.demo.xxx
@ComponentScan("controller")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

