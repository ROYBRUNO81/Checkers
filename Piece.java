package org.cis1200.checkers;

/**
 * piece class for representing game pieces
 */

public class Piece {
    private int row;
    private int col;

    private final int color;

    public Piece(int y, int x, int c) {
        this.row = y;
        this.col = x;
        this.color = c;
    }

    /**
     * Checks if piece can legally move to the target position
     *
     * @param row Target row
     * @param col Target column
     * @return True if move is valid, false otherwise
     */

    public boolean canMoveTo(int row, int col) {
        return false;
    }

    /**
     * Getter for row
     *
     * @return row
     */
    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public int getColor() {
        return this.color;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Piece piece = (Piece) obj;
        return row == piece.row && col == piece.col && color == piece.color;
    }

    public Piece clonePiece() {
        return new Piece(row, col, color);
    }

}

class SoldierPiece extends Piece {

    public SoldierPiece(int y, int x, int c) {
        super(y, x, c);
    }

    /**
     * Checks if piece can legally move to the target position
     *
     * @param row Target row
     * @param col Target column
     * @return True if move is valid, false otherwise
     */
    @Override
    public boolean canMoveTo(int row, int col) {
        if (row >= 10 || col >= 10) {
            return false;
        }
        if (getColor() == 2) { // black (Top Player)
            return (getRow() + 1 == row && getCol() + 1 == col)
                    || (getRow() + 1 == row && getCol() - 1 == col);
        } else if (getColor() == 1) { // red (Bottom Player)
            return (getRow() - 1 == row && getCol() + 1 == col)
                    || (getRow() - 1 == row && getCol() - 1 == col);
        }
        // Soldier move rules
        return false;
    }

    public SoldierPiece clonePiece() {
        return new SoldierPiece(getRow(), getCol(), getColor());
    }
}

class KingPiece extends Piece {

    public KingPiece(int y, int x, int c) {
        super(y, x, c);
    }

    /**
     * Checks if piece can legally move to the target position
     *
     * @param row Target row
     * @param col Target column
     * @return True if move is valid, false otherwise
     */
    @Override
    public boolean canMoveTo(int row, int col) {
        if (row >= 10 || col >= 10) {
            return false;
        }
        if (getColor() == 1 || getColor() == 2) { // black
            int i = getRow();
            int j = getCol();
            while (i + 1 < 10 && j + 1 < 10) {
                if (row == i + 1 && col == j + 1) {
                    return true;
                }
                j += 1;
                i += 1;
            }
            i = getRow();
            j = getCol();
            while (i + 1 < 10 && j - 1 >= 0) {
                if (row == i + 1 && col == j - 1) {
                    return true;
                }
                j -= 1;
                i += 1;
            }
            i = getRow();
            j = getCol();
            while (i - 1 >= 0 && j + 1 < 10) {
                if (row == i - 1 && col == j + 1) {
                    return true;
                }
                j += 1;
                i -= 1;
            }
            i = getRow();
            j = getCol();
            while (i - 1 >= 0 && j - 1 >= 0) {
                if (row == i - 1 && col == j - 1) {
                    return true;
                }
                j -= 1;
                i -= 1;
            }
        }
        // Soldier move rules
        return false;
    }

    public KingPiece clonePiece() {
        return new KingPiece(getRow(), getCol(), getColor());
    }

}
