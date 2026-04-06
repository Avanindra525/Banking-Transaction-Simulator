
package com.bank.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Account {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne
@JoinColumn(name = "user_id")
private User user;

private String name;
private String email;
private String accountType;
private double balance;

public Account() {
}

public Long getUserId() {
    return user != null ? user.getId() : null;
}

public void setUserId(Long userId) {
    if (userId != null) {
        this.user = new User();
        this.user.setId(userId);
    }
}

public Account(Long id, String name, String email, double balance) {
this.id = id;
this.name = name;
this.email = email;
this.balance = balance;
}

public Account(Long id, String name, String email, String accountType, double balance) {
this.id = id;
this.name = name;
this.email = email;
this.accountType = accountType;
this.balance = balance;
}

public User getUser() {
    return user;
}

public void setUser(User user) {
    this.user = user;
}

public Long getId() {
return id;
}

public void setId(Long id) {
this.id = id;
}

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public String getEmail() {
return email;
}

public void setEmail(String email) {
this.email = email;
}

public String getAccountType() {
return accountType;
}

public void setAccountType(String accountType) {
this.accountType = accountType;
}

public double getBalance() {
return balance;
}

public void setBalance(double balance) {
this.balance = balance;
}

}
