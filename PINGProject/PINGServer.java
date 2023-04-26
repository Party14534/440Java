import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PINGServer{

    public static String clientID = "";
    public static String seqNum = "";

    //Gets CLient Id and seq number from message
    public static void getVals(String msg){

        //Split the string into lines
        String[] msgLines = msg.split("\n",0);

        //For loop that runs until it finds a ':' and then gets the values after that
        String clientIDLine = msgLines[2];
        for(int i = 0; i < clientIDLine.length(); i++){
            if(clientIDLine.charAt(i) == ':'){
                clientID = clientIDLine.substring(i+1, clientIDLine.length());
                break;
            }
        }
        String seqNumLine = msgLines[3];
        for(int i = 0; i < seqNumLine.length(); i++){
            if(seqNumLine.charAt(i) == ':'){
                seqNum = seqNumLine.substring(i+1, seqNumLine.length());
                break;
            }
        }

    }

    //Sends the response to client
    public static void sendResponse(DatagramSocket socket, DatagramPacket receivePacket, byte[] buffer, boolean accepted) throws IOException{

        //Creates string that holds the data from the received packets buffer
        String data = new String(buffer);
        data = data.substring(0, data.indexOf('\0'));

        //If the packet was dropped sets it as dropped
        String error;
        if(accepted) error = "RECEIVED";
        else error = "DROPPED";

        //Gets values from packet
        getVals(data);

        //Prints out packet info
        System.out.println("\nIP:" + receivePacket.getAddress().toString().substring(1) + " :: Port:" + receivePacket.getPort() +
        " :: ClientID:" + clientID + " :: SEQ#:" + seqNum + " :: " + error + "\n");

        //Returns if packet dropped
        if(!accepted) return;

        //Prints out msg from client
        String msgLines[] = data.split("\n",0);
        for(int i = 0; i < msgLines.length; i++){
            if(i != 0 && i != 6)System.out.println(msgLines[i]);
            else if(i == 0) System.out.println("---------- Received Ping Request Packet Header ----------");
            else if(i == 6) System.out.println("--------- Received Ping Request Packet Payload ------------");
        }
        System.out.println();

        //Creates a send buffer
        byte[] sBuffer = null;

        //Sets dividing lines to reflect the sender
        msgLines[0] = "---------- Received Ping Response Packet Header ----------";
        msgLines[6] = "---------- Received Ping Response Packet Payload ----------";
        data = "";

        //Rebuilds packet
        for(int i = 0; i < msgLines.length; i++){

            //Used to set payload to uppercase
            if(i > 6){
                for(int j = 0; j < msgLines[i].length(); j++){
                    if(msgLines[i].charAt(j) == ':'){
                        String before = msgLines[i].substring(0,j);
                        String after = msgLines[i].substring(j,msgLines[i].length()).toUpperCase();
                        msgLines[i] = before + after;
                        break;
                    }
                }
            }
            data += msgLines[i] + "\n";
        }

        //Prints out new packet to be sent to client
        msgLines = data.split("\n",0);
        for(int i = 0; i < msgLines.length; i++){
            if(i != 6 && i != 0)System.out.println(msgLines[i]);
            if(i == 0) System.out.println("---------- Ping Response Packet Header ----------");
            if(i == 6) System.out.println("---------- Ping Response Packet Payload ----------");
        }

        //Sets send buffer from msg
        sBuffer = data.getBytes();

        //Creates and sends packet
        DatagramPacket sendPacket = new DatagramPacket(sBuffer, sBuffer.length, receivePacket.getAddress(), receivePacket.getPort());

        socket.send(sendPacket);

    }


    //Main
    public static void main(String[] args) throws IOException{

        //Throwing error if there are not enough arguments
        if(args.length != 2){
            System.out.println("ERR - arg " + args.length);
            return;
        }

        //Get port and loss from the input
        int port = Integer.parseInt(args[0]);
        int loss = Integer.parseInt(args[1]);

        //Throw error if port is invalid
        if(port < 0 || port > 65535){
            System.out.println("ERR - Invalid Port");
            return;
        }

        //create socket
        DatagramSocket socket;

        //Throws error if port is in use
        try{
            socket = new DatagramSocket(port);
        } catch(IOException e){
            System.out.println("ERR - cannot create PINGServer socket using port number " + port);
            return;
        }

        //Prints out server info
        System.out.println("PINGServer started with server IP: " + InetAddress.getLocalHost().toString().substring(0)
        + ", port: " + port + " ...\n");

        //Creates packet
        DatagramPacket receivePacket = null;

        //While loop that runs forever
        while(true){

            //Creates buffer
            byte[] buffer = new byte[65535];

            //Create a receive packet
            receivePacket = new DatagramPacket(buffer, buffer.length);

            //Receive packet on socket
            socket.receive(receivePacket);

            //generate random number for loss
            int randNumber = (int)Math.floor(Math.random() * (100));

            //Sends response packet
            if(loss > randNumber) sendResponse(socket, receivePacket, buffer, true);
            else sendResponse(socket, receivePacket, buffer, false);

        }

    }

}