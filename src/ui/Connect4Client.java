package ui;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.application.Platform;


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
        try {
            // Connect to the server
            socket = new Socket("localhost", 8000);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to the server!");

            // Wait for server message
            Object serverMessage = in.readObject();
            if (serverMessage instanceof String) {
                String message = (String) serverMessage;
                System.out.println("Server: " + message);

                if (message.contains("Enter 1 or 2:")) {
                    // Ask user for game mode
                    Scanner scanner = new Scanner(System.in);
                    int mode;

                    /* Need to add Gui for selecting gamemode */
                    // Test

                    while (true) {
                        try {
                            System.out.print("Select mode (1 for single-player, 2 for multiplayer): ");
                            mode = Integer.parseInt(scanner.nextLine()); // Use nextLine to safely handle non-integer input

                            if (mode == 1 || mode == 2) {
                                break; // Exit the loop if input is valid
                            } else {
                                System.out.println("Invalid choice. Please enter 1 for single-player or 2 for multiplayer.");
                            }

                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input! Please enter a valid number (1 or 2).");
                        }
                    }


                    out.writeObject(mode);
                    out.flush();

                    System.out.println("Client: Sent mode selection to the server.");

                    // Launch GUI after game mode selection
                    Platform.runLater(() -> {
                        Connect4GUI gui = new Connect4GUI(socket, out, in);
                        gui.start(new Stage());
                    });
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error during client-server communication:");
            e.printStackTrace();
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


