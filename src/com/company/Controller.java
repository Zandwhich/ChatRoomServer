package com.company;/*
 * Author: Alex Zdanowicz
 */

import json_simple.JSONObject;
import json_simple.parser.JSONParser;
import json_simple.parser.ParseException;

import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
    public static final int PORT = 1024;

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

    /**
     * The color to use for messages from people
     */
    public static final Color MESSAGE_COLOR = Color.BLACK;

    /**
     * The color to use for the initial message when someone connects
     */
    public static final Color INITIAL_MESSAGE_COLOR = Color.GRAY;

    /**
     * The second half of the message to print when someone connects
     */
    public static final String JUST_CONNECTED_MESSAGE = " has joined the chat";

    // Variables

    /**
     * The list of participants in the group
     */
    private ArrayList<Participant> participants;

    /**
     * The JSONParser used to parse the initial connection message
     */
    private JSONParser parser;


    /* Constructors */

    /**
     * The default constructor
     */
    public Controller() {
        this.participants = new ArrayList<>();
        this.parser = new JSONParser();
    }//end com.company.Controller()

    /* Methods */

    // Public

    /**
     * The method that runs the server-side application
     */
    public void run() {

        // Print out the IP Address of this computer
        try {
            System.out.println("IP Address: " + Inet4Address.getLocalHost());
        } catch (UnknownHostException e) {
            System.err.println("There was an error printing out the IP Address of the local host.\n" +
                    "Shutting down the program");
            this.printErrorMessage(e);
            return;
        }//end try/catch

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(Controller.PORT);
        } catch (IOException e) {
            System.err.println("There was an error creating the server socket. Breaking out");
            this.printErrorMessage(e);
            return;
        }//end try/catch

        while (true) {
            Socket client;
            try {
                System.out.println("Waiting on port: " + Controller.PORT);
                client = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("There was an error creating the client connection. Breaking out");
                this.printErrorMessage(e);
                break;
            }//end try/catch

            String input = "";
            String name = "";
            try {
                DataInputStream in = new DataInputStream(client.getInputStream());
                input = in.readUTF();
                name = getNameOutOfInitialMessage(input);
            } catch (IOException e) {
                System.err.println("There was an error creating the temporary client connection. Continuing on");
                this.printErrorMessage(e);
            } catch (ParseException e) {
                System.err.println("There was an error parsing the name out of the initial message.\nInitial message: " + input);
            }//end try/catch

            Participant participant;
            try {
                participant = new Participant(name, client, this);
            } catch (IOException e) {
                System.err.println("There was an error creating a participant. Breaking out");
                this.printErrorMessage(e);
                break;
            }// end try/catch
            this.participants.add(participant);
            System.out.println("Connected to a client computer: " + participant.getInetAddress() + " on local port " +
                    participant.getLocalPort());
            this.initialConnectionMessage(name);
            //this.port++;
        }//end while true
    }//end run()

    /**
     * Sends a message to all of the participants
     * @param name The name of the participant sending the message
     * @param nameColor The color of the name of the participant
     * @param message The message that the participant has sent
     * @param messageColor The color of the message
     */
    public void sendMessage(String name, Color nameColor, String message, Color messageColor) {
        JSONObject messageObject = this.constructNamedMessage(name, message, nameColor, messageColor);
        this.broadcast(messageObject.toString());
    }//end sendMessage()

    /**
     * Sends a message to all of the participants
     * @param message The message
     * @param messageColor The color of the message
     */
    public void sendMessage(String message, Color messageColor) {
        JSONObject messageObject = this.constructMessage(message, messageColor);
        this.broadcast(messageObject.toString());
    }//end sendMessage()

    // Private

    /**
     * Constructs the JSONObject for a named object (as according to the README)
     * @param name The name of the participant that sent the message
     * @param message The message of the participant
     * @param nameColor The color of the name
     * @return The constructed JSONObject with all of the information
     */
    @SuppressWarnings("DuplicatedCode")
    private JSONObject constructNamedMessage(String name, String message, Color nameColor, Color messageColor) {
        JSONObject nameObject = new JSONObject();
        nameObject.put(Controller.TEXT_KEY, name);

        // TODO: Uncomment this later when we're ready for colors
//        JSONObject nameColorObject = new JSONObject();
//        nameColorObject.put(Controller.RED_KEY, nameColor.getRed());
//        nameColorObject.put(Controller.GREEN_KEY, nameColor.getGreen());
//        nameColorObject.put(Controller.BLUE_KEY, nameColor.getBlue());
//
//        nameObject.put(Controller.COLOR_KEY, nameColorObject);

        JSONObject messageObject = new JSONObject();
        messageObject.put(Controller.TEXT_KEY, message);

        // TODO: Uncomment this later when we're ready for colors
//        JSONObject messageColorObject = new JSONObject();
//        messageColorObject.put(Controller.RED_KEY, messageColor.getRed());
//        messageColorObject.put(Controller.GREEN_KEY, messageColor.getGreen());
//        messageColorObject.put(Controller.BLUE_KEY, messageColor.getBlue());
//
//        messageObject.put(Controller.COLOR_KEY, messageColorObject);

        JSONObject namedMessageObject = new JSONObject();
        namedMessageObject.put(Controller.NAME_KEY, nameObject);
        namedMessageObject.put(Controller.MESSAGE_KEY, messageObject);

        return namedMessageObject;
    }//end constructNamedMessage()

    /**
     * Constructs the JSONObject for a message without a name
     * (Mostly used for the initial message)
     * @param message The message
     * @param messageColor The color of the message
     * @return The JSONObject constructed and ready to be sent
     */
    private JSONObject constructMessage(String message, Color messageColor) {
        JSONObject messageObject = new JSONObject();
        messageObject.put(Controller.TEXT_KEY, message);

        // TODO: Uncomment this later when we're ready for colors
//        JSONObject messageColorObject = new JSONObject();
//        messageColorObject.put(Controller.RED_KEY, messageColor.getRed());
//        messageColorObject.put(Controller.GREEN_KEY, messageColor.getGreen());
//        messageColorObject.put(Controller.BLUE_KEY, messageColor.getBlue());
//
//        messageObject.put(Controller.COLOR_KEY, messageColorObject);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Controller.MESSAGE_KEY, messageObject);

        return jsonObject;
    }//end constructMessage()

    /**
     * Sends a message out to all of the participants
     * @param message The message to send out to all of the participants
     */
    private void broadcast(String message) {
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
     * Prints out a series of error messages when one happens
     * @param e The exception
     */
    private void printErrorMessage(Exception e) {
        System.err.println("Port number: " + Controller.PORT);
        System.err.println("Message: " + e.getMessage());
        System.err.println("Cause: " + e.getCause());
        System.err.println("Stack Trace:"); e.printStackTrace();
    }//end printErrorMessage()

    /**
     * Sends out to everyone the message when someone first connects
     * @param name The name of the person who just connected
     */
    private void initialConnectionMessage(String name) {
        this.sendMessage(name + Controller.JUST_CONNECTED_MESSAGE, Controller.INITIAL_MESSAGE_COLOR);
    }//end initialConnectionMessage()

    /**
     * Gets the name out of the initial message that someone sends
     * @param message The initial message that a participant sends
     * @return The name of the participant
     */
    private String getNameOutOfInitialMessage(String message) throws ParseException {
        JSONObject jsonObject = (JSONObject) this.parser.parse(message);

        return (String) jsonObject.get(Controller.NAME_KEY);
    }//end getNameOutOfInitialMessage()
}//end com.company.Controller
