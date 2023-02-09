package at.craftworks.__companyLower__.__projectLower__;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


import javax.annotation.PostConstruct;

@Slf4j
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class __namePascal__Application {

    @PostConstruct
    public void init() {
        // features to be called, right after the startup of the spring framework

        // springdoc enum generation policy
        // io.swagger.v3.core.jackson.ModelResolver.enumsAsRef is loaded from system property enums-as-ref
        // this property has the effect, that TypeScript enums are only generated once (per ref) if set to true
        // otherwise enums are generated in each ts file, where the enum is used
        // https://github.com/springdoc/springdoc-openapi/issues/232
        System.setProperty("enums-as-ref", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(__namePascal__Application.class, args);
    }
}
