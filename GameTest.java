package org.cis1200.checkers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private Checkers game;

    @BeforeEach
    void setUp() {
        game = new Checkers();
    }

    @Test
    void testInitialBoardSetup() {
        // Test that the board is correctly initialized
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if ((i + j) % 2 == 1) {
                    if (i < 4) {
                        assertEquals(2, game.getPiece(i, j).getColor());
                    } else if (i > 5) {
                        assertEquals(1, game.getPiece(i, j).getColor());
                    } else {
                        assertNull(game.getPiece(i, j));
                    }
                } else {
                    assertNull(game.getPiece(i, j));
                }
            }
        }
    }

    @Test
    void testSoldierMovement() {
        // Test valid and invalid soldier piece movements
        assertTrue(game.movePiece(6, 1, 5, 2));
        assertFalse(game.movePiece(6, 1, 4, 2));
        assertFalse(game.movePiece(6, 1, 7, 2));
        assertFalse(game.movePiece(3, 6, 5, 4));
    }

    @Test
    void testKingMovement() {
        // Test valid and invalid king piece movements
        game.makeKing(game.getPiece(6, 1));
        assertTrue(game.movePiece(6, 1, 4, 3));
        assertFalse(game.movePiece(4, 3, 5, 2));
        assertFalse(game.movePiece(3, 4, 4, 5));
        assertTrue(game.movePiece(3, 4, 5, 2));
    }

    @Test
    void testJumping() {
        // Test valid and invalid jumping moves
        game.movePiece(6, 1, 5, 2);
        game.movePiece(3, 6, 4, 5);
        assertFalse(game.currentPlayerCanJumpPiece());
        assertTrue(game.movePiece(5, 2, 4, 3));
        assertTrue(game.currentPlayerCanJumpPiece());
    }

    @Test
    void testWinningCondition() {
        // Test winning condition
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (game.getPiece(i, j) != null && game.getPiece(i, j).getColor() == 2) {
                    game.deletePiece(i, j);
                }
            }
        }
        assertEquals(1, game.checkWinner());
    }

    @Test
    void testJumpTwice() {
        // Test jumping twice
        game.movePiece(6, 1, 5, 2);
        game.movePiece(3, 2, 4, 1);
        game.movePiece(7, 0, 6, 1);
        game.movePiece(3, 8, 4, 9);
        game.movePiece(5, 2, 4, 3);
        assertTrue(game.currentPlayerCanJumpPiece());
        assertTrue(game.movePiece(3, 4, 5, 2));
        assertFalse(game.player1);
        assertTrue(game.currentPlayerCanJumpPiece());
    }

    @Test
    void makeKing() {
        // delete all black pieces
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (game.getPiece(i, j) != null && game.getPiece(i, j).getColor() == 2) {
                    game.deletePiece(i, j);
                }
            }
        }
        // set black piece at 1,2
        game.setPiece(2, 1, 2);
        game.setPiece(1, 2, 1);
        assertTrue(game.player1);
        assertTrue(game.movePiece(2, 1, 0, 3));
        assertTrue(game.getPiece(0, 3) instanceof KingPiece);
        assertFalse(game.player1);
        assertEquals(1, game.getPiece(0, 3).getColor());
        game.setPiece(2, 0, 9);
        assertTrue(game.movePiece(0,9,1,8));
        assertTrue(game.movePiece(0,3,2,1));
        assertTrue(game.movePiece(1,8,2,9));
        assertTrue(game.movePiece(2,1,0,3));
    }

    @Test
    void testJumpTwiceSamePiece(){
        // delete all black pieces
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (game.getPiece(i, j) != null && game.getPiece(i, j).getColor() == 2) {
                    game.deletePiece(i, j);
                }
            }
        }
        game.setPiece(2, 4, 1);
        game.setPiece(2, 2, 3);
        game.setPiece(2, 5, 4);
        game.setPiece(1, 5, 0);
        game.setPiece(2, 0, 5);
        assertTrue(game.movePiece(5,0,3,2));
        assertTrue(game.player1);
        assertFalse(game.movePiece(6,3,4,5));
        assertTrue(game.player1);
        assertTrue(game.movePiece(3,2,1,4));
        assertFalse(game.player1);
        assertTrue(game.movePiece(0,5,2,3));
    }

    @Test
    void testNextJump(){
        // delete all black pieces
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (game.getPiece(i, j) != null && game.getPiece(i, j).getColor() == 2) {
                    game.deletePiece(i, j);
                }
            }
        }
        game.setPiece(2, 3, 6);
        game.setPiece(2, 1, 4);
        game.setPiece(2, 1, 2);
        game.setPiece(1, 4, 7);
        assertTrue(game.movePiece(4,7,2,5));
        assertTrue(game.player1);
        assertTrue(game.movePiece(2,5,0,3));
        assertTrue(game.player1);
        assertTrue(game.getPiece(0,3) instanceof KingPiece);
        assertTrue(game.movePiece(0,3,2,1));
        assertFalse(game.player1);
    }

    @Test
    void testUndo(){
        // make a move red
        assertTrue(game.movePiece(6, 1, 5, 2));
        assertEquals(2, game.getMoveHistory().size());
        // undo move
        game.undo();
        assertEquals(1, game.getMoveHistory().size());
        // make a move black
        assertFalse(game.movePiece(3, 6, 4, 5));
        // test (5, 2) empty
        assertNull(game.getPiece(5, 2));
        // test (6, 1) has red piece
        assertEquals(1, game.getPiece(6, 1).getColor());
        // make a move red
        assertTrue(game.movePiece(6, 1, 5, 2));
        assertTrue(game.movePiece(3, 8, 4, 9));
        // undo move
        game.undo();
        assertEquals(1, game.getMoveHistory().size());
        // test (4, 9) empty
        assertNull(game.getPiece(4, 9));
        // test (3, 8) has black piece
        assertEquals(2, game.getPiece(3, 8).getColor());
        // test AI
        assertFalse(game.randomAI());
        assertEquals(1, game.getMoveHistory().size());
    }

    @Test
    void testSaveLoadGame(){
        // delete all black pieces
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (game.getPiece(i, j) != null && game.getPiece(i, j).getColor() == 2) {
                    game.deletePiece(i, j);
                }
            }
        }
        game.setPiece(2, 3, 4);
        assertTrue(game.movePiece(6, 1, 5, 2));
        assertTrue(game.movePiece(3, 4, 4, 3));
        assertFalse(game.movePiece(6, 9, 5, 8));
        game.saveGame();
        game.loadGame();
        assertTrue(game.player1);
        assertEquals(1, game.getPiece(5,2).getColor());
        assertTrue(game.movePiece(5,2,3,4));

    }

    @Test
    public void testNoPossibleMove(){
        // delete all black pieces
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (game.getPiece(i, j) != null && game.getPiece(i, j).getColor() == 2) {
                    game.deletePiece(i, j);
                }
            }
        }
        game.setPiece(2, 4, 1);
        assertTrue(game.movePiece(6, 9, 5, 8));
        assertTrue(game.movePiece(4, 1, 5, 0));
        assertTrue(game.movePiece(5, 8, 4, 9));
        assertFalse(game.randomAI());
        assertEquals(1, game.checkWinner());
    }
}