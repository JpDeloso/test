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
import java.net.InetAddress;
import java.net.Socket;

class Positions implements Serializable {

}

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
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 59090);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
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
