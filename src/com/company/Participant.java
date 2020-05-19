package com.company;/*
 * Author: Alex Zdanowicz
 */

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
        private JSONParser parser;

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

                    System.out.println("In the 'run' method of the InputThread waiting for input from the participant\n");

                    input = this.readInput();

                    System.out.println("In the 'run' method of the InputThread, just read the input from the participant: " + input + "\n");

                } catch (IOException e) {
                    System.err.println("There was an error while trying to read in data from the participant");
                    System.err.println("Message: " + e.getMessage());
                    System.err.println("Cause: " + e.getCause());
                    System.err.println("Stack Trace:"); e.printStackTrace();
                    break;
                }//end try/catch

                System.out.println("In the 'run' method of the InputThread, about to deserialize the input into a JSON\n");

                JSONObject jsonInput = this.deserializeMessageFromParticipant(input);

                // There was an error with reading in this message; skipping over it and continuing on
                if (jsonInput == null) {

                    System.out.println("There was an error deserializing the message, and the jsonInput is null\n");

                    continue;
                }//end if

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
    private String name;

    /**
     * The color for the participant's name
     */
    private Color nameColor;

    /**
     * Socket of participant
     */
    private Socket client;

    /**
     * The controller with which to talk
     */
    private Controller controller;

    /**
     * Input stream from which to receive messages
     */
    private DataInputStream in;

    /**
     * Output stream from which to send messages
     */
    private DataOutputStream out;


    /* Constructors */

    /**
     * The constructor of the com.company.Participant class
     * @param name The name of the participant
     * @param client The socket of the participant
     * @param controller The controller that controls everything
     * @throws IOException TODO: Fill this in
     */
    public Participant(String name, Socket client, Controller controller) throws IOException /* TODO: Figure out where to throw the exception */ {

        System.out.println("In the constructor for the participant " + name + "\n");

        this.name = name;
        this.client = client;
        this.controller = controller;

        System.out.println("Creating the DataInputStream for the participant\n");

        this.in = new DataInputStream(this.client.getInputStream());

        System.out.println("Finished creating the DataInputStream for the participant\nCreating the DataOutputStream for the participant\n");

        this.out = new DataOutputStream(this.client.getOutputStream());

        System.out.println("Finished creating the DataOutputStream for the participant\nCreating the input thread\n");

        InputThread inputThread = new InputThread(Participant.INPUT_THREAD_NAME);

        System.out.println("Finished creating the input thread\nFinished the participant constructor\n");
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
     * @throws IOException TODO: Fill this in
     */
    public void sendMessage(String message) throws IOException /* TODO: Figure out where to throw the exception */ {

        System.out.println("Sending a message out to participant: " + this.name + "\nMessage: " + message + "\n");

        this.out.writeUTF(message);

        System.out.println("Sent out a message to participant: " + this.name + "\n");
    }//end sendMessage()


}//end com.company.Participant
