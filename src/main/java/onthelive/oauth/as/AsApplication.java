package onthelive.oauth.as;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "onthelive.oauth.as")
public class AsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsApplication.class, args);
    }

}
