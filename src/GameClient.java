// GameClient.java
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

public class GameClient extends Application {
    private Circle circle;
    private PrintWriter out;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = new Pane();
        circle = new Circle(200, 200, 20, Color.BLUE);
        root.getChildren().add(circle);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Multiplayer Game");
        primaryStage.show();

        scene.setOnKeyPressed(this::handleKeyPress);

        new Thread(this::connectToServer).start();
    }

    private void handleKeyPress(KeyEvent event) {
        double x = circle.getCenterX();
        double y = circle.getCenterY();

        if (event.getCode() == KeyCode.UP) {
            y -= 10;
        } else if (event.getCode() == KeyCode.DOWN) {
            y += 10;
        } else if (event.getCode() == KeyCode.LEFT) {
            x -= 10;
        } else if (event.getCode() == KeyCode.RIGHT) {
            x += 10;
        }

        circle.setCenterX(x);
        circle.setCenterY(y);

        if (out != null) {
            out.println(x + "," + y);
        }
    }

    private void connectToServer() {
        try {
            InetAddress localAddress = null;

            // Enumerate all network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Ignore loopback and non-up interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                // Iterate through the associated IP addresses
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Check if the address is IPv4 and not a loopback address
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        localAddress = addr;
                        break;
                    }
                }
                // Break out of the loop if a suitable address is found
                if (localAddress != null)
                    break;
            }

            if (localAddress == null) {
                System.out.println("Unable to detect local IP address.");
                return;
            }

            String serverAddress = localAddress.getHostAddress();
            int serverPort = 59090;

            Socket socket = new Socket(serverAddress, serverPort);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                String[] parts = message.split(",");
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);

                Platform.runLater(() -> {
                    circle.setCenterX(x);
                    circle.setCenterY(y);
                });
            }
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e);
        }
    }
}
