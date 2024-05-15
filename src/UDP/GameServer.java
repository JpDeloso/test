// src/main/java/server/GameServerUDP.java
package UDP;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private static final int PORT = 12345;
    private GameState gameState = new GameState();
    private ConcurrentHashMap<SocketAddress, Integer> clientMap = new ConcurrentHashMap<>();
    private static DatagramSocket serverSocket;


    public static void main(String[] args) throws SocketException {
        serverSocket = new DatagramSocket(PORT);
        new GameServer().start();
    }

    public void start() {
        try {
            System.out.println("UDP Server started on port " + PORT);

            byte[] receiveBuffer = new byte[1024];
            byte[] sendBuffer;

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);
                System.out.println(receivePacket.getSocketAddress());

                ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Player player = (Player) ois.readObject();

                if (clientMap.containsKey(receivePacket.getSocketAddress())) {
                    clientMap.put(receivePacket.getSocketAddress(), player.getId());
                } else {
                    clientMap.put(receivePacket.getSocketAddress(), gameState.getPlayers().size());
                }
                gameState.updatePlayer(player);

                // Broadcast updated game state to all clients
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(gameState);
                sendBuffer = baos.toByteArray();

                for (SocketAddress clientAddress : clientMap.keySet()) {
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress);
                    serverSocket.send(sendPacket);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
