package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Arrays;
import java.util.Optional;

import java.io.*;
import java.net.Socket;
// Ensure JavaFX is properly linked

/**
 * Connect4GUI class provides a graphical user interface for Connect4
 * that communicates with a server for gameplay.
 */
public class Connect4GUI extends Application {
    private static final int TILE_SIZE = 80;
    private static final int ROWS = 6;
    private static final int COLUMNS = 7;

    private Label statusLabel = new Label("Waiting for server...");
    private Circle[][] boardCells = new Circle[ROWS][COLUMNS];
    private TextField inputField = new TextField();
    private Button submitButton = new Button("Submit");
    private boolean isGameOver = false;

    private Label player1WinsLabel = new Label("Player 1 (X) Wins: 0");
    private Label player2WinsLabel = new Label("Player 2 (O)/Computer Wins: 0");
    private int player1Wins = 0;
    private int player2Wins = 0;


    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;


    /**
     * Constructor to initialize the Connect4 GUI with an existing server connection.
     *
     * @param socket The socket connected to the server.
     * @param out    The output stream for sending data to the server.
     * @param in     The input stream for receiving data from the server.
     */
    public Connect4GUI(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }


    /**
     * Initializes and displays the graphical user interface (GUI) for the Connect 4 game.
     * The GUI consists of the game board, status indicators, and controls for submitting player moves.
     * Handles server communication in a separate thread and updates the game board and status dynamically.
     *
     * @param primaryStage The main stage used to host the Connect 4 GUI.
     */
    @Override
    public void start(Stage primaryStage) {


        // UI Layout
        VBox root = new VBox(10);
        HBox statusBox = new HBox(10, statusLabel, player1WinsLabel, player2WinsLabel);

        GridPane board = createBoard();
        HBox inputBox = createUserInputBox();

        root.getChildren().addAll(statusBox, board, inputBox);

        Scene scene = new Scene(root, TILE_SIZE * COLUMNS, TILE_SIZE * ROWS + 120);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Connect 4");
        primaryStage.show();

        // Start separate thread to listen to server messages
        Thread serverListener = new Thread(this::listenToServer);
        serverListener.setDaemon(true);
        serverListener.start();


    }

    /**
     * Creates the Connect4 game board using JavaFX.
     *
     * @return A GridPane containing the game board.
     */
    private GridPane createBoard() {
        GridPane board = new GridPane();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Circle cell = new Circle(TILE_SIZE / 2 - 5);
                cell.setFill(Color.LIGHTGRAY);
                cell.setStroke(Color.BLACK);
                boardCells[row][col] = cell; // Save reference to update later
                board.add(cell, col, row);
            }
        }

        // Add click handlers for playable columns
        for (int col = 0; col < COLUMNS; col++) {
            final int colIndex = col; // Capture column index for lambda
            Rectangle columnArea = new Rectangle(TILE_SIZE, TILE_SIZE * ROWS);
            columnArea.setFill(Color.TRANSPARENT);
            columnArea.setOnMouseClicked(e -> submitMove(colIndex + 1));
            board.add(columnArea, col, 0, 1, ROWS);
        }

        return board;
    }

    /**
     * Creates a user input box with a text field for column input.
     *
     * @return An HBox containing the input field and submit button.
     */
    private HBox createUserInputBox() {
        inputField.setPromptText("Enter column (1-7)");
        inputField.setPrefWidth(100);
        submitButton.setOnAction(e -> {
            try {
                int column = Integer.parseInt(inputField.getText());
                submitMove(column);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid input! Please enter a number (1-7).");
            }
            inputField.clear();
        });

        HBox inputBox = new HBox(10, inputField, submitButton);
        inputBox.setStyle("-fx-padding: 10;");
        return inputBox;
    }

    /**
     * Submits the player's move to the server for the chosen column.
     *
     * @param column The column the player chose (1-based).
     */
    private void submitMove(int column) {
        if (isGameOver) return;

        try {
            out.writeObject(column); // Send the move to the server
        } catch (IOException e) {
            showErrorAndExit("Error sending move to server: " + e.getMessage());
        }
    }



    /**
     * Listens for messages from the server in a separate thread.
     */
    private void listenToServer() {
        try {
            System.out.println("Listening for server messages...");
            while (true) {

                    Object response = in.readObject();

                    if (response instanceof String) {
                        String message = (String) response;

                        // Check if the message is a board state
                        if (message.startsWith("[[") && message.endsWith("]]")) {
                            // Convert the String back to a char[][]
                            char[][] boardState = parseBoardState(message);

                            Platform.runLater(() -> {
                                updateBoard(boardState);
                                statusLabel.setText("Board updated."); // Optional: Debugging message.
                            });
                        } else {
                            // Handle other string messages (e.g., "Your turn", "You win", etc.)
                            Platform.runLater(() -> {
                                statusLabel.setText(message);
                                if (message.equals("New game started")) {
                                    resetGame();
                                    Platform.runLater(() -> {
                                        statusLabel.setText("Game restarted! Your turn.");
                                        inputField.setDisable(false); // Re-enable input
                                        submitButton.setDisable(false);
                                    });
                                } else if (message.contains("Invalid move")) {
                                    Platform.runLater(() -> statusLabel.setText("Invalid move! Your turn."));

                                } else if (message.contains("wins!") && !isGameOver) { // Increment only if !isGameOver
                                    isGameOver = true; // Prevent further increments

                                    Platform.runLater(() -> {
                                        if (message.contains("Player X")) {
                                            statusLabel.setText("Player X wins!");
                                            player1Wins++;
                                            player1WinsLabel.setText("Player 1 (X) Wins: " + player1Wins);
                                        } else if (message.contains("Player O")) {
                                            statusLabel.setText("Player O wins!");
                                            player2Wins++;
                                            player2WinsLabel.setText("Player 2 (O)/Computer Wins: " + player2Wins);
                                        }
                                        handleReplayPrompt(); // Prompt after incrementing
                                    });
                                } else if (message.contains("Game is a draw!") && !isGameOver) {
                                    isGameOver = true;
                                    Platform.runLater(() -> {
                                        statusLabel.setText(message);
                                        handleReplayPrompt();
                                    });
                                }
                            });
                        }
                    } else if (response instanceof char[][]) {
                        char[][] boardState = (char[][]) response;
                        Platform.runLater(() -> {
                            updateBoard(boardState);
                            statusLabel.setText("Board updated."); // Optional: Debugging message.
                        });
                    } else {
                        Platform.runLater(() -> {
                            System.out.println("Unexpected message type received!");
                            statusLabel.setText("Unexpected message type received!");
                        });
                    }

            }
        } catch (EOFException e) {
            Platform.runLater(() -> showErrorAndExit("Server disconnected. Game ended."));
        } catch (IOException | ClassNotFoundException e) {
            Platform.runLater(() -> showErrorAndExit("Connection lost: " + e.getMessage()));
        }
    }
    /**
     * Displays a prompt asking the user whether they want to replay or exit the game.
     */
    private void handleReplayPrompt() {
        Platform.runLater(() -> {
            // Show confirmation dialog for replay
            Alert replayAlert = new Alert(Alert.AlertType.CONFIRMATION);
            replayAlert.setTitle("Replay?");
            replayAlert.setHeaderText("Would you like to play again?");
            replayAlert.setContentText("Choose Yes to continue or No to exit.");

            ButtonType yesButton = new ButtonType("Yes");
            ButtonType noButton = new ButtonType("No");

            replayAlert.getButtonTypes().setAll(yesButton, noButton);

            // Handle the player's response
            Optional<ButtonType> result = replayAlert.showAndWait();
            if (result.isPresent() && result.get() == yesButton) {
                sendReplayResponseToServer("Yes");
            } else {
                sendReplayResponseToServer("No");
            }
        });
    }
    /**
     * Sends the user's replay response to the server.
     *
     * @param response The user's replay response ("Yes" or "No").
     */
    private void sendReplayResponseToServer(String response) {
        try {
            out.writeObject(response); // Send response (e.g., "Yes" for replay, "No" to exit)
            out.flush();

            if (response.equals("Yes")) {
                System.out.println("Replay acknowledged: Waiting for server to reset the game...");
            } else {
                System.out.println("Exiting the game.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the game state and UI.
     */
    private void resetGame() {
        Platform.runLater(() -> {
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    boardCells[row][col].setFill(Color.LIGHTGRAY);
                }
            }
            statusLabel.setText("Waiting for server to start the new game...");
            inputField.setDisable(true);
            submitButton.setDisable(true);
            isGameOver = false; // Allow input again
        });
    }

    /**
     * Parses a string representation of the board state into a 2D array.
     *
     * @param boardStateString The string representation of the board state.
     * @return A 2D char array of the board state.
     */
    private char[][] parseBoardState(String boardStateString) {
        // Remove the outer brackets and split into rows
        String[] rows = boardStateString.substring(2, boardStateString.length() - 2).split("\\], \\[");
        char[][] boardState = new char[ROWS][COLUMNS];

        // Iterate through rows
        for (int row = 0; row < ROWS; row++) {
            if (row >= rows.length) {
                // If fewer rows are provided, fill the remaining rows with empty cells (for safety).
                Arrays.fill(boardState[row], ' ');
                continue;
            }

            // Split each row into cells
            String[] cells = rows[row].split(", ");
            for (int col = 0; col < COLUMNS; col++) {
                if (col >= cells.length) {
                    // Missing cell data, assign default
                    boardState[row][col] = ' ';
                } else {
                    // Handle cell content safely
                    String cell = cells[col].trim();
                    if ("X".equals(cell)) {
                        boardState[row][col] = 'X';
                    } else if ("O".equals(cell)) {
                        boardState[row][col] = 'O';
                    } else {
                        boardState[row][col] = ' '; // Default to blank space
                    }
                }
            }
        }

        return boardState;
    }


    /**
     * Updates the UI board to reflect the current game state.
     *
     * @param boardState A 2D array representing the board state.
     */
    private void updateBoard(char[][] boardState) {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                char cellState = boardState[row][col];
                Circle circle = boardCells[row][col];
                if (cellState == 'X') {
                    circle.setFill(Color.RED);
                } else if (cellState == 'O') {
                    circle.setFill(Color.YELLOW);
                } else {
                    circle.setFill(Color.LIGHTGRAY);
                }
            }
        }
    }

    /**
     * Disables further user input after the game has ended.
     */
    private void disableInput() {
        inputField.setDisable(true);
        submitButton.setDisable(true);
    }

    /**
     * Displays an error message and exits the application.
     *
     * @param errorMessage The error message to display.
     */
    private void showErrorAndExit(String errorMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Fatal Error");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            Platform.exit();
        });
    }

    /**
     * Closes resources on application exit.
     */
    @Override
    public void stop() {
        try {
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
