import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class PINGClient {

    public static String buildMsg(int i, int ClientID, String timeStamp, String hostname){

        String rest = Integer.toHexString((int)Math.floor(Math.random() * (6553500) + 1));

        String msg = "---------- Ping Request Packet Header ----------\n" +
        "Version: 1\n"+
        "Client ID: " + ClientID + "\n" +
        "Sequence No.: " + i + "\n" +
        "Time: " + timeStamp + "\n" +
        "Payload Size: \n" +
        "--------- Ping Request Packet Payload ------------\n" +
        "Host: " + hostname + "\n" +
        "Class-name: VCU-CMSC440-SPRING-2023\n" +
        "User-name: Dellimore, Zachariah\n" +
        "Rest: " + rest + "\n";

        int payloadSize = msg.getBytes().length + 3;

        msg = "---------- Ping Request Packet Header ----------\n" +
        "Version: 1\n"+
        "Client ID: " + ClientID + "\n" +
        "Sequence No.: " + i + "\n" +
        "Time: " + timeStamp + "\n" +
        "Payload Size: " + payloadSize + "\n" +
        "--------- Ping Request Packet Payload ------------\n" +
        "Host: " + hostname + "\n" +
        "Class-name: VCU-CMSC440-SPRING-2023\n" +
        "User-name: Dellimore, Zachariah\n" +
        "Rest: " + rest + "\n";

        return msg;
    }
    
    public static boolean sendPacket(DatagramSocket socket, DatagramPacket packet, int wait) throws IOException{

        socket.send(packet);

        socket.setSoTimeout(wait*1000);


        byte[] buffer = new byte[65535];

        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);

        while(true){
            try{
                socket.receive(receivePacket);
                String returnData = new String(buffer);
                System.out.println(returnData);
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
            System.out.println("Input only 5 arguments");
            return;
        }

        InetAddress IP = InetAddress.getByName(args[0]);
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
        double timeStamp = ((double)System.currentTimeMillis())/1000.0;
        String timeStampString = formatter.format(timeStamp);

        for(int i = 0; i < packets; i++){

            String msg = buildMsg(i, clientID, timeStampString, args[0]);
            System.out.println(msg);

            buffer = msg.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, IP, port);

            if(sendPacket(socket, packet, wait)) break;

        }    

        String RTT = formatter.format((((double)System.currentTimeMillis())/1000.0) - timeStamp);
        System.out.println("RTT: " + RTT);

        socket.close();

    }


}