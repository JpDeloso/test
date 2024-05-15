// src/main/java/client/GameClientUDP.java
package UDP;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameClient extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private int playerId;
    private double playerX = 100;
    private double playerY = 100;
    private Pane pane = new Pane();
    private Map<Integer, Rectangle> playerShapes = new ConcurrentHashMap<>();

    private DatagramSocket clientSocket;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Multiplayer Game (UDP)");

        Scene scene = new Scene(pane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        new Thread(this::connectToServer).start();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                playerY -= 10;
            } else if (event.getCode() == KeyCode.DOWN) {
                playerY += 10;
            } else if (event.getCode() == KeyCode.LEFT) {
                playerX -= 10;
            } else if (event.getCode() == KeyCode.RIGHT) {
                playerX += 10;
            }
            sendPlayerUpdate();
        });
    }

    private void connectToServer() {
        try {
            clientSocket = new DatagramSocket();

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientSocket.receive(receivePacket);

                ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                GameState gameState = (GameState) ois.readObject();

                Platform.runLater(() -> updateGameState(gameState));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendPlayerUpdate() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            Player player = new Player(playerId, playerX, playerY);
            oos.writeObject(player);
            oos.flush();

            byte[] sendBuffer = baos.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateGameState(GameState gameState) {
        gameState.getPlayers().forEach((id, player) -> {
            Rectangle rect = playerShapes.computeIfAbsent(id, k -> {
                Rectangle newRect = new Rectangle(50, 50, Color.BLUE);
                pane.getChildren().add(newRect);
                return newRect;
            });
            rect.setX(player.getX());
            rect.setY(player.getY());
        });
    }
}
