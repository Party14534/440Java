import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class PINGServer{

    public static void buildMsg(String msg, int ClientID, int seqNum){

        int lineBreakCount = 0;
        int spaceCount = 0;
        String idStr = "";
        for(int i = 0; i < msg.length(); i++){
            if(msg.charAt(i) == '\n'){ lineBreakCount++; continue;}
            
            if(lineBreakCount == 2){
                while(spaceCount < 3){
                    if(msg.charAt(i) == ' '){spaceCount++; i++;}
                    if(msg.charAt(i) == '\n') {lineBreakCount++; break;}
                    else if(spaceCount == 2) idStr += msg.charAt(i);
                    i++;
                }
            }

        }

        System.out.println(idStr);
        ClientID = Integer.parseInt(idStr);

    }

    public static void sendResponse(DatagramSocket socket, DatagramPacket receivePacket, byte[] buffer) throws IOException{

        int clientID = 3332;
        int seqNum = 0;

        String data = new String(buffer);
        data = data.substring(0, data.indexOf('\0'));

        System.out.println(data);

        buildMsg(data, clientID, seqNum);
        System.out.println("IP:" + receivePacket.getAddress() + " :: Port:" + receivePacket.getPort() +
        " :: ClientID:" + clientID + " :: SEQ#:" + seqNum + " :: RECEIVED");

        byte[] sBuffer = null;

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
            if(loss > randNumber)sendResponse(socket, receivePacket, buffer);
            else System.out.println("Packet dropped due to loss: " + randNumber + " > " + loss);

        }

        socket.close();

    }

}