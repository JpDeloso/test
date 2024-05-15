// src/main/java/server/GameServerUDP.java
package UDP;

import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private static final int PORT = 12345;
    private GameState gameState = new GameState();
    private ConcurrentHashMap<SocketAddress, Integer> clientMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new GameServer().start();
    }

    public void start() {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("UDP Server started on port " + PORT);

            byte[] receiveBuffer = new byte[1024];
            byte[] sendBuffer;

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                Player player = (Player) ois.readObject();

                clientMap.put(receivePacket.getSocketAddress(), player.getId());
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
