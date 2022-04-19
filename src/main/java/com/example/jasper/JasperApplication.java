package com.example.jasper;

import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JasperApplication {

    public static void main(String[] args) throws JRException {
        SpringApplication.run(JasperApplication.class, args);
        Main.start();
    }

}
