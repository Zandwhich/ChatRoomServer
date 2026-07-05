/*
 * Author: Alex Zdanowicz
 */

package com.company;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
     * The key for the colour object
     */
    public static final String COLOR_KEY = "color";

    /**
     * The key for the red value in the colour
     */
    public static final String RED_KEY = "red";

    /**
     * The key for the green value in the colour
     */
    public static final String GREEN_KEY = "green";

    /**
     * The key for the blue value in the colour
     */
    public static final String BLUE_KEY = "blue";

    /**
     * The colour to use for messages from people
     */
    public static final Color MESSAGE_COLOR = Color.BLACK;

    /**
     * The colour to use when the system sends a message
     */
    public static final Color SYSTEM_MESSAGE_COLOR = Color.GRAY;

    /**
     * The second half of the message to print when someone connects
     */
    public static final String PARTICIPANT_CONNECTED_MESSAGE = " has joined the chat";

    public static final String PARTICIPANT_DISCONNECTED_MESSAGE = " has left the chat";

    /**
     * The list of participants in the group
     */
    private final ArrayList<Participant> participants;

    /**
     * The JSONParser used to parse the initial connection message
     */
    private final JSONParser parser;

    /**
     * The default constructor
     */
    public Controller() {
        this.participants = new ArrayList<>();
        this.parser = new JSONParser();
    }

    /**
     * Prints the IP address of this machine.
     * @return true if successful, false if the server should shut down
     */
    private boolean printLocalAddress() {
        try {
            System.out.println("IP Address: " + Inet4Address.getLocalHost());
            return true;
        } catch (UnknownHostException e) {
            System.err.println("There was an error printing out the IP Address of the local host.\n" +
                    "Shutting down the program");
            this.printErrorMessage(e);
            return false;
        }
    }

    /**
     * Creates the server socket on the configured port.
     * @return the ServerSocket, or null if it could not be created
     */
    private ServerSocket createServerSocket() {
        try {
            return new ServerSocket(Controller.PORT);
        } catch (IOException e) {
            System.err.println("There was an error creating the server socket. Breaking out");
            this.printErrorMessage(e);
            return null;
        }
    }

    /**
     * Continuously accepts new client connections until an unrecoverable error occurs.
     * @param serverSocket the socket to accept connections on
     */
    private void acceptConnections(ServerSocket serverSocket) {
        while (true) {
            Socket client = this.acceptClient(serverSocket);
            if (client == null) break;

            this.handleNewConnection(client);
        }
    }

    /**
     * Blocks until a new client connects.
     * @param serverSocket the socket to accept on
     * @return the connected client socket, or null on error
     */
    private Socket acceptClient(ServerSocket serverSocket) {
        try {
            System.out.println("Waiting on port: " + Controller.PORT);
            return serverSocket.accept();
        } catch (IOException e) {
            System.err.println("There was an error creating the client connection. Breaking out");
            this.printErrorMessage(e);
            return null;
        }
    }

    /**
     * Sets up a newly-connected client as a participant and registers them.
     * If the participant can't be created (e.g. their streams fail to open),
     * this client is dropped but the server keeps running and accepting others.
     * @param client the newly-connected socket
     */
    private void handleNewConnection(Socket client) {
        String name = this.readParticipantName(client);

        Participant participant = this.createParticipant(name, client);
        if (participant == null) return;

        this.registerParticipant(participant);
    }

    /**
     * Reads and parses the initial handshake message from a client to get their name.
     * Falls back to an empty name if reading or parsing fails.
     * @param client the client to read from
     * @return the participant's name, or "" if it couldn't be determined
     */
    private String readParticipantName(Socket client) {
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
        }
        return name;
    }

    /**
     * Wraps a client socket in a Participant.
     * @param name the participant's name
     * @param client the client socket
     * @return the new Participant, or null if construction failed
     */
    private Participant createParticipant(String name, Socket client) {
        try {
            return new Participant(name, client, this);
        } catch (IOException e) {
            System.err.println("There was an error creating a participant. Breaking out");
            this.printErrorMessage(e);
            return null;
        }
    }

    /**
     * Adds a participant to the group and announces their arrival.
     * @param participant the newly-connected participant
     */
    private void registerParticipant(Participant participant) {
        this.participants.add(participant);
        System.out.println("Connected to a client computer: " + participant.getInetAddress() +
                " on local port " + participant.getLocalPort() + " (" + participant.getName() + ")");
        this.sendParticipantConnectedMessage(participant);
    }

    /**
     * Removes a participant from the group and announces their departure.
     * @param participant the disconnected participant
     */
    public void disconnectParticipant(Participant participant) {
        this.participants.remove(participant);
        System.out.println("Participant disconnected: " + participant.getInetAddress() +
                " on local port " + participant.getLocalPort() + " (" + participant.getName() + ")");
        this.sendParticipantDisconnectedMessage(participant);
    }

    public void run() {
        if (!printLocalAddress()) return;

        ServerSocket serverSocket = createServerSocket();
        if (serverSocket == null) return;

        acceptConnections(serverSocket);
    }

    /**
     * Sends a message to all the participants
     * @param name The name of the participant sending the message
     * @param nameColor The colour of the name of the participant
     * @param message The message that the participant has sent
     * @param messageColor The colour of the message
     */
    public void sendMessage(String name, Color nameColor, String message, Color messageColor) {
        JSONObject messageObject = this.constructNamedMessage(name, message, nameColor, messageColor);
        this.broadcast(messageObject.toString());
    }

    /**
     * Sends a message to all the participants
     * @param message The message
     * @param messageColor The colour of the message
     */
    public void sendMessage(String message, Color messageColor) {
        JSONObject messageObject = this.constructMessage(message, messageColor);
        this.broadcast(messageObject.toString());
    }

    /**
     * Constructs the JSONObject for a named object (as according to the README)
     * @param name The name of the participant that sent the message
     * @param message The message of the participant
     * @param nameColor The colour of the name
     * @return The constructed JSONObject with all the information
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
    }

    /**
     * Constructs the JSONObject for a message without a name
     * (Mostly used for the initial message)
     * @param message The message
     * @param messageColor The colour of the message
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
    }

    /**
     * Sends a message out to all the participants
     * @param message The message to send out to all the participants
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
            }
        }
    }

    /**
     * Prints out a series of error messages when one happens
     * @param e The exception
     */
    private void printErrorMessage(Exception e) {
        System.err.println("Port number: " + Controller.PORT);
        System.err.println("Message: " + e.getMessage());
        System.err.println("Cause: " + e.getCause());
        System.err.println("Stack Trace:"); e.printStackTrace();
    }

    private void sendSystemMessage(String message) {
        this.sendMessage(message, Controller.SYSTEM_MESSAGE_COLOR);
    }

    /**
     * Broadcasts a message when someone joins the chat
     * @param participant The participant that just joined the chat
     */
    private void sendParticipantConnectedMessage(Participant participant) {
        this.sendSystemMessage(participant.getName() + Controller.PARTICIPANT_CONNECTED_MESSAGE);
    }

    /**
     * Broadcasts a message when someone leaves the chat
     * @param participant The participant that just left the chat
     */
    public void sendParticipantDisconnectedMessage(Participant participant) {
        this.sendSystemMessage(participant.getName() + Controller.PARTICIPANT_DISCONNECTED_MESSAGE);
    }

    /**
     * Gets the name out of the initial message that someone sends
     * @param message The initial message that a participant sends
     * @return The name of the participant
     */
    private String getNameOutOfInitialMessage(String message) throws ParseException {
        JSONObject jsonObject = (JSONObject) this.parser.parse(message);

        return (String) jsonObject.get(Controller.NAME_KEY);
    }
}