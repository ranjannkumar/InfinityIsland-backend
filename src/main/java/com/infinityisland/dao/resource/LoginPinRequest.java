package com.infinityisland.dao.resource;

public class LoginPinRequest {
    String pin;
    String name;

    public LoginPinRequest() {
    }

    public LoginPinRequest(String pin, String name) {
        this.pin = pin;
        this.name = name;
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
