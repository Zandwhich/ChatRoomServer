/*
 * Author: Alex Zdanowicz
 */

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
        this.port = 1024;
    }//end Controller()

    /**
     * The method that runs the server-side application
     */
    public void run() {
        try {
            System.out.println("IP Address: " + Inet4Address.getLocalHost());

            ServerSocket serverSocket = new ServerSocket(1024);
            // TODO: Multi-thread this
            Socket client = serverSocket.accept();

            Participant participant = new Participant("Participant", client, this);
            this.participants.add(participant);

            System.out.println("Connected to a client computer: " + participant.getInetAddress() + " on local port " +
                    participant.getLocalPort());

            this.participants.get(0).sendMessage("This is a test");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
        }//end try/catch
    }//end run()
}//end Controller
