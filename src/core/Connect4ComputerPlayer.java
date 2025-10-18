package core;

import java.util.Random;

/**
 * Connect4ComputerPlayer class generates moves for the computer player.
 */
public class Connect4ComputerPlayer {
    private Connect4Logic game;

    /**
     * Constructor initializes the computer player with the game logic.
     *
     * @param game The Connect4Logic instance representing the game.
     */
    public Connect4ComputerPlayer(Connect4Logic game) {
        this.game = game;
    }

    /**
     * Generates a move for the computer player.
     *
     * @return The column number (1-7) where the computer will place its piece.
     */
    public int generateMove() {
        Random random = new Random();
        int column;
        do {
            column = random.nextInt(7) + 1; // Randomly select a column (1-7)
        } while (!isValidMove(column)); // Ensure the move is valid
        return column;
    }

    /**
     * Checks if a move is valid (i.e., the column is not full).
     *
     * @param column The column to check.
     * @return True if the move is valid, false otherwise.
     */
    private boolean isValidMove(int column) {
        // Check if the column is valid without modifying the board
        if (column < 1 || column > 7) {
            return false; // Invalid column
        }

        int colIndex = column - 1;
        char[][] board = game.getBoard();
        return board[0][colIndex] == ' '; // Valid if the top cell is empty
    }
}

