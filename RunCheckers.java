package org.cis1200.checkers;

import javax.swing.*;
import java.awt.*;

public class RunCheckers implements Runnable {
    JButton saveGame;
    JButton loadGame;
    JButton resetGame;
    JButton undoMove;
    final JLabel status = new JLabel("Player 1's Turn");
    CheckersBoard board = new CheckersBoard(status);
    @Override
    public void run() {
        JFrame frame = new JFrame("Checkers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        final JPanel status_panel = new JPanel();
        frame.add(status_panel, BorderLayout.SOUTH);

        status_panel.add(status);

        JPanel panel = new JPanel();
        frame.add(panel);
        panel.setLayout(new BorderLayout());
        panel.add(board, BorderLayout.CENTER);

        JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayout(4, 1));

        saveGame = new JButton("Save Game");
        loadGame = new JButton("Load Game");
        resetGame = new JButton("Reset Game");
        undoMove = new JButton("Undo Move");

        panel2.add(saveGame);
        panel2.add(loadGame);
        panel2.add(resetGame);
        panel2.add(undoMove);

        resetGame.addActionListener(e -> {
            board.reset();
            status.setText("Player 1's Turn");
        });
        undoMove.addActionListener(e -> {
            board.undo();
            status.setText(board.getStatus());
        });
        saveGame.addActionListener(e -> {
            board.saveGame();
            status.setText("Game Saved");
        });
        loadGame.addActionListener(e -> {
            board.loadGame();
            status.setText("Game Loaded");
        });

        panel.add(panel2, BorderLayout.LINE_END);
        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
    }
}