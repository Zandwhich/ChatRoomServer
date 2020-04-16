package com.company;/*
 * Author: Alex Zdanowicz
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.net.Inet4Address;

/**
 * The controller that manages participants in the server-side application
 */
public class Controller {

    /* Fields */

    // Constants

    /**
     * The starting port number for the program
     */
    public static final int STARTING_PORT = 1024;

    // Variables

    /**
     * The list of participants in the group
     */
    private ArrayList<Participant> participants;

    /**
     * The current port of the application.
     * This field will cycle up and up as more people connect
     */
    private int port;

    /* Constructor */

    /**
     * The default constructor
     */
    public Controller() {
        this.participants = new ArrayList<>();
        this.port = Controller.STARTING_PORT;
    }//end com.company.Controller()

    /**
     * The method that runs the server-side application
     */
    public void run() {
        try {
            System.out.println("IP Address: " + Inet4Address.getLocalHost());

            ServerSocket serverSocket = new ServerSocket(1024);
            // TODO: Multi-thread this
            Socket client = serverSocket.accept();

            Participant participant = new Participant("com.company.Participant", client, this);
            this.participants.add(participant);

            System.out.println("Connected to a client computer: " + participant.getInetAddress() + " on local port " +
                    participant.getLocalPort());

            // Test receiving messages
            while (true) {
                System.out.println(this.participants.get(0).retrieveMessage());
            }//end while

//            this.participants.get(0).sendMessage("This is a test");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
        }//end try/catch
    }//end run()

    /**
     * Sends a message out to all of the participants
     * @param message The message to send out to all of the participants
     */
    public void broadcast(String message) {
        for (Participant participant : this.participants) {
            try {
                participant.sendMessage(message);
            } catch (IOException e) {
                System.err.println("There was an error sending the message:" + message + " to participant: " +
                        participant.getName());
                System.err.println("Message: " + e.getMessage());
                System.err.println("Cause: " + e.getCause());
                System.err.println("Stack Trace: " + e.getStackTrace().toString());
            }//end try/catch
        }//end for
    }//end broadcast()
}//end com.company.Controller
