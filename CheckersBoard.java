package org.cis1200.checkers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

class CheckersBoard extends JPanel {

    private static final int SQUARE_SIZE = 50;
    private static final int BOARD_SIZE = 10;
    private static final Color ACTIVE_SQUARE_COLOR = Color.WHITE;
    private static final Color INACTIVE_SQUARE_COLOR = new Color(139, 69, 19); // Brown
    private static final Color HIGHLIGHT_COLOR = Color.YELLOW;

    private Checkers game;
    private int selectedRow = -1;
    private int selectedCol = -1;

    private JLabel status; // current status text

    public CheckersBoard(JLabel statusInit) {
        game = new Checkers();
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE, BOARD_SIZE * SQUARE_SIZE));
        addMouseListener(new AIClickHandler());
        status = statusInit;
        setLayout(new BorderLayout());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);
        highlightJumps(g);
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Color squareColor = ((row + col) % 2 == 0) ? INACTIVE_SQUARE_COLOR : ACTIVE_SQUARE_COLOR;
                g.setColor(squareColor);
                g.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    private void drawPieces(Graphics g) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = game.getPiece(row, col);
                if (piece != null) {
                    drawPiece(g, row, col, piece);
                }
            }
        }
    }

    private void drawPiece(Graphics g, int row, int col, Piece piece) {
        int x = col * SQUARE_SIZE + SQUARE_SIZE / 8;
        int y = row * SQUARE_SIZE + SQUARE_SIZE / 8;
        int diameter = SQUARE_SIZE / 2 + SQUARE_SIZE / 4;

        g.setColor(piece.getColor() == 1 ? Color.RED : Color.BLACK);
        g.fillOval(x, y, diameter, diameter);
        // If the piece is a king, draw a crown by drawing two yellow circles
        if (piece instanceof KingPiece) {
            g.setColor(Color.YELLOW);
            g.drawOval(x + 5, y + 5, diameter - 10, diameter - 10);
            g.drawOval(x + 10, y + 10, diameter - 20, diameter - 20);
        }
    }

    private void highlightJumps(Graphics g) {
        List<String> squares = game.getAllPossiblePositions();
        if (!squares.isEmpty()) {
            for (String square : squares) {
                int row = Integer.parseInt(square.substring(0, 1));
                int col = Integer.parseInt(square.substring(1, 2));
                g.setColor(HIGHLIGHT_COLOR);
                g.drawRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
                // Draw a thick border around the square
                g.drawRect(col * SQUARE_SIZE + 1, row * SQUARE_SIZE + 1, SQUARE_SIZE - 2, SQUARE_SIZE - 2);
            }
        }
    }

    /**
     * (Re-)sets the game to its initial state.
     */
    public void reset() {
        game = new Checkers();
        selectedRow = -1;
        selectedCol = -1;
        status.setText("Player 1's Turn");
        updateBoard();
    }

    public void undo() {
        game.undo();
        updateBoard();
    }

    public String getStatus() {
        if (game.player1) {
            return "Player 1's Turn";
        } else {
            return "Player 2's Turn";
        }
    }

    public void saveGame() {
        if (game.saveGame()) {
            int option = JOptionPane.showConfirmDialog(CheckersBoard.this, "Game successfully saves!\nWould you like to exit? Selecting No reloads the game!", "Game Saved", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
            } else {
                game.loadGame();
                updateBoard();
            }
        } else {
            JOptionPane.showMessageDialog(CheckersBoard.this, "Game could not be saved!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadGame() {
        if (game.loadGame()) {
            updateBoard();
        } else {
            JOptionPane.showMessageDialog(CheckersBoard.this, "Game could not be loaded!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBoard() {
        repaint();
    }

    private class ClickHandler extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            int row = e.getY() / 50;
            int col = e.getX() / 50;

            if (selectedRow == -1 && selectedCol == -1) {
                if (row < 10 && col < 10 && row >= 0 && col >= 0) {
                    if (game.getPiece(row, col) != null && game.getPiece(row, col).getColor() == (game.player1 ? 1 : 2)) {
                        selectedRow = row;
                        selectedCol = col;
                        status.setText("Selected piece at (" + row + ", " + col + ")");
                    }
                } else {
                    status.setText("Invalid selection");
                }
            } else {
                if (game.movePiece(selectedRow, selectedCol, row, col)) {
                    if (game.currentPlayerCanJumpPiece()) {
                        status.setText("Player " + (game.player1 ? 1 : 2) + " can jump");
                    } else {
                        status.setText("Player " + (game.player1 ? 1 : 2) + "'s Turn");
                    }
                    selectedRow = -1;
                    selectedCol = -1;
                } else if (game.getPiece(row, col) != null && game.getPiece(row, col).getColor() == (game.player1 ? 1 : 2)) {
                    selectedRow = row;
                    selectedCol = col;
                    status.setText("Selected piece at (" + row + ", " + col + ")");
                } else {
                    status.setText("Invalid move");
                    selectedRow = -1;
                    selectedCol = -1;
                }
            }
            updateBoard();
        }

    }

    private class AIClickHandler extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            int row = e.getY() / 50;
            int col = e.getX() / 50;

            if (game.player1) {
                if (selectedRow == -1 && selectedCol == -1) {
                    if (row < 10 && col < 10 && row >= 0 && col >= 0) {
                        if (game.getPiece(row, col) != null && game.getPiece(row, col).getColor() == 1) {
                            selectedRow = row;
                            selectedCol = col;
                            status.setText("Selected piece at (" + row + ", " + col + ")");
                        } else {
                            status.setText("Invalid selection");
                        }
                    }
                } else {
                    if (game.movePiece(selectedRow, selectedCol, row, col)) {
                        if (game.currentPlayerCanJumpPiece()) {
                            status.setText("Player " + (game.player1 ? 1 : "1's") + " can jump");
                        } else {
                            status.setText("Player " + (game.player1 ? 1 : "1's") + "'s Turn");
                        }
                        selectedRow = -1;
                        selectedCol = -1;
                    } else if (game.getPiece(row, col) != null && game.getPiece(row, col).getColor() == 1) {
                        selectedRow = row;
                        selectedCol = col;
                        status.setText("Selected piece at (" + row + ", " + col + ")");
                    } else {
                        status.setText("Invalid move");
                        selectedRow = -1;
                        selectedCol = -1;
                    }
                }
            }
            updateBoard();
            while (!game.player1 && game.checkWinner() == 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                game.randomAI();
                // 100ms delay
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                game.currentPlayerCanJumpPiece();
                updateBoard();
            }
            if (game.checkWinner() == 1) {
                int option = JOptionPane.showConfirmDialog(CheckersBoard.this, "Player 1 wins!\nWould you like to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    reset();
                } else {
                    System.exit(0);
                }
            } else if (game.checkWinner() == 2) {
                int option = JOptionPane.showConfirmDialog(CheckersBoard.this, "AI wins!\nWould you like to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    reset();
                    status.setText("Player 1's Turn");
                } else {
                    System.exit(0);
                }
            }
        }
    }
}