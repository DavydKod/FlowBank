package com.davyd.models;

import com.davyd.util.Validation;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    protected User(){}

    public User(String name, String email){
        this.name = Validation.validateNotBlank(name, "Name");
        this.email = Validation.validateEmail(email);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void changeName(String newName) {
        this.name = Validation.validateNotBlank(newName, "Name");
    }
}
