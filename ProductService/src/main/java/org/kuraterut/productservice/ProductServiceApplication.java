package org.kuraterut.productservice;

import org.kuraterut.jwtsecuritylib.config.JwtSecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "org.kuraterut.productservice",
        "org.kuraterut.jwtsecuritylib"
})
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}