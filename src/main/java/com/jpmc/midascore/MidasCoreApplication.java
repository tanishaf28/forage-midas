package com.jpmc.midascore; //This is the entrypouint .It scans for components:sets up dependency injection ;basically it turns everything on and gets the application running. 

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MidasCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MidasCoreApplication.class, args);//This is the main method that starts the application. It runs the SpringApplication which bootstraps the application, starting the Spring context and the embedded server.
    }

}
