package com.company;/*
 * Author: Alex Zdanowicz
 */

import json_simple.JSONObject;
import json_simple.parser.JSONParser;

import java.awt.*;
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

    /**
     * The key for the name part of the object
     */
    public static final String NAME_KEY = "name";

    /**
     * The key for the message part of the object
     */
    public static final String MESSAGE_KEY = "message";

    /**
     * The key for the text of objects
     */
    public static final String TEXT_KEY = "text";

    /**
     * The key for the time that is sent in
     */
    public static final String TIME_KEY = "time";

    /**
     * The key for the color object
     */
    public static final String COLOR_KEY = "color";

    /**
     * The key for the red value in the color
     */
    public static final String RED_KEY = "red";

    /**
     * The key for the green value in the color
     */
    public static final String GREEN_KEY = "green";

    /**
     * The key for the blue value in the color
     */
    public static final String BLUE_KEY = "blue";

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
                // TODO: This is only here temporarily
                String temp = this.participants.get(0).retrieveMessage();
                System.out.println(temp);
                JSONParser parser = new JSONParser();
                JSONObject message = (JSONObject) parser.parse(temp);
                JSONObject returnMessage = this.constructNamedMessage((String) message.get(Controller.NAME_KEY),
                        (String) message.get(Controller.MESSAGE_KEY), Color.BLUE, Color.BLACK);
                this.broadcast(returnMessage.toString());
            }//end while

//            this.participants.get(0).sendMessage("This is a test");
        } catch (Exception e) {
            System.err.println("There was an error with the connection of a participant");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Stack Trace:"); e.printStackTrace();
            System.err.println("Cause: " + e.getCause());
        }//end try/catch
    }//end run()

    /**
     * Sends a message out to all of the participants
     * @param message The message to send out to all of the participants
     */
    public void broadcast(String message) {
        // TODO: This should be in another thread
        for (Participant participant : this.participants) {
            try {
                participant.sendMessage(message);
            } catch (IOException e) {
                System.err.println("There was an error sending the message:" + message + " to participant: " +
                        participant.getName());
                System.err.println("Message: " + e.getMessage());
                System.err.println("Cause: " + e.getCause());
                System.err.println("Stack Trace:"); e.printStackTrace();
            }//end try/catch
        }//end for
    }//end broadcast()

    /**
     * Constructs the JSONObject for a named object (as according to the README)
     * @param name The name of the participant that sent the message
     * @param message The message of the participant
     * @param nameColor The color of the name
     * @return The constructed JSONObject with all of the information
     */
    @SuppressWarnings("DuplicatedCode")
    public JSONObject constructNamedMessage(String name, String message, Color nameColor, Color messageColor) {
        JSONObject nameObject = new JSONObject();
        nameObject.put(Controller.TEXT_KEY, name);

        JSONObject nameColorObject = new JSONObject();
        nameColorObject.put(Controller.RED_KEY, nameColor.getRed());
        nameColorObject.put(Controller.GREEN_KEY, nameColor.getGreen());
        nameColorObject.put(Controller.BLUE_KEY, nameColor.getBlue());

        nameObject.put(Controller.COLOR_KEY, nameColorObject);

        JSONObject messageObject = new JSONObject();
        messageObject.put(Controller.TEXT_KEY, message);

        JSONObject messageColorObject = new JSONObject();
        messageColorObject.put(Controller.RED_KEY, messageColor.getRed());
        messageColorObject.put(Controller.GREEN_KEY, messageColor.getGreen());
        messageColorObject.put(Controller.BLUE_KEY, messageColor.getBlue());

        messageObject.put(Controller.COLOR_KEY, messageColorObject);

        JSONObject namedMessageObject = new JSONObject();
        namedMessageObject.put(Controller.NAME_KEY, nameObject);
        namedMessageObject.put(Controller.MESSAGE_KEY, messageObject);

        return namedMessageObject;
    }//end constructNamedMessage()
}//end com.company.Controller
