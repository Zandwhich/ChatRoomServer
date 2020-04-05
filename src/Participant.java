/*
 * Author: Alex Zdanowicz
 */

import java.net.*;
import java.io.*;

/**
 * The class that represents a participant in a chat
 */
public class Participant {

    /* Fields */

    /**
     * Name of the participant
     */
    String name;

    /**
     * Socket of participant
     */
    Socket client;

    /**
     * The controller with which to talk
     */
    Controller controller;

    /**
     * Input stream from which to receive messages
     */
    DataInputStream in;

    /**
     * Output stream from which to send messages
     */
    DataOutputStream out;


    /* Constructors */

    /**
     * The constructor of the Participant class
     * @param name The name of the participant
     * @param client The socket of the participant
     * @param controller The controller that controls everything
     * @throws IOException TODO: Fill this in
     */
    public Participant(String name, Socket client, Controller controller) throws IOException /* TODO: Figure out where to throw the exception */ {
        this.name = name;
        this.client = client;
        this.controller = controller;
        this.in = new DataInputStream(this.client.getInputStream());
        this.out = new DataOutputStream(this.client.getOutputStream());
    }//end Participant()


    /* Methods */

    // Getters
    public String getName() { return this.name; }//end getName()
    public Socket getClient() { return this.client; }//end getClient()
    public int getPort() { return this.client.getPort(); }//end getPort()
    public InetAddress getInetAdress() { return this.client.getInetAddress(); }//end getInetAddress

    /**
     * Sends a message to the participant
     * @param message The message that is sent to the participant
     * @throws IOException TODO: Fill this in
     */
    public void sendMessage(String message) throws IOException /* TODO: Figure out where to throw the exception */ {
        this.out.writeUTF(message);
    }//end sendMessage()


}//end Participant
