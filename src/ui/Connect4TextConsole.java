package ui;

import core.Connect4Logic;
import core.Connect4ComputerPlayer;
import javafx.application.Application;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Connect4TextConsole class provides a text-based UI for the Connect 4 game.
 */
public class Connect4TextConsole {
    private Connect4Logic game;
    private Connect4ComputerPlayer computerPlayer;
    private boolean isComputer;

    /**
     * Constructor initializes the game logic.
     */
    public Connect4TextConsole() {
        game = new Connect4Logic();
        computerPlayer = new Connect4ComputerPlayer(game);
    }

    /**
     * Starts the game and handles the game loop.
     */
    public void startGame() throws InputMismatchException, IndexOutOfBoundsException {
        Scanner scanner = new Scanner(System.in);
        char choice;

        System.out.println("Begin Game. Enter 'P' to play against another player; enter 'C' to play against computer.");
        while (true) {
            try {
                choice = scanner.next().charAt(0); // Read the first character of the input
                if (choice == 'P' || choice == 'p' || choice == 'C' || choice == 'c') {
                    break; // Exit the loop if the input is valid
                } else {
                    throw new InputMismatchException();
                }
            }
            catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter 'P' or 'C'.");
            }
        }

        isComputer = (choice == 'C' || choice == 'c'); // Set the game mode
        System.out.println("Start game " + (isComputer ? "against computer." : "against another player."));

        int column;

        while (true) {
            printBoard();
            column = -1;

            // Handle current player's turn
            try {
                if (game.getCurrentPlayer() == 'X' || !isComputer) {
                    // Human player's turn
                    System.out.println("Player " + game.getCurrentPlayer() + " - your turn. Choose a column number from 1-7.");
                    if (scanner.hasNextInt()) {
                        column = scanner.nextInt();
                        if (column < 1 || column > 7) {
                            throw new IndexOutOfBoundsException();
                        }
                    } else {
                        throw new InputMismatchException();
                    }
                } else if (isComputer && game.getCurrentPlayer() == 'O') {
                    // Computer player's turn
                    column = computerPlayer.generateMove();
                    System.out.println("Computer (Player O) chooses column " + column);
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please choose a column number from 1 to 7.");
                scanner.next();
                continue;
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Invalid column number! Please choose a column number from 1 to 7.");
                continue;
            }


            // Attempt the move
            try {
                if (!game.playTurn(column)) {
                    throw new IndexOutOfBoundsException();
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("That column is full! Try a different column."); // Retry same turn
                continue;
            }

            // Check for win or draw after a successful move
            if (game.checkWin()) {
                printBoard();
                System.out.println("Player " + game.getCurrentPlayer() + " wins!");
                break; // Exit game if there's a win
            }

            if (game.isBoardFull()) {
                printBoard();
                System.out.println("The game is a draw!");
                break; // Exit game if it's a draw
            }

            // Turn proceeds to the next player automatically
        }

        scanner.close();

    }




    /**
     * Prints the current state of the game board.
     */
    private void printBoard() {
        char[][] board = game.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.out.print("|" + board[i][j]);
            }
            System.out.println("|");
        }
    }

    /**
     * Main method to start the game.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) throws InputMismatchException {
        boolean validSelection = false;
        while (!validSelection) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Would you like to play the game with GUI or Console? Enter 'G' for GUI, 'C' for Console.");
            try {
                char choice = scanner.next().charAt(0);
                if (choice == 'G' || choice == 'g') {
                    validSelection = true;
                    Application.launch(Connect4GUI.class); // Launch GUI game
                } else if (choice == 'C' || choice == 'c') {
                    validSelection = true;
                    Connect4TextConsole console = new Connect4TextConsole();
                    console.startGame(); // Launch console game
                } else {
                    throw new InputMismatchException();
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter 'G' or 'C'.");
            }
        }
    }

}