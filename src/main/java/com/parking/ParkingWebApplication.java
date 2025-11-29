package com.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParkingWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParkingWebApplication.class, args);
        System.out.println("🚀 Parking System running at http://localhost:8080");
    }
}