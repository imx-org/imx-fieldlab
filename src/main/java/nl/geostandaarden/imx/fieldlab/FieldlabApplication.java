package nl.geostandaarden.imx.fieldlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("nl.geostandaarden.imx")
public class FieldlabApplication {

  public static void main(String[] args) {
    SpringApplication.run(FieldlabApplication.class, args);
  }
}
