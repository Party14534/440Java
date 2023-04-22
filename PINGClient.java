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
    public static double avgPayload = 0;
    public static double totalLoad = 0;
    public static int numRTT = 0;
    public static int numPayload = 0;

    public static String buildMsg(int i, int ClientID, String timeStamp, String hostname){

        int payloadSize = (int)Math.floor(Math.random() * (300 - 200 + 1) + 200);
        String payload = "Host: " + hostname + "\n" +
        "Class-name: VCU-CMSC440-SPRING-2023\n" +
        "User-name: Dellimore, Zachariah\n" +
        "Rest: ";
        int bits = payloadSize - payload.getBytes().length;
        String rest = "";
        for(int j = 0; j < bits; j++){
            int randNum = (int)Math.floor(Math.random() * (36));
            if(randNum >= 10) randNum+=39;
            randNum+=48;
        
            rest += (char)randNum;
        }
        rest += "\n\0";


        String msg = "---------- Ping Request Packet Header ----------\n" +
        "Version: 1\n"+
        "Client ID: " + ClientID + "\n" +
        "Sequence No.: " + i + "\n" +
        "Time: " + timeStamp + "\n" +
        "Payload Size: " + payloadSize + "\n" +
        "--------- Ping Request Packet Payload ------------\n" +
        payload + rest;

        return msg;
    }
    
    public static boolean sendPacket(DatagramSocket socket, DatagramPacket packet, int wait, int loadSize) throws IOException{

        socket.send(packet);

        socket.setSoTimeout(wait*1000);


        byte[] buffer = new byte[65535];

        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        while(true){
            try{
                socket.receive(receivePacket);
                String returnData = new String(buffer);
                System.out.println(returnData);
                totalLoad += loadSize;
                numPayload++;
                avgPayload = totalLoad/numPayload;
                pingResPackets++;
                break;

            } catch (SocketTimeoutException e){
                return false;
            }
        }


        return true;
    }

    public static void main(String[] args) throws IOException{

        //Throwing error
        if(args.length != 5){
            System.out.println("ERR: " + args.length);
            return;
        }

        InetAddress IP = InetAddress.getByName(args[0]);
        IP = InetAddress.getByName(IP.getHostAddress());
        int port = Integer.parseInt(args[1]);
        int clientID = Integer.parseInt(args[2]);
        int packets = Integer.parseInt(args[3]);
        int wait = Integer.parseInt(args[4]);

        //Fill out error later
        if(port < 0 || port > 65535) return;

        //Printing out values
        System.out.println("PINGClient started with server IP: " + IP.toString().substring(1) +
        ", port: " + port + ", clientID: " + clientID + ", packets: " + packets
        + ", wait: " + wait);

        DatagramSocket socket;
        socket = new DatagramSocket();

        byte buffer[] = null;

        NumberFormat formatter = new DecimalFormat("#0.000");

        while(seqNum < packets+1){

            pingReqPackets++;

            double timeStamp = ((double)System.currentTimeMillis())/1000.0;
            String timeStampString = formatter.format(timeStamp);

            String msg = buildMsg(seqNum, clientID, timeStampString, args[0]);
            System.out.println(msg);

            buffer = msg.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, IP, port);

            if(sendPacket(socket, packet, wait, msg.getBytes().length)){
                seqNum++;
                double RTT = (((double)System.currentTimeMillis())/1000.0) - timeStamp;
                String RTTS = formatter.format((((double)System.currentTimeMillis())/1000.0) - timeStamp);
                totalRTT += RTT;
                numRTT++;
                avgRTT = totalRTT/numRTT;
                System.out.println("RTT: " + RTTS);
                if(RTT > maxRTT) maxRTT = RTT;
                if(RTT < minRTT) minRTT = RTT;
            }

        }
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