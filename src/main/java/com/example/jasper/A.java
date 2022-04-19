package com.example.jasper;

import lombok.Data;

@Data
public class A {
    private String id;
    private String name;

    public A(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
