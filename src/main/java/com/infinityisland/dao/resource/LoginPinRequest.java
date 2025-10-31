package com.infinityisland.dao.resource;

public class LoginPinRequest {
    private String pin;
    private String name; // required for new signup, optional for existing login

    public LoginPinRequest() {
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}