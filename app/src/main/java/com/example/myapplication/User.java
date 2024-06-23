package com.example.myapplication;

// User.java
public class User {

    private String id;
    private String name;
    private Integer age;

    public User(String id, Integer age, String name) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public String getId() {
        return id;
    }


    public String getAge() {
        return age + "歳";
    }

    public String getName() {
        return name;
    }
}

