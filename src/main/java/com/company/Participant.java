/*
 * Author: Alex Zdanowicz
 */

package com.company;

import java.awt.*;
import java.io.*;
import java.net.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/** The class that represents a participant in a chat */
public class Participant {

  /** The thread to deal with inputs from the participant */
  private class InputThread extends Thread {

    /** The JSONParser used in parsing Strings into JSONObjects */
    final JSONParser parser;

    /** A reference to the participant */
    final Participant participant;

    /** If the participant is connected */
    public Boolean connected = true;

    /**
     * The constructor for the input thread
     *
     * @param threadName The name of the thread
     */
    public InputThread(String threadName, Participant participant) {
      super(threadName);
      this.parser = new JSONParser();
      this.participant = participant;
    }

    /** Deals with receiving inputs from the participant */
    @Override
    public void run() {
      while (connected) {
        if (!processNextMessage()) break;
      }
    }

    /**
     * Reads, deserializes, and broadcasts a single message from the participant.
     *
     * @return true if the loop should continue, false if the participant disconnected
     */
    private boolean processNextMessage() {
      String input;
      try {
        input = this.readInput();
      } catch (IOException e) {
        this.handleDisconnect();
        return false;
      }

      JSONObject jsonInput = this.deserializeMessageFromParticipant(input);
      if (jsonInput == null) return true;

      this.broadcastMessage(jsonInput);
      return true;
    }

    /**
     * Reads in the input from the participant
     *
     * @return The input from the participant in a String form
     */
    private String readInput() throws IOException {
      return in.readUTF();
    }

    /**
     * Deserializes a message from the participant from a String into a JSONObject
     *
     * @param message The JSON object received from the participant as a String
     * @return The JSON object received from the participant as a JSONObject
     */
    private JSONObject deserializeMessageFromParticipant(String message) {
      JSONObject jsonObject = null;
      try {
        jsonObject = (JSONObject) this.parser.parse(message);
      } catch (ParseException e) {
        System.err.println(
            "An error occurred while trying to parse a message from participant "
                + name
                + " into JSON. Will continue on");
        System.err.println("Message from the participant: " + message);
        System.err.println("Error Message: " + e.getMessage());
        System.err.println("Cause of error: " + e.getCause());
        System.err.println("Stack Trace:");
        e.printStackTrace();
      }
      return jsonObject;
    }

    /**
     * Prints out errors if there is a malformed message from the participant
     *
     * @param participantMessage The malformed message from the participant
     */
    private void malformedParticipantMessage(JSONObject participantMessage) {
      System.err.println("There was a malformed message from participant: " + name);
      System.err.println("Malformed message: " + participantMessage.toString());
    }

    /**
     * Broadcasts this message to all the participants
     *
     * @param jsonMessage The message to broadcast as a JSON object
     */
    private void broadcastMessage(JSONObject jsonMessage) {
      String message = this.extractField(jsonMessage, Controller.MESSAGE_KEY);
      if (message == null) return;

      String name = this.extractField(jsonMessage, Controller.NAME_KEY);
      if (name == null) return;

      controller.sendMessage(name, nameColor, message, Controller.MESSAGE_COLOR);
    }

    /**
     * Extracts a required string field from a JSON message.
     *
     * @param jsonMessage The JSON message to extract from
     * @param key The key to extract
     * @return The field value, or null if missing or null
     */
    private String extractField(JSONObject jsonMessage, String key) {
      if (!jsonMessage.containsKey(key) || jsonMessage.get(key) == null) {
        this.malformedParticipantMessage(jsonMessage);
        return null;
      }
      return (String) jsonMessage.get(key);
    }

    private void handleDisconnect() {
      controller.disconnectParticipant(participant);
      connected = false;
    }
  }

  /** The name of the input thread */
  public static final String INPUT_THREAD_NAME = "Input Thread";

  /** Name of the participant */
  private final String name;

  /** The colour for the participant's name */
  private Color nameColor;

  /** Socket of participant */
  private final Socket client;

  /** The controller with which to talk */
  private final Controller controller;

  /** Input stream from which to receive messages */
  private final DataInputStream in;

  /** Output stream from which to send messages */
  private final DataOutputStream out;

  /**
   * The constructor of the com.company.Participant class
   *
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

    InputThread inputThread = new InputThread(Participant.INPUT_THREAD_NAME, this);
    inputThread.start();
  }

  /**
   * @return The name of the participant
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return The socket of the participant
   */
  public Socket getClient() {
    return this.client;
  }

  /**
   * @return The local port of the participant's connection
   */
  public int getLocalPort() {
    return this.client.getLocalPort();
  }

  /**
   * @return The remote port of the participant's connection
   */
  public int getRemotePort() {
    return this.client.getPort();
  }

  /**
   * @return The internet address of the participant
   */
  public InetAddress getInetAddress() {
    return this.client.getInetAddress();
  }

  /**
   * Sends a message to the participant
   *
   * @param message The message that is sent to the participant
   * @throws IOException If there's an error sending the message
   */
  public void sendMessage(String message) throws IOException {
    this.out.writeUTF(message);
  }

  /**
   * Retrieves messages from the participant
   *
   * @return The message from the participant
   * @throws IOException If there's an error reading the message
   */
  public String retrieveMessage() throws IOException {
    return this.in.readUTF();
  }
}
