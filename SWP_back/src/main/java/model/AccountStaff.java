/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author DTanh
 */
public class AccountStaff {
    private int accountStaffId;
    private String userName;
    private String passWord;
    private String role;
    private String email;
    private String img;
    private boolean status;

    public AccountStaff() {
    }

    public AccountStaff(int accountStaffId, String userName, String passWord, String role, String email, String img, boolean status) {
        this.accountStaffId = accountStaffId;
        this.userName = userName;
        this.passWord = passWord;
        this.role = role;
        this.email = email;
        this.img = img;
        this.status = status;
    }

    public int getAccountStaffId() {
        return accountStaffId;
    }

    public void setAccountStaffId(int accountStaffId) {
        this.accountStaffId = accountStaffId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
