package com.example;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test {

    public static void main(String[] args) {
        BCryptPasswordEncoder pe = new BCryptPasswordEncoder();

        String hashPAssed = pe.encode("test1234");

        System.out.println(hashPAssed);
    }
}
