package com.somsubhra.springauth.registration;

import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Service
public class EmailValidator implements Predicate<String> {

    @Override
    public boolean test(String s) {
//        Todo: validate email using regex
        return true;
    }
}
