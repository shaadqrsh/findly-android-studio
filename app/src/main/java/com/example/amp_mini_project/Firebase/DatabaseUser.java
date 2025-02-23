package com.example.amp_mini_project.Firebase;

public class DatabaseUser {
    private String name;
    private String phone;
    private String email;
    private String password;
    private String profileUri;

    public static String key_name = "name";
    public static String key_phone = "phone";
    public static String key_email = "email";
    public static String key_password = "password";
    public static  String key_profile_uri = "profileImage";

    public DatabaseUser() {

    }

    public DatabaseUser(String name, String phoneNumber, String email, String password) {
        this.name = name;
        this.phone = phoneNumber;
        this.email = email;
        this.password = password;
    }

    public DatabaseUser(String name, String phoneNumber, String email, String password, String profileUri) {
        this.name = name;
        this.phone = phoneNumber;
        this.email = email;
        this.password = password;
        this.profileUri = profileUri;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfileUri() { return profileUri; }
}