package com.davyd.auth;

import com.davyd.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "user_credentials")
public class UserCredentials {
    @Id
    @Column(name = "user_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    protected UserCredentials(){
    }

    public UserCredentials(User user, String passwordHash){
        this.user = user;
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public User getUser() {
        return user;
    }
}
