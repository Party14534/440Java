import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PINGServer{

     
    public static int clientID = 0;
    public static int seqNum = 0;

    public static void getVals(String msg){

        int lineBreakCount = 0;
        int spaceCount = 0;
        String idStr = "";
        String seqStr = "";
        for(int i = 0; i < msg.length(); i++){
            if(msg.charAt(i) == '\n'){ lineBreakCount++; continue;}

            spaceCount = 0;
            
            if(lineBreakCount == 2){
                while(spaceCount < 3){
                    if(msg.charAt(i) == ' '){spaceCount++; i++;}
                    if(msg.charAt(i) == '\n') {lineBreakCount++; i++; break;}
                    else if(spaceCount == 2) idStr += msg.charAt(i);
                    i++;
                }
            }
            spaceCount = 0;
            if(lineBreakCount == 3){
                while(spaceCount < 3){
                    if(msg.charAt(i) == ' '){spaceCount++; i++;}
                    if(msg.charAt(i) == '\n') {lineBreakCount++; i++; break;}
                    else if(spaceCount == 2) seqStr += msg.charAt(i);
                    i++;
                }
            }

        }

        clientID = Integer.parseInt(idStr);
        seqNum = Integer.parseInt(seqStr);

    }

    public static void sendResponse(DatagramSocket socket, DatagramPacket receivePacket, byte[] buffer, boolean accepted) throws IOException{

        String data = new String(buffer);
        data = data.substring(0, data.indexOf('\0'));

        String error;
        if(accepted) error = "RECEIVED";
        else error = "DROPPED";

        System.out.println("DATA: " + data);

        getVals(data);
        System.out.println("IP:" + receivePacket.getAddress() + " :: Port:" + receivePacket.getPort() +
        " :: ClientID:" + clientID + " :: SEQ#:" + seqNum + " :: " + error);

        if(!accepted) return;

        byte[] sBuffer = null;

        String msgLines[] = data.split("\n",100);
        msgLines[0] = "---------- Ping Response Packet Header ----------";
        msgLines[6] = "---------- Ping Response Packet Payload ----------";
        data = "";
        for(int i = 0; i < msgLines.length; i++) data += msgLines[i] + "\n";

        data = data.toUpperCase();

        sBuffer = data.getBytes();

        DatagramPacket sendPacket = new DatagramPacket(sBuffer, sBuffer.length, receivePacket.getAddress(), receivePacket.getPort());

        socket.send(sendPacket);

    }

    public static void main(String[] args) throws IOException{

        //Throwing error
        if(args.length != 2){
            System.out.println("Input only 2 arguments");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int loss = Integer.parseInt(args[1]);

        System.out.println("PINGServer started with server IP: " + InetAddress.getLocalHost()
        + ", port: " + port);

        DatagramSocket socket;
        socket = new DatagramSocket(port);

        byte[] buffer = new byte[65535];
        DatagramPacket receivePacket = null;

        boolean rcvPackets = true;

        while(rcvPackets){

            receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket);
            int randNumber = (int)Math.floor(Math.random() * (100));
            if(loss > randNumber) sendResponse(socket, receivePacket, buffer, true);
            else sendResponse(socket, receivePacket, buffer, false);

        }

        socket.close();

    }

}