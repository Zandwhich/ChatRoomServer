/*
 * Author: Alex Zdanowicz
 */

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
    ArrayList<Participant> participants;

    /* Constructor */

    /**
     * The default constructor
     */
    public Controller() {
        this.participants = new ArrayList<>();
    }//end Controller()

    /**
     * The method that runs the server-side application
     */
    public void run() {
        try {
            System.out.println("IP Address: " + Inet4Address.getLocalHost());
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
        }//end try/catch
    }//end run()
}//end Controller
