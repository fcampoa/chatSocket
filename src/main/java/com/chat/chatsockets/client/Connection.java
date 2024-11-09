/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.chat.chatsockets.client;

import com.chat.chatsockets.models.RegistrationModel;
import com.chat.tcpcommons.ClientThread;
import com.chat.tcpcommons.ConnectionTemplate;
import com.chat.tcpcommons.IConnection;
import com.chat.tcpcommons.IObserver;
import com.chat.tcpcommons.InboxChat;
import com.chat.tcpcommons.Message;
import com.chat.tcpcommons.MessageType;
import com.chat.tcpcommons.User;
import com.chat.tcpcommons.logging.IChatLogger;
import com.chat.tcpcommons.logging.LoggerFactory;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author felix
 */
public class Connection implements ConnectionTemplate, IObserver, IConnection, Serializable {

    private ClientThread client;
    private List<InboxChat> privateChats;
    private MainChat mainChat;
    private User user;
    private int port;
    private String host;

    private final IChatLogger logger = LoggerFactory.getLogger(Connection.class);
    
    ReentrantLock lock = new ReentrantLock();

    public Connection() {
        privateChats = new ArrayList<>();
        user = new User();
    }

    @Override
    public void init() {
        try {
            register();
            Socket clientSocket = new Socket(host, port);
            client = new ClientThread(clientSocket);
            client.setUser(user);
            Thread chatThread = new Thread(client);
            chatThread.start();
            client.subscribe(this);            
            mainChat = new MainChat(user, this);
            var connectionMessage = new Message.Builder().sender(user).messageType(MessageType.CONNECTION_MESSAGE).build();
            sendMessage(connectionMessage);
            mainChat.setVisible(true);
            logger.info("Usuario conectado");
        } catch (Exception ex) {
            logger.error(String.format(" %s : el servidor no responde", ex.getMessage()));
            System.exit(0);
        }
    }

    @Override
    public void onUpdate(Object obj) {
        proccessMessage((Message) obj);
    }

    @Override
    public synchronized void onUsersUpdate(Message message) {
        lock.lock();
        try {
        if (message.getBody() != null) {
        mainChat.setUsers(((List<User>) message.getBody()));
        }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onInboxMessage(Message message) {
        var inbox = privateChats.stream().filter(i -> i.getUser().equals(message.getSender())).findFirst().orElse(null);
        if (inbox != null) {
            inbox.receiveMessage(message);
            return;
        }
        openInbox(message.getSender());
        onInboxMessage(message);
    }

    @Override
    public void onGeneralMessage(Message message) {
        mainChat.receiveMessage(message);
    }

    @Override
    public void sendMessage(Message message) {
        client.sendMessage(message);
    }

    @Override
    public void closeInbox(InboxChat inbox) {
        privateChats.remove(inbox);
        inbox.dispose();
    }

    @Override
    public void openInbox(User friend) {
        var inbox = privateChats.stream().filter(i -> i.getUser().equals(user)).findFirst().orElse(null);
        if (inbox == null) {
            inbox = new InboxChat(mainChat, friend, user, this);
            privateChats.add(inbox);
            inbox.setVisible(true);
        }
        inbox.requestFocus();
    }

    @Override
    public void disconnect() {
        client.disconnect();
        System.exit(0);
    }

    private void register() {
        var model = new RegistrationModel();
        var register = new Registration(null, true, model);
        register.setVisible(true);
        user = model.getUser();
        port = model.getPort();
        host = model.getHost();
    }

}
