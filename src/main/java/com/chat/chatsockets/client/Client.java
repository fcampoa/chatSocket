/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chat.chatsockets.client;

import com.chat.tcpcommons.InboxChat;
import com.chat.chatsockets.models.RegistrationModel;
import com.chat.tcpcommons.IConnection;
import com.chat.tcpcommons.Message;
import com.chat.tcpcommons.MessageType;
import com.chat.tcpcommons.User;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.System.exit;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author felix
 */
public class Client extends Thread implements IConnection {

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private MainChat chat;
    private User user;
    private Socket clientSocket;
    private List<User> connectedUsers;
    private List<InboxChat> privateChats = new ArrayList<>();
    private Registration register;
    private String host;
    private int port;
    private boolean connected;

    public Client() {
        this.user = new User();
        init();
    }

    @Override
    public void run() {
        try {
            proccessStream();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error al procesar: " + e.getMessage() + " en la clase " + getClass().getName());            
            System.exit(0);
        }
    }

    public void disconnect() {
        try {
            var message = new Message.Builder().sender(user).messageType(MessageType.DISCONNECT).build();
            sendMessage(message);
            this.in.close();
            this.out.close();
            this.clientSocket.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.exit(0);
    }

    public void openInbox(User friend) {
        var inbox = new InboxChat(chat, friend, user, this);
        privateChats.add(inbox);
        inbox.setVisible(true);
    }

    public void sendMessage(Message message) {
        try {
            this.out.writeObject(message);
            this.out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("error al enviar mensaje: " + ex.getMessage() + " en la clase " + getClass().getName());
        }
    }

    public void closeInbox(InboxChat inbox) {
        this.privateChats.remove(inbox);
        inbox.dispose();
    }

    private void proccessStream() throws Exception {
        while (connected) {
            var message = (Message) in.readObject();

            if (message.getMessageType() == MessageType.MESSAGE_GENERAL) {
                this.chat.receiveMessage(message);
            }
            if (message.getMessageType() == MessageType.USERS_UPDATE) {
                updateUsers(message);
            }
            if (message.getMessageType() == MessageType.MESSAGE_INBOX) {
                receiveInbox(message);
            }
        }
    }

    private void receiveInbox(Message message) {
        var inbox = privateChats.stream().filter(i -> i.getUser().equals(message.getSender())).findFirst().orElse(null);
        if (inbox != null) {
            inbox.receiveMessage(message);
            return;
        }
        openInbox(message.getSender());
        receiveInbox(message);
    }
    @Override
    public void init() {
        try {
            register();
            if (user.getEmail() != null) {
                this.clientSocket = new Socket(this.host, this.port);
                this.out = new ObjectOutputStream(clientSocket.getOutputStream());
                this.in = new ObjectInputStream(clientSocket.getInputStream());
                this.connected = true;
                Message message = new Message.Builder().sender(user).messageType(MessageType.CONNECTION_MESSAGE).build();
                sendMessage(message);
                this.chat = new MainChat(user, this);
                this.chat.setVisible(true);
                start();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "El servidor no responde.", "Error de conexión", JOptionPane.ERROR_MESSAGE);
            register();
        }
    }

    private void register() {
        var model = new RegistrationModel();
        this.register = new Registration(null, true, model);
        this.register.setVisible(true);
        this.user = model.getUser();
        this.host = model.getHost();
        this.port = model.getPort();
    }

    private void updateUsers(Message message) {
        this.connectedUsers = (List<User>) message.getBody();
        this.connectedUsers.remove(this.user);
        this.chat.setUsers(this.connectedUsers);
    }
}
