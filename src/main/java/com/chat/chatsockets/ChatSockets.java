/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.chat.chatsockets;

import com.chat.chatsockets.client.Connection;

/**
 *
 * @author felix
 */
public class ChatSockets {

    public static void main(String[] args) {
        Connection connection = new Connection();
        connection.init();
    }
}
