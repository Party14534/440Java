import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class PINGClient {

    public static int seqNum = 1;
    public static int pingReqPackets = 0;
    public static int pingResPackets = 0;
    public static double minRTT = Double.MAX_VALUE;
    public static double maxRTT = Double.MIN_VALUE;
    public static double avgRTT = 0;
    public static double totalRTT = 0;
    public static double currentPayload = 0;    
    public static double avgPayload = 0;
    public static double totalLoad = 0;
    public static int numRTT = 0;
    public static int numPayload = 0;

    //Function that builds the send packet
    public static String buildMsg(int i, int ClientID, String timeStamp, String hostname){

        int payloadSize = (int)Math.floor(Math.random() * (300 - 150 + 1) + 150); //Gets random payload size
        totalLoad += payloadSize;
        numPayload++;
        avgPayload = totalLoad/numPayload;

        //Creating default payload packet without rest to get size
        String payload = "--------- Ping Request Packet Payload ------------\n" +
        "Host: " + hostname + "\n" +
        "Class-name: VCU-CMSC440-SPRING-2023\n" +
        "User-name: Dellimore, Zachariah\n" +
        "Rest: ";
        String payloadEnd = "---------------------------------------\0";

        int bits = payloadSize - payload.getBytes().length; //Gets the number of bits that need to be added

        String rest = ""; //String that will hold the rest

        //For loop that generates a random alpha character
        for(int j = 0; j < bits; j++){
            int randNum = (int)Math.floor(Math.random() * (62));
            if(randNum >= 36) randNum+=6;
            if(randNum >= 10) randNum+=7;
            randNum+=48;
        
            rest += (char)randNum;
        }
        rest+="\n";

        //Adds payload and randdom chars to packets
        String msg = "---------- Ping Request Packet Header ----------\n" +
        "Version: 1\n"+
        "Client ID: " + ClientID + "\n" +
        "Sequence No.: " + i + "\n" +
        "Time: " + timeStamp + "\n" +
        "Payload Size: " + payloadSize + "\n" +
        payload + rest + payloadEnd;

        return msg;
    }
    
    //Returns true if packet sent successfully
    public static boolean sendPacket(DatagramSocket socket, DatagramPacket packet, int wait) throws IOException{

        //Sends packet on socket
        socket.send(packet); 

        //Sets the sockets timeour
        socket.setSoTimeout(wait*1000);

        //Creates message buffer
        byte[] buffer = new byte[65535];

        //Creates a receive packet
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        //Try catch loop that tries to receive until there is a timeout
        try{
            socket.receive(receivePacket); //Receives packet
            String returnData = new String(buffer);

            //Prints out packet
            String msgLines[] = returnData.split("\n",0);
            for(int i = 0; i < msgLines.length; i++){ 
                System.out.print(msgLines[i]);
                if(i != 11) System.out.print("\n");
            }

            //Updates global statistic variables
            pingResPackets++;
            return true;

        } catch (SocketTimeoutException e){
            //If the socket times out it returns false
            return false;
        }
        
    }

    public static void main(String[] args) throws IOException{

        //Throwing error if there are not 5 arguments
        if(args.length != 5){
            System.out.println("ERR - arg " + args.length);
            return;
        }

        //Getting IP, port, clientID, number of packets, and wait time from the arguments
        InetAddress IP = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        int clientID = Integer.parseInt(args[2]);
        int packets = Integer.parseInt(args[3]);
        int wait = Integer.parseInt(args[4]);

        //Throwing error if there is an invalid port number
        if(port < 0 || port > 65535){
            System.out.println("ERR - Invalid Port");
            return;
        }

        //Printing out client values 
        System.out.println("PINGClient started with server IP: " + IP.toString().substring(1) +
        ", port: " + port + ", clientID: " + clientID + ", packets: " + packets
        + ", wait: " + wait + "\n");

        //Creating socket
        DatagramSocket socket;
        socket = new DatagramSocket();

        //Creating a byte buffer
        byte buffer[] = null;

        //Number formatter used to make RTT values look better
        NumberFormat formatter = new DecimalFormat("#0.000");

        //While loop that runs for as many packets you have
        while(seqNum <= packets){

            pingReqPackets++; //Increases number of ping packets sent

            double timeStamp = ((double)System.currentTimeMillis())/1000.0; //Gets current time
            String timeStampString = formatter.format(timeStamp); //Sets current time to string a formats it

            //Builds and print our message
            String msg = buildMsg(seqNum, clientID, timeStampString, args[0]);
            System.out.println(msg + "\n");

            //sets buffer equal to msg
            buffer = msg.getBytes();

            //Creates packet
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, IP, port);

            //Sends packet to host
            if(sendPacket(socket, packet, wait)){

                //Gets and prints out RTT
                double RTT = (((double)System.currentTimeMillis())/1000.0) - timeStamp;
                String RTTS = formatter.format((((double)System.currentTimeMillis())/1000.0) - timeStamp);
                System.out.println("RTT: " + RTTS + " seconds\n");

                //Updating gloabl statistic variables
                totalRTT += RTT;
                numRTT++;
                avgRTT = totalRTT/numRTT;
                if(RTT > maxRTT) maxRTT = RTT;
                if(RTT < minRTT) minRTT = RTT;

            } else{
                System.out.println("--------------- Ping Response Packet Timed-Out ------------------\n");
            }

            seqNum++;
        }

        //Prints out statistics
        if(minRTT == Double.MAX_VALUE) minRTT = 0.000;
        String avgRTTS = formatter.format(avgRTT);
        String minRTTS = formatter.format(minRTT);
        String maxRTTS = formatter.format(maxRTT);
        String avgPayloadS = formatter.format(avgPayload);
        String lossRateS = formatter.format(((double)pingResPackets/(double)pingReqPackets));
        System.out.println("Summary: " + pingReqPackets + " :: " + pingResPackets + " :: " + lossRateS +
        " :: " + minRTTS + " :: " + maxRTTS + " :: " + avgRTTS + " :: " + avgPayloadS);

        socket.close();

    }


}