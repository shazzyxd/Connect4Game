package core;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.*;
/**
 * core.Connect4Server class manages the server side of the Connect 4 game.
 * It accepts client connections, pairs them for games, or allows a player to play against the computer.
 */
public class Connect4Server {
    private static final int PORT = 8000; // Server port
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * Entry point for the Connect 4 server application. Listens for incoming client connections
     * and starts either a single-player or multiplayer game session.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("Server is starting...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("Waiting for a player...");
                Socket player = serverSocket.accept();
                System.out.println("Player connected.");

                // Ask the player if they want single or multiplayer mode
                ObjectOutputStream out = new ObjectOutputStream(player.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(player.getInputStream());

                out.writeObject("Would you like to play against another player (2) or the computer (1)? Enter 1 or 2:");
                out.flush();
                int mode = (int) in.readObject();


                if (mode == 1) {
                    // Single-player game
                    System.out.println("Player chose to play against computer.");
                    pool.execute(new SinglePlayerSession(player, out, in));
                } else {
                    // Multiplayer game - wait for a second player
                    System.out.println("Waiting for a second player...");
                    Socket player2 = serverSocket.accept();
                    System.out.println("Second player connected.");
                    pool.execute(new MultiplayerSession(player, player2, out, in, out, in));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }
}

/**
 * Executes a multiplayer session between two players.
 */
class MultiplayerSession implements Runnable {
    private final Socket player1;
    private final Socket player2;
    private final Connect4Logic game;
    private final ObjectOutputStream out1;
    private final ObjectInputStream in1;
    private final ObjectOutputStream out2;
    private final ObjectInputStream in2;


    /**
     * Creates a new MultiplayerSession object that initializes player sockets, communication
     * streams, and the game logic.
     *
     * @param player1 The first player's socket.
     * @param player2 The second player's socket.
     * @param out1    Output stream for player 1.
     * @param in1     Input stream for player 1.
     * @param out2    Output stream for player 2.
     * @param in2     Input stream for player 2.
     */
    public MultiplayerSession(Socket player1, Socket player2, ObjectOutputStream out1, ObjectInputStream in1, ObjectOutputStream out2, ObjectInputStream in2) {
        this.player1 = player1;
        this.player2 = player2;
        this.game = new Connect4Logic();
        this.out1 = out1;
        this.in1 = in1;
        this.out2 = out2;
        this.in2 = in2;

    }

    /**
     * Executes the multiplayer game session between two connected players.
     * Handles the game flow by alternating turns between players, processing their moves,
     * updating the game board, and notifying players of game results (win, draw, or invalid moves).
     * After the game ends, prompts both players to decide whether to replay or exit.
     *
     * This method ensures continuous communication with both players using their respective
     * input and output streams while managing the Connect 4 game state.
     */
    @Override
    public void run() {
        try {
            out1.writeObject("Welcome Player 1! You are 'X'");
            out2.writeObject("Welcome Player 2! You are 'O'");

            boolean isGameOver = false;

            while (!isGameOver) {
                // Player 1 turn
                isGameOver = handleTurn(in1, out1, out2, 'X');
                if (isGameOver) break;

                // Player 2 turn
                isGameOver = handleTurn(in2, out2, out1, 'O');
            }

            askToReplay(out1, in1, out2, in2);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a single player's turn, processes their input, updates the board, and notifies the opponent.
     *
     * @param in           Input stream of the current player.
     * @param out          Output stream for the current player.
     * @param opponentOut  Output stream for the opponent.
     * @param player       The current player.
     * @return True if the game is over (win or draw), otherwise false.
     * @throws IOException            When communication with a client fails.
     * @throws ClassNotFoundException When an invalid object is received from the client.
     */
    private boolean handleTurn(ObjectInputStream in, ObjectOutputStream out, ObjectOutputStream opponentOut, char player)
            throws IOException, ClassNotFoundException {
        out.writeObject("Your turn, Player " + player + ". Enter a column (1-7):");
        out.flush();

        int column = (int) in.readObject();
        if (game.makeMove(column)) {

            String boardStateString = Arrays.deepToString(game.getBoard());
            out.writeObject(boardStateString);
            out.flush();

            opponentOut.writeObject(boardStateString);
            out.flush();


            if (game.checkWin()) {
                String message = "Player " + player + " wins!";
                out.writeObject(message);  // Send to player 1
                opponentOut.writeObject(message);  // Send to player 2
                out.flush();
                opponentOut.flush();
                return true;


            } else if (game.isBoardFull()) {
                out.writeObject("Game is a draw!");
                out.flush();
                opponentOut.writeObject("Game is a draw!");
                out.flush();
                return true;
            } else {
                game.switchPlayer();
            }
        } else {
            out.writeObject("Invalid move. Try again.");
            out.flush();
            opponentOut.writeObject("Invalid move. Try again.");
            out.flush();
        }
        return false;
    }

    /**
     * Prompts both players to decide if they wish to replay. Restarts the session if both agree.
     *
     * @param out1 Output stream for player 1.
     * @param in1  Input stream for player 1.
     * @param out2 Output stream for player 2.
     * @param in2  Input stream for player 2.
     * @throws IOException            When communication with a client fails.
     * @throws ClassNotFoundException When an invalid object is received from the client.
     */
    private void askToReplay(ObjectOutputStream out1, ObjectInputStream in1, ObjectOutputStream out2, ObjectInputStream in2)
            throws IOException, ClassNotFoundException {
        out1.writeObject("Do you want to play again? Enter 'Yes' or 'No':");
        out1.flush();

        out2.writeObject("Do you want to play again? Enter 'Yes' or 'No':");
        out2.flush();

        String response1 = (String) in1.readObject();
        String response2 = (String) in2.readObject();


        if (response1.equalsIgnoreCase("Yes") && response2.equalsIgnoreCase("Yes")) {
            out1.writeObject("New game started");
            out2.writeObject("New game started");
            new MultiplayerSession(player1, player2, out1, in1, out2, in2).run();
        } else {
            out1.writeObject("Thank you for playing!");
            out2.writeObject("Thank you for playing!");
            player1.close();
            player2.close();
        }
    }
}

/**
 * Executes a single-player session where the player competes against the computer.
 */
class SinglePlayerSession implements Runnable {
    private final Socket player;
    private final Connect4Logic game;
    private final Connect4ComputerPlayer computerPlayer;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    /**
     * Creates a new SinglePlayerSession object that initializes the player socket, communication streams,
     * game logic, and computer player.
     *
     * @param player The player's socket.
     * @param out    Output stream for the player.
     * @param in     Input stream from the player.
     */
    public SinglePlayerSession(Socket player, ObjectOutputStream out, ObjectInputStream in) {
        this.player = player;
        this.game = new Connect4Logic();
        this.computerPlayer = new Connect4ComputerPlayer(game);
        this.out = out;
        this.in = in;
    }

    /**
     * Executes the single-player game session where the player competes against the computer.
     * Manages the game flow by alternating turns between the player and the computer, processes moves,
     * updates the game board, and notifies the player of game results (win, draw, or invalid moves).
     * After the game ends, asks the player whether they wish to replay or exit.
     *
     * The method communicates with the player via input and output streams and leverages
     * computer AI for automated gameplay.
     */
    @Override
    public void run() {

        try {
            out.writeObject("Welcome! You are 'X'. The computer is 'O'.");
            out.flush(); // Ensure the welcome message is sent

            boolean isGameOver = false;

            while (!isGameOver) {
                // Player's turn
                out.writeObject("Your turn. Enter a column (1-7):");
                out.flush(); // Ensure the prompt is sent
                Object input = in.readObject();

                if (input instanceof Integer) {
                    int column = (Integer) input;
                    if (game.makeMove(column)) {

                        char[][] board = game.getBoard();
                        String boardStateString = Arrays.deepToString(game.getBoard());
                        out.writeObject(boardStateString);
                        out.flush();

                        if (game.checkWin()) {
                            out.writeObject("Player X wins!");
                            out.flush();
                            break;
                        } else if (game.isBoardFull()) {
                            out.writeObject("Game is a draw!");
                            out.flush();
                            break;
                        } else {
                            game.switchPlayer();
                        }

                        // Computer's turn
                        int computerMove = computerPlayer.generateMove();
                        game.makeMove(computerMove);

                        boardStateString = Arrays.deepToString(game.getBoard());
                        out.writeObject(boardStateString);
                        out.flush();

                        if (game.checkWin()) {
                            out.writeObject("Player O wins!");
                            out.flush();
                            break;
                        } else if (game.isBoardFull()) {
                            out.writeObject("Game is a draw!");
                            out.flush();
                            break;
                        } else {
                            game.switchPlayer();
                        }
                    } else {
                        out.writeObject("Invalid move. Try again.");
                        out.flush();
                    }
                } else {
                    out.writeObject("Invalid input. Please enter a valid column (1-7).");
                    out.flush();
                }
            }

            askToReplay(out, in);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prompts the player to decide if they wish to replay. Restarts the session if they agree.
     *
     * @param out Output stream for the player.
     * @param in  Input stream from the player.
     * @throws IOException            If communication with the client fails.
     * @throws ClassNotFoundException If an invalid object is received from the client.
     */
    private void askToReplay(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        out.writeObject("Do you want to play again? Enter 'Yes' or 'No':");
        String response = (String) in.readObject();
        if (response.equalsIgnoreCase("Yes")) {
            out.writeObject("New game started");
            out.flush();
            new SinglePlayerSession(player, out, in).run();
        } else {
            out.writeObject("Thank you for playing!");
            player.close();
        }
    }
}