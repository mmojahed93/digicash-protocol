package ir.ac.modares.model;

import ir.ac.modares.entity.EntityEnum;

import java.math.BigInteger;

public class UserModel {

    private BigInteger id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private BigInteger balance;
    private EntityEnum userType;

    public UserModel(BigInteger id, String firstName, String lastName, String username, String password, BigInteger balance, EntityEnum userType) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.userType = userType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public EntityEnum getUserType() {
        return userType;
    }

    public void setUserType(EntityEnum userType) {
        this.userType = userType;
    }
}
