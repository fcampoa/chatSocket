/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chat.chatsockets.models;

import com.chat.tcpcommons.User;

/**
 *
 * @author felix
 */
public class RegistrationModel {
    private User user;
    private String host;
    private int port;

    public RegistrationModel() {
        this.user = new User();
    }

    public RegistrationModel(User user, String host, int port) {
        this.user = user;
        this.host = host;
        this.port = port;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    
}
