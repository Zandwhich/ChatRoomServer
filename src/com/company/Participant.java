/*
 * Author: Alex Zdanowicz
 */

package com.company;

import json_simple.JSONObject;
import json_simple.parser.JSONParser;
import json_simple.parser.ParseException;

import java.awt.*;
import java.net.*;
import java.io.*;

/**
 * The class that represents a participant in a chat
 */
public class Participant {

    /* Internal Classes */

    /**
     * The thread to deal with inputs from the participant
     */
    private class InputThread extends Thread {

        /* Fields */

        // Variables

        /**
         * The JSONParser used in parsing Strings into JSONObjects
         */
        JSONParser parser;

        /* Constructors */

        /**
         * The constructor for the input thread
         * @param threadName The name of the thread
         */
        public InputThread(String threadName) {
            super(threadName);
            this.parser = new JSONParser();
        }//end InputThread()

        /* Methods */

        // Public

        /**
         * Deals with receiving inputs from the participant
         */
        @Override
        public void run() {
            String input;

            while(true) {
                try {
                    input = this.readInput();
                } catch (IOException e) {
                    System.err.println("There was an error while trying to read in data from the participant");
                    System.err.println("Message: " + e.getMessage());
                    System.err.println("Cause: " + e.getCause());
                    System.err.println("Stack Trace:"); e.printStackTrace();
                    break;
                }//end try/catch

                JSONObject jsonInput = this.deserializeMessageFromParticipant(input);

                // There was an error with reading in this message; skipping over it and continuing on
                if (jsonInput == null) continue;

                this.broadcastMessage(jsonInput);
            }//end while
        }//end run()

        // Private

        /**
         * Reads in the input from the participant
         * @return The input from the participant in a String form
         */
        private String readInput() throws IOException {
            return in.readUTF();
        }//end readInput()

        /**
         * Deserializes a message from the participant from a String into a JSONObject
         * @param message The JSON object received from the participant as a String
         * @return The JSON object received from the participant as a JSONObject
         */
        private JSONObject deserializeMessageFromParticipant(String message) {
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) this.parser.parse(message);
            } catch (ParseException e) {
                System.err.println("An error occurred while trying to parse a message from participant " + name +
                        " into JSON. Will continue on");
                System.err.println("Message from the participant: " + message);
                System.err.println("Error Message: " + e.getMessage());
                System.err.println("Cause of error: " + e.getCause());
                System.err.println("Stack Trace:"); e.printStackTrace();
            }//end try/catch
            return jsonObject;
        }//end deserializeMessageFromParticipant()

        /**
         * Prints out errors if there is a malformed message from the participant
         * @param participantMessage The malformed message from the participant
         */
        private void malformedParticipantMessage(JSONObject participantMessage) {
            System.err.println("There was a malformed message from participant: " + name);
            System.err.println("Malformed message: " + participantMessage.toString());
        }//end malformedParticipantMessage()

        /**
         * Broadcasts this message to all of the participants
         * @param jsonMessage The message to broadcast as a JSON object
         */
        private void broadcastMessage(JSONObject jsonMessage) {
            if (!jsonMessage.containsKey(Controller.MESSAGE_KEY) || jsonMessage.get(Controller.MESSAGE_KEY) == null) {
                this.malformedParticipantMessage(jsonMessage);
                return;
            }//end if

            String message = (String) jsonMessage.get(Controller.MESSAGE_KEY);

            if (!jsonMessage.containsKey(Controller.NAME_KEY) || jsonMessage.get(Controller.NAME_KEY) == null) {
                this.malformedParticipantMessage(jsonMessage);
                return;
            }//end if

            String name = (String) jsonMessage.get(Controller.NAME_KEY);

            controller.sendMessage(name, nameColor, message, Controller.MESSAGE_COLOR);
        }//end sendMessage()
    }//end InputThread

    /* Fields */

    // Constants

    /**
     * The name of the input thread
     */
    public static final String INPUT_THREAD_NAME = "Input Thread";

    // Variables

    /**
     * Name of the participant
     */
    private final String name;

    /**
     * The color for the participant's name
     */
    private Color nameColor;

    /**
     * Socket of participant
     */
    private final Socket client;

    /**
     * The controller with which to talk
     */
    private final Controller controller;

    /**
     * Input stream from which to receive messages
     */
    private final DataInputStream in;

    /**
     * Output stream from which to send messages
     */
    private final DataOutputStream out;


    /* Constructors */

    /**
     * The constructor of the com.company.Participant class
     * @param name The name of the participant
     * @param client The socket of the participant
     * @param controller The controller that controls everything
     * @throws IOException When there is a faulty connection
     */
    public Participant(String name, Socket client, Controller controller) throws IOException {
        this.name = name;
        this.client = client;
        this.controller = controller;
        this.in = new DataInputStream(this.client.getInputStream());
        this.out = new DataOutputStream(this.client.getOutputStream());

        InputThread inputThread = new InputThread(Participant.INPUT_THREAD_NAME);
        inputThread.start();
    }//end com.company.Participant()


    /* Methods */

    // Getters
    public String getName() { return this.name; }//end getName()
    public Socket getClient() { return this.client; }//end getClient()
    public int getLocalPort() { return this.client.getLocalPort(); }//end getLocalPort()
    public int getRemotePort() { return this.client.getPort(); }//end getRemotePort()
    public InetAddress getInetAddress() { return this.client.getInetAddress(); }//end getInetAddress

    /**
     * Sends a message to the participant
     * @param message The message that is sent to the participant
     * @throws IOException Where there's an error? Idk man...
     */
    public void sendMessage(String message) throws IOException {
        this.out.writeUTF(message);
    }//end sendMessage()

    /**
     * Retrieves messages from the participant
     * @return The message from the participant
     * @throws IOException I think this is for when there's an error (lol)
     */
    public String retrieveMessage() throws IOException {
        return this.in.readUTF();
    }//end retrieveMessage()


}//end com.company.Participant
