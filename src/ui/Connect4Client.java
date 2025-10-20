package ui;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;


/**
 * Connect4Client connects to the Connect 4 server and facilitates gameplay for the player.
 */
public class Connect4Client extends Application {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;

    /**
     * Starts the client application and connects to the Connect 4 server. Once connected, it handles
     * the player's mode selection (single-player or multiplayer) and launches the game's user interface.
     *
     * @param primaryStage The primary stage used by the JavaFX application.
     */
    @Override
    public void start(Stage primaryStage) {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter Server IP Address: ");
        try {
            // Connect to the server
            String ip = input.nextLine();
            socket = new Socket(ip, 8000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to the server!");

            // Wait for server message
            Object serverMessage = in.readObject();
            if (serverMessage instanceof String) {
                String message = (String) serverMessage;
                System.out.println("Server: " + message);

            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during client-server communication:");
            e.printStackTrace();
            System.out.print("Press any key to exit.");

            try {
                System.in.read();
            } catch (IOException ex) {

            }
            System.exit(1);
        }

        // Simple GUI layout
        Text prompt = new Text("Select mode:");
        Button singlePlayer = new Button("Single Player");
        Button multiPlayer = new Button("Multiplayer");

        // Button actions
        singlePlayer.setOnAction(e -> sendModeAndLaunchGUI(1, primaryStage));
        multiPlayer.setOnAction(e -> sendModeAndLaunchGUI(2, primaryStage));

        HBox root = new HBox(10, prompt, singlePlayer, multiPlayer);
        Scene scene = new Scene(root, 300, 100);

        primaryStage.setTitle("Connect4 Mode Selection");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void sendModeAndLaunchGUI(int mode, Stage stage) {
        try {
            out.writeObject(mode);
            out.flush();
            System.out.println("Sent mode: " + mode);

            // Launch Connect4GUI
            Platform.runLater(() -> {
                try {
                    Connect4GUI gui = new Connect4GUI(socket, out, in);
                    gui.start(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            // Close the mode selection window
            stage.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * The main method serves as the entry point for the client application. It launches the JavaFX
     * application and initializes the client.
     *
     * @param args The command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args); // Launch the JavaFX application
    }
}


