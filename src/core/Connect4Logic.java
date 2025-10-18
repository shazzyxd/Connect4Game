package core;

/**
 * Connect4Logic class handles the game logic for Connect 4.
 * It manages the game board, player moves, and checks for a win or draw.
 */
public class Connect4Logic {
    private static final int ROWS = 6;
    private static final int COLUMNS = 7;
    private char[][] board;
    private char currentPlayer;
    private int lastPlacedRow;

    /**
     * Constructor initializes the game board and sets the starting player.
     */
    public Connect4Logic() {
        board = new char[ROWS][COLUMNS];
        currentPlayer = 'X'; // Player X starts first
        initializeBoard();
    }

    /**
     * Initializes the game board with empty spaces.
     */
    private void initializeBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                board[i][j] = ' ';
            }
        }
    }

    /**
     * Switches the current player between X and O.
     */
    public void switchPlayer() {
        if (currentPlayer == 'X') {
            currentPlayer = 'O';
        } else {
            currentPlayer = 'X';
        }
    }

    /**
     * Attempts to place a player's piece in the specified column.
     *
     * @param column The column where the piece is to be placed (1-7).
     * @return True if the move is valid and successful, false otherwise.
     */
    public boolean makeMove(int column) {
        if (column < 1 || column > COLUMNS) {
            return false; // Invalid column
        }

        int colIndex = column - 1;
        for (int i = ROWS - 1; i >= 0; i--) {
            if (board[i][colIndex] == ' ') {
                board[i][colIndex] = currentPlayer;
                lastPlacedRow = i; // Track the row where the piece was placed
                return true;
            }
        }
        return false; // Column is full
    }

    /**
     * Checks if the current player has won the game.
     *
     * @return True if the current player has won, false otherwise.
     */
    public boolean checkWin() {
        // Check horizontal, vertical, and diagonal lines for a win
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (board[i][j] == currentPlayer) {
                    if (checkDirection(i, j, 1, 0) || // Horizontal
                            checkDirection(i, j, 0, 1) || // Vertical
                            checkDirection(i, j, 1, 1) || // Diagonal (top-left to bottom-right)
                            checkDirection(i, j, 1, -1)) { // Diagonal (top-right to bottom-left)
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks for a win in a specific direction.
     *
     * @param row    The starting row.
     * @param col    The starting column.
     * @param rowDir The row direction (1, 0, or -1).
     * @param colDir The column direction (1, 0, or -1).
     * @return True if four consecutive pieces are found in the specified direction.
     */
    private boolean checkDirection(int row, int col, int rowDir, int colDir) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            int newRow = row + i * rowDir;
            int newCol = col + i * colDir;
            if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLUMNS &&
                    board[newRow][newCol] == currentPlayer) {
                count++;
            } else {
                break;
            }
        }
        return count == 4;
    }

    /**
     * Checks if the game board is full (i.e., a draw).
     *
     * @return True if the board is full, false otherwise.
     */
    public boolean isBoardFull() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (board[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the current player (X or O).
     *
     * @return The current player.
     */
    public char getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Returns the current state of the game board.
     *
     * @return The game board as a 2D char array.
     */
    public char[][] getBoard() {
        return board;
    }

    /**
     * Executes a player's turn by placing a piece in the specified column.
     *
     * @param column The column where the piece is to be placed (1-7).
     * @return True if the move was valid and successful, false otherwise.
     */
    public boolean playTurn(int column) {
        if (makeMove(column)) { // Check if the move can be made
            if (checkWin()) {
                return true;
            }
            if (isBoardFull()) {
                return true;
            }
            // Valid move with no win/draw, switch player
            switchPlayer();
            return true; // Return true to indicate a valid move
        } else {
            return false; // Invalid move (e.g., full column)
        }
    }

    /**
     * Returns the row where the last piece was placed.
     *
     * @return The row where the last piece was placed, or -1 if no piece was placed.
     */
    public int getLastPlacedRow() {
        return lastPlacedRow;
    }

}
