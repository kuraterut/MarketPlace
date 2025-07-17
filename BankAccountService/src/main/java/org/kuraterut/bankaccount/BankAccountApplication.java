package org.kuraterut.bankaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "org.kuraterut.bankaccount",
        "org.kuraterut.jwtsecuritylib"
})
public class BankAccountApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankAccountApplication.class, args);
    }
}