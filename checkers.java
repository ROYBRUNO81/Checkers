package org.cis1200.checkers;

import java.util.*;
import java.io.*;

class Checkers {
    private Piece[][] board;
    int numPlayer1;
    int numPlayer2;
    boolean player1;
    private Stack<Piece[][]> moveHistory = new Stack<>();
    private Stack<String> playerHistory = new Stack<>();
    private Stack<Piece> deletedPieces = new Stack<>();
    // stores all possible jumps for each piece
    private HashMap<Piece, HashMap<Piece, List<String>>> possibleJumps = new HashMap<>();
    // stores all possible jumps for some piece after a jump
    private HashMap<Piece, HashMap<Piece, List<String>>> nextJump = new HashMap<>();

    /**
     * Constructor sets up game state.
     */
    public Checkers() {
        reset();
    }

    /**
     * Resets the game to its initial state.
     */
    public void reset() {
        board = new Piece[10][10];
        numPlayer1 = 20;
        numPlayer2 = 20;
        player1 = true;
        moveHistory = new Stack<>();
        fillBoard();
        moveHistory.push(deepCloneBoard());
        playerHistory.push(Boolean.toString(player1) + " " + numPlayer1 + " " + numPlayer2);
    }

    /**
     * Fills the board with pieces.
     */
    public void fillBoard() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if ((i + j) % 2 == 1) {
                    if (i < 4) {
                        board[i][j] = new SoldierPiece(i, j, 2);
                    } else if (i > 5) {
                        board[i][j] = new SoldierPiece(i, j, 1);
                    }
                }
            }
        }
    }

    /**
     * Displays the board.
     */
    public void displayBoard() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == null) {
                    System.out.print(" |");
                } else {
                    System.out.print(board[i][j].getColor() + "|" );
                }
            }
            System.out.println();
        }
    }

    /**
     * Returns the board piece.
     *
     * @return The board
     */
    public Piece getPiece(int row, int col) {
        return board[row][col];
    }

    /**
     * Deletes a piece from the board.
     *
     * @param row The row of the piece
     * @param col The column of the piece
     */
    public void deletePiece (int row, int col) {
        deletedPieces.add(board[row][col]);
        if (board[row][col].getColor() == 1) {
            numPlayer1--;
        } else {
            numPlayer2--;
        }
        board[row][col] = null;
    }

    public void setPiece (int color, int row, int col) {
        board[row][col] = new SoldierPiece(row, col, color);
    }

    /**
     * Undoes the last move.
     */
    public void undo() {
        if (moveHistory.size() >= 2){
            moveHistory.pop();
            playerHistory.pop();
            board = moveHistory.peek().clone();
            board = deepCloneBoard();
            String[] playerInfo = playerHistory.peek().split(" ");
            player1 = Boolean.parseBoolean(playerInfo[0]);
            numPlayer1 = Integer.parseInt(playerInfo[1]);
            numPlayer2 = Integer.parseInt(playerInfo[2]);
            if (!player1) {
                undo();
            }
            if (!possibleJumps.isEmpty()){
                possibleJumps.clear();
            }
            currentPlayerCanJumpPiece();
        }
    }

    public Piece[][] deepCloneBoard() {
        Piece[][] clonedBoard = new Piece[10][10];
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (board[row][col] != null) {
                    clonedBoard[row][col] = board[row][col].clonePiece();
                }
            }
        }
        return clonedBoard;
    }
    public Stack<Piece[][]> getMoveHistory() {
        return (Stack<Piece[][]>) moveHistory.clone();
    }

    /**
     * Makes a piece a king.
     * @param piece The piece to make a king
     */
    public void makeKing(Piece piece) {
        board[piece.getRow()][piece.getCol()] = new KingPiece(piece.getRow(), piece.getCol(), piece.getColor());
    }

    /**
     * Returns all squares that pieces that can jump can land on.
     *
     * @return A list of all possible positions
     */
    public List<String> getAllPossiblePositions(){
        List<String> positions = new ArrayList<>();
        if (!nextJump.values().isEmpty()){
            for (Map.Entry<Piece, HashMap<Piece, List<String>>> entry : nextJump.entrySet()) {
                for (Map.Entry<Piece, List<String>> innerEntry : entry.getValue().entrySet()) {
                    positions.addAll(innerEntry.getValue());
                }
            }
            return positions;
        }
        if (!possibleJumps.isEmpty()) {
            for (Map.Entry<Piece, HashMap<Piece, List<String>>> entry : possibleJumps.entrySet()) {
                for (Map.Entry<Piece, List<String>> innerEntry : entry.getValue().entrySet()) {
                    positions.addAll(innerEntry.getValue());
                }
            }
            return positions;
        }
        return new ArrayList<>();
    }

    public void clearPossibleJumps() {
        possibleJumps.clear();
    }

    /**
     * Returns all squares that a piece can move to.
     *
     * @param piece The piece to move
     * @return A list of all possible positions
     */
    public List<String> getPossiblePositions(Piece piece) {
        List<String> positions = new ArrayList<>();
        if (possibleJumps.containsKey(piece)) {
            HashMap<Piece, List<String>> jumps = possibleJumps.get(piece);
            for (Map.Entry<Piece, List<String>> entry : jumps.entrySet()) {
                positions.addAll(entry.getValue());
            }
        }
        return positions;
    }

    /**
     * Returns the piece that a piece can jump.
     *
     * @param piece The piece to move
     * @param position The position to move to
     * @return The piece that can be jumped
     */
    public Piece getPieceToJump(Piece piece, String position) {
        HashMap<Piece, List<String>> jumps = possibleJumps.get(piece);
        for (Map.Entry<Piece, List<String>> entry : jumps.entrySet()) {
            if (entry.getValue().contains(position)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Transfers a piece to a new position.
     * @param piece The piece to move
     * @param newRow The new row
     * @param newCol The new column
     */
    public void transferPiece (Piece piece, int newRow, int newCol) {
        if (piece instanceof KingPiece) {
            board[newRow][newCol] = new KingPiece(newRow, newCol, piece.getColor());
            board[piece.getRow()][piece.getCol()] = null;
        } else {
            board[newRow][newCol] = new SoldierPiece(newRow, newCol, piece.getColor());
            board[piece.getRow()][piece.getCol()] = null;
        }
    }

    /**
     * Checks if a player has won.
     *
     * @return The player that has won
     */
    public int checkWinner() {
        if (numPlayer1 == 0) {
            return 2;
        }
        if (numPlayer2 == 0) {
            return 1;
        }
        return 0;
    }

    /**
     * Moves a piece to a new position.
     *
     * @param row The row of the piece
     * @param col The column of the piece
     * @param newRow The new row
     * @param newCol The new column
     * @return True if the move is successful, false otherwise
     */
    public boolean movePiece(int row, int col, int newRow, int newCol) {
        Piece piece = getPiece(row, col);
        Piece target = getPiece(newRow, newCol);
        if (piece == null) {
            return false;
        }
        if (target != null) {
            return false;
        }
        if (piece.getColor() == 1 && !player1) {
            return false;
        }
        if (piece.getColor() == 2 && player1) {
            return false;
        }
        if (currentPlayerCanJumpPiece()) {
            String position = Integer.toString(newRow) + Integer.toString(newCol);
            if (nextJump.isEmpty()) {
                if (!possibleJumps.containsKey(piece)) {
                    return false;
                }
                if (!getPossiblePositions(piece).contains(position)) {
                    return false;
                }
            } else {
                if (!nextJump.containsKey(piece)) {
                    return false;
                }
                if (!nextJump.get(piece).containsKey(getPieceToJump(piece, position))) {
                    return false;
                }
                if (!nextJump.get(piece).get(getPieceToJump(piece, position)).contains(position)) {
                    return false;
                }
                nextJump.clear();
            }

            Piece pieceToJump = getPieceToJump(piece, position);
            deletePiece(pieceToJump.getRow(), pieceToJump.getCol());
            transferPiece(piece, newRow, newCol);
            piece = getPiece(newRow, newCol);
            clearPossibleJumps();
            // make king if piece reaches the end of the board
            if (piece.getRow() == 0 && piece.getColor() == 1) {
                makeKing(piece);
                piece = getPiece(newRow, newCol);
            } else if (piece.getRow() == 9 && piece.getColor() == 2) {
                makeKing(piece);
                piece = getPiece(newRow, newCol);
            }
            if (piece instanceof KingPiece) {
                nextJump.put(piece, canJumpPieceKing(piece));
                if (nextJump.get(piece).isEmpty()) {
                    player1 = !player1;
                    nextJump.clear();
                }
            } else {
                nextJump.put(piece, canJumpPieceSoldier(piece));
                if (nextJump.get(piece).isEmpty()) {
                    player1 = !player1;
                    nextJump.clear();
                }
            }
            moveHistory.push(deepCloneBoard());
            playerHistory.push(Boolean.toString(player1) + " " + numPlayer1 + " " + numPlayer2);
            return true;
        }
        if (!piece.canMoveTo(newRow, newCol)) {
            return false;
        }
        if (piece instanceof KingPiece) {
            board[newRow][newCol] = new KingPiece(newRow, newCol, piece.getColor());
            board[row][col] = null;
        } else {
            board[newRow][newCol] = new SoldierPiece(newRow, newCol, piece.getColor());
            board[row][col] = null;
        }
        // make king if piece reaches the end of the board
        piece = getPiece(newRow, newCol);
        if (piece.getRow() == 0 && piece.getColor() == 1) {
            makeKing(piece);
        } else if (piece.getRow() == 9 && piece.getColor() == 2) {
            makeKing(piece);
        }
        player1 = !player1;
        moveHistory.push(deepCloneBoard());
        playerHistory.push(Boolean.toString(player1) + " " + numPlayer1 + " " + numPlayer2);
        return true;
    }

    /**
     * Checks if current player can jump a piece.
     */
    public boolean currentPlayerCanJumpPiece() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Piece piece = getPiece(i, j);
                if (piece != null && piece.getColor() == (player1 ? 1 : 2)) {
                    if (piece instanceof KingPiece) {
                        HashMap<Piece, List<String>> jumps = canJumpPieceKing(piece);
                        if (!jumps.isEmpty()) {
                            possibleJumps.put(piece, jumps);
                        }
                    } else {
                        HashMap<Piece, List<String>> jumps = canJumpPieceSoldier(piece);
                        if (!jumps.isEmpty()) {
                            possibleJumps.put(piece, jumps);
                        }
                    }
                }
            }
        }
        return !possibleJumps.isEmpty();
    }

    /**
     * Returns all possible moves for a soldier piece.
     *
     * @param piece The piece to move
     * @return A list of all possible moves
     */
    public HashMap<Piece, List<String>> canJumpPieceSoldier(Piece piece) {
        HashMap<Piece, List<String>> pieces = new HashMap<>();
        int i = piece.getRow();
        int j = piece.getCol();
        if (i - 1 > 0 && j + 1 < 9) {
            Piece topRight = getPiece(i - 1, j + 1);
            if (topRight != null && topRight.getColor() == (player1 ? 2 : 1)) {
                Piece jump = getPiece(i - 2, j+ 2);
                if (jump == null) {
                    String value = Integer.toString(i - 2) + Integer.toString(j + 2);
                    ArrayList<String> positions = new ArrayList<>(List.of(value));
                    pieces.put(topRight, positions);
                }
            }
        }
        if (i - 1 > 0 && j - 1 > 0) {
            Piece topLeft = getPiece(i - 1, j - 1);
            if (topLeft != null && topLeft.getColor() == (player1 ? 2 : 1)) {
                Piece jump = getPiece(i - 2, j - 2);
                if (jump == null) {
                    String value = Integer.toString(i - 2) + Integer.toString(j - 2);
                    ArrayList<String> positions = new ArrayList<>(List.of(value));
                    pieces.put(topLeft, positions);
                }
            }
        }
        if (i + 1 < 9 && j + 1 < 9) {
            Piece bottomRight = getPiece(i + 1, j + 1);
            if (bottomRight != null && bottomRight.getColor() == (player1 ? 2 : 1)) {
                Piece jump = getPiece(i + 2, j + 2);
                if (jump == null) {
                    String value = Integer.toString(i + 2) + Integer.toString(j + 2);
                    ArrayList<String> positions = new ArrayList<>(List.of(value));
                    pieces.put(bottomRight, positions);
                }
            }
        }
        if (i + 1 < 9 && j - 1 > 0) {
            Piece bottomLeft = getPiece(i + 1, j - 1);
            if (bottomLeft != null && bottomLeft.getColor() == (player1 ? 2 : 1)) {
                Piece jump = getPiece(i + 2, j - 2);
                if (jump == null) {
                    String value = Integer.toString(i + 2) + Integer.toString(j - 2);
                    ArrayList<String> positions = new ArrayList<>(List.of(value));
                    pieces.put(bottomLeft, positions);
                }
            }
        }
        return pieces;
    }

    /**
     * Returns all possible moves for a king piece.
     *
     * @param piece The piece to move
     * @return A list of all possible moves
     */
    public HashMap<Piece, List<String>> canJumpPieceKing(Piece piece) {
        HashMap<Piece, List<String>> pieces = new HashMap<>();
        int i = piece.getRow();
        int j = piece.getCol();
        while (i + 1 < 9 && j + 1 < 9) {
            Piece bottomRight = getPiece(i + 1, j + 1);
            if (bottomRight != null && bottomRight.getColor() == (player1 ? 2 : 1)) {
                ArrayList<String> positions = new ArrayList<>();
                while (i + 2 < 10 && j + 2 < 10) {
                    if (getPiece(i + 2, j + 2) == null) {
                        String value = Integer.toString(i + 2) + Integer.toString(j + 2);
                        positions.add(value);
                    }else {
                        break;
                    }
                    j += 1;
                    i += 1;
                }
                if (!positions.isEmpty()) {
                    pieces.put(bottomRight, positions);
                }
                break;
            } else if (bottomRight == null) {
                j += 1;
                i += 1;
            } else {
                break;
            }
        }
        i = piece.getRow();
        j = piece.getCol();
        while (i + 1 < 9 && j - 1 > 0) {
            Piece bottomLeft = getPiece(i + 1, j - 1);
            if (bottomLeft != null && bottomLeft.getColor() == (player1 ? 2 : 1)) {
                ArrayList<String> positions = new ArrayList<>();
                while (i + 2 < 10 && j - 2 >= 0) {
                    if (getPiece(i + 2, j - 2) == null) {
                        String value = Integer.toString(i + 2) + Integer.toString(j - 2);
                        positions.add(value);
                    }else {
                        break;
                    }
                    j -= 1;
                    i += 1;
                }
                if (!positions.isEmpty()) {
                    pieces.put(bottomLeft, positions);
                }
                break;
            }else if (bottomLeft == null){
                j -= 1;
                i += 1;
            }else {
                break;
            }
        }
        i = piece.getRow();
        j = piece.getCol();
        while (i - 1 > 0 && j + 1 < 9) {
            Piece topRight = getPiece(i - 1, j + 1);
            if (topRight != null && topRight.getColor() == (player1 ? 2 : 1)) {
                ArrayList<String> positions = new ArrayList<>();
                while (i - 2 >= 0 && j + 2 < 10) {
                    if (getPiece(i - 2, j + 2) == null) {
                        String value = Integer.toString(i - 2) + Integer.toString(j + 2);
                        positions.add(value);
                    }else {
                        break;
                    }
                    j += 1;
                    i -= 1;
                }
                if (!positions.isEmpty()) {
                    pieces.put(topRight, positions);
                }
                break;
            }else if (topRight == null){
                j += 1;
                i -= 1;
            }else {
                break;
            }
        }
        i = piece.getRow();
        j = piece.getCol();
        while (i - 1 > 0 && j - 1 > 0) {
            Piece topLeft = getPiece(i - 1, j - 1);
            if (topLeft != null && topLeft.getColor() == (player1 ? 2 : 1)) {
                ArrayList<String> positions = new ArrayList<>();
                while (i - 2 >= 0 && j - 2 >= 0) {
                    if (getPiece(i - 2, j - 2) == null) {
                        String value = Integer.toString(i - 2) + Integer.toString(j - 2);
                        positions.add(value);
                    }else {
                        break;
                    }
                    j -= 1;
                    i -= 1;
                }
                if (!positions.isEmpty()) {
                    pieces.put(topLeft, positions);
                }
                break;
            }else if (topLeft == null){
                j -= 1;
                i -= 1;
            }else {
                break;
            }
        }
        return pieces;
    }

    /**
     * Returns all possible moves for a king piece.
     *
     * @param piece The piece to move
     * @return A list of all possible moves
     */
    private List<String> getPossibleKingMoves(Piece piece){
        List<String> moves = new ArrayList<>();
        int i = piece.getRow();
        int j = piece.getCol();
        while (i + 1 < 10 && j + 1 < 10) {
            if (getPiece(i + 1, j + 1) == null) {
                moves.add(Integer.toString(i + 1) + Integer.toString(j + 1));
            } else {
                break;
            }
            j += 1;
            i += 1;
        }
        i = piece.getRow();
        j = piece.getCol();
        while (i + 1 < 10 && j - 1 >= 0) {
            if (getPiece(i + 1, j - 1) == null) {
                moves.add(Integer.toString(i + 1) + Integer.toString(j - 1));
            } else {
                break;
            }
            j -= 1;
            i += 1;
        }
        i = piece.getRow();
        j = piece.getCol();
        while (i - 1 >= 0 && j + 1 < 10) {
            if (getPiece(i - 1, j + 1) == null) {
                moves.add(Integer.toString(i - 1) + Integer.toString(j + 1));
            } else {
                break;
            }
            j += 1;
            i -= 1;
        }
        i = piece.getRow();
        j = piece.getCol();
        while (i - 1 >= 0 && j - 1 >= 0) {
            if (getPiece(i - 1, j - 1) == null) {
                moves.add(Integer.toString(i - 1) + Integer.toString(j - 1));
            } else {
                break;
            }
            j -= 1;
            i -= 1;
        }
        return moves;
    }

    /**
     * Returns all possible moves for a soldier piece.
     *
     * @param piece The piece to move
     * @return A list of all possible moves
     */
    private List<String> getPossibleSoldierMoves(Piece piece){
        List<String> moves = new ArrayList<>();
        int i = piece.getRow();
        int j = piece.getCol();
        if (piece.getColor() == 1) {
            if (i - 1 >= 0 && j + 1 < 10) {
                if (getPiece(i - 1, j + 1) == null) {
                    moves.add(Integer.toString(i - 1) + Integer.toString(j + 1));
                }
            }
            if (i - 1 >= 0 && j - 1 >= 0) {
                if (getPiece(i - 1, j - 1) == null) {
                    moves.add(Integer.toString(i - 1) + Integer.toString(j - 1));
                }
            }
        } else {
            if (i + 1 < 10 && j + 1 < 10) {
                if (getPiece(i + 1, j + 1) == null) {
                    moves.add(Integer.toString(i + 1) + Integer.toString(j + 1));
                }
            }
            if (i + 1 < 10 && j - 1 >= 0) {
                if (getPiece(i + 1, j - 1) == null) {
                    moves.add(Integer.toString(i + 1) + Integer.toString(j - 1));
                }
            }
        }
        return moves;
    }

    /**
     * Returns all possible moves for movables piece (black).
     * @return A list of all possible moves
     */
    private HashMap<Piece, List<String>> getAIMoves() {
        // get all possible moves for the AI (Black)
        HashMap<Piece, List<String>> moves = new HashMap<Piece, List<String>>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Piece piece = getPiece(i, j);
                if (piece != null && piece.getColor() == 2) {
                    if (piece instanceof KingPiece) {
                        List<String> kingMoves = getPossibleKingMoves(piece);
                        if (!kingMoves.isEmpty()) {
                            moves.put(piece, kingMoves);
                        }
                    } else {
                        List<String> soldierMoves = getPossibleSoldierMoves(piece);
                        if (!soldierMoves.isEmpty()) {
                            moves.put(piece, soldierMoves);
                        }
                    }
                }
            }
        }
        return moves;
    }

    /**
     * Makes a random move for the AI.
     * @return True if the move is successful, false otherwise
     */
    public boolean randomAI() {
        if (!player1) {
            if (currentPlayerCanJumpPiece()){
                if (nextJump.values().isEmpty()) {
                    List<Piece> pieces = new ArrayList<>(possibleJumps.keySet());
                    if (pieces.isEmpty()) {
                        numPlayer2 = 0;
                        return false;
                    }
                    Random rand = new Random();
                    Piece piece = pieces.get(rand.nextInt(pieces.size()));
                    List<String> positions = getPossiblePositions(piece);
                    if (positions.isEmpty()) {
                        return false;
                    }
                    String position = positions.get(rand.nextInt(positions.size()));
                    int row = Integer.parseInt(position.substring(0, 1));
                    int col = Integer.parseInt(position.substring(1));
                    return movePiece(piece.getRow(), piece.getCol(), row, col);
                } else {
                    List<Piece> pieces = new ArrayList<>(nextJump.keySet());
                    if (pieces.isEmpty()) {
                        numPlayer2 = 0;
                        return false;
                    }
                    Random rand = new Random();
                    Piece piece = pieces.get(rand.nextInt(pieces.size()));
                    List<String> positions = getPossiblePositions(piece);
                    if (positions.isEmpty()) {
                        return false;
                    }
                    String position = positions.get(rand.nextInt(positions.size()));
                    int row = Integer.parseInt(position.substring(0, 1));
                    int col = Integer.parseInt(position.substring(1));
                    return movePiece(piece.getRow(), piece.getCol(), row, col);
                }
            } else {
                HashMap<Piece, List<String>> moves = getAIMoves();
                List<Piece> pieces = new ArrayList<>(moves.keySet());
                if (pieces.isEmpty()) {
                    numPlayer2 = 0;
                    return false;
                }
                Random rand = new Random();
                Piece piece = pieces.get(rand.nextInt(pieces.size()));
                List<String> positions = moves.get(piece);
                if (positions.isEmpty()) {
                    return false;
                }
                String position = positions.get(rand.nextInt(positions.size()));
                int row = Integer.parseInt(position.substring(0, 1));
                int col = Integer.parseInt(position.substring(1));
                return movePiece(piece.getRow(), piece.getCol(), row, col);
            }
        }
        return false;
    }

    public boolean saveGame() {
        try {
            // Create a new file
            File file = new File("game_state.txt");
            file.createNewFile();

            // Open a FileWriter to write to the file
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);

            // Write the board state
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (board[i][j] == null) {
                        bw.write("\n");
                    } else {
                        bw.write(i + "," + j + "," + board[i][j].getColor() + "," + (board[i][j] instanceof SoldierPiece ? 1 : 2) + "\n");
                    }
                }
            }

            // Write other game state variables
            bw.write("numPlayer1\n" + numPlayer1 + "\n");
            bw.write("numPlayer2\n" + numPlayer2 + "\n");
            bw.write("player1\n" + player1 + "\n");

            bw.write("deletedPieces\n" + deletedPieces.size() + "\n");
            for (Piece piece : deletedPieces) {
                bw.write(piece.getRow() + "," + piece.getCol() + "," + piece.getColor() + "," + (piece instanceof SoldierPiece ? 1 : 2) + "\n");
            }
            int size = 0;
            if (!possibleJumps.isEmpty()){
                for (Map.Entry<Piece, HashMap<Piece, List<String>>> entry : possibleJumps.entrySet()) {
                    for (Map.Entry<Piece, List<String>> innerEntry : entry.getValue().entrySet()) {
                        size += innerEntry.getValue().size();
                    }
                }
            }
            bw.write("possibleJumps\n" + (possibleJumps.isEmpty() ? 0 : size) + "\n");
            if (!possibleJumps.isEmpty()){
                for (Map.Entry<Piece, HashMap<Piece, List<String>>> entry : possibleJumps.entrySet()) {
                    for (Map.Entry<Piece, List<String>> innerEntry : entry.getValue().entrySet()) {
                        for (String position : innerEntry.getValue()) {
                            bw.write(entry.getKey().getRow() + "," + entry.getKey().getCol() + "," + entry.getKey().getColor() + "," + (entry.getKey() instanceof SoldierPiece ? 1 : 2) + "," + innerEntry.getKey().getRow() + "," + innerEntry.getKey().getCol() + "," + innerEntry.getKey().getColor() + "," + (innerEntry.getKey() instanceof SoldierPiece ? 1 : 2) + "," + position + "\n");
                        }
                    }
                }
            }
            size = 0;
            if (!nextJump.isEmpty()){
                for (Map.Entry<Piece, HashMap<Piece, List<String>>> entry : nextJump.entrySet()) {
                    for (Map.Entry<Piece, List<String>> innerEntry : entry.getValue().entrySet()) {
                        size += innerEntry.getValue().size();
                    }
                }
            }
            bw.write("nextJump\n" + (nextJump.isEmpty() ? 0 : size) + "\n");
            if (!nextJump.isEmpty()){
                for (Map.Entry<Piece, HashMap<Piece, List<String>>> entry : nextJump.entrySet()) {
                    for (Map.Entry<Piece, List<String>> innerEntry : entry.getValue().entrySet()) {
                        for (String position : innerEntry.getValue()) {
                            bw.write(entry.getKey().getRow() + "," + entry.getKey().getCol() + "," + entry.getKey().getColor() + "," + (entry.getKey() instanceof SoldierPiece ? 1 : 2) + "," + innerEntry.getKey().getRow() + "," + innerEntry.getKey().getCol() + "," + innerEntry.getKey().getColor() + "," + (innerEntry.getKey() instanceof SoldierPiece ? 1 : 2) + "," + position + "\n");
                        }
                    }
                }
            }

            // Close the BufferedWriter
            bw.close();
            fw.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadGame() {
        try {
            // Open a FileReader to read from the file
            File file = new File("game_state.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            // Read the board state
            board = new Piece[10][10];
            String line;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    line = br.readLine();
                    if (line.isEmpty()) {
                        board[i][j] = null;
                    } else {
                        String[] parts = line.split(",");
                        int row = Integer.parseInt(parts[0]);
                        int col = Integer.parseInt(parts[1]);
                        int color = Integer.parseInt(parts[2]);
                        int type = Integer.parseInt(parts[3]);
                        if (type == 1) {
                            board[i][j] = new SoldierPiece(row, col, color);
                        } else {
                            board[i][j] = new KingPiece(row, col, color);
                        }
                    }
                }
            }

            // Read other game state variables
            line = br.readLine();
            numPlayer1 = Integer.parseInt(br.readLine());
            line = br.readLine();
            numPlayer2 = Integer.parseInt(br.readLine());
            line = br.readLine();
            player1 = Boolean.parseBoolean(br.readLine());
            line = br.readLine();
            int deletedPiecesSize = Integer.parseInt(br.readLine());
            deletedPieces = new Stack<>();
            for (int i = 0; i < deletedPiecesSize; i++) {
                line = br.readLine();
                String[] parts = line.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                int color = Integer.parseInt(parts[2]);
                int type = Integer.parseInt(parts[3]);
                if (type == 1) {
                    deletedPieces.add(new SoldierPiece(row, col, color));
                } else {
                    deletedPieces.add(new KingPiece(row, col, color));
                }
            }
            line = br.readLine();
            possibleJumps = new HashMap<>();
            int possibleJumpsSize = Integer.parseInt(br.readLine());
            for (int i = 0; i < possibleJumpsSize; i++) {
                line = br.readLine();
                String[] parts = line.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                int color = Integer.parseInt(parts[2]);
                int type = Integer.parseInt(parts[3]);
                int row2 = Integer.parseInt(parts[4]);
                int col2 = Integer.parseInt(parts[5]);
                int color2 = Integer.parseInt(parts[6]);
                int type2 = Integer.parseInt(parts[7]);
                String position = parts[8];
                if (board[row][col] != null) {
                    if (!possibleJumps.containsKey(getPiece(row, col))) {
                        possibleJumps.put(getPiece(row, col), new HashMap<>());
                    }
                    if (board[row2][col2] != null) {
                        if (!possibleJumps.get(getPiece(row, col)).containsKey(getPiece(row2, col2))) {
                            possibleJumps.get(getPiece(row, col)).put(getPiece(row2, col2), new ArrayList<>());
                        }
                        possibleJumps.get(getPiece(row, col)).get(getPiece(row2, col2)).add(position);
                    }
                }
            }
            line = br.readLine();
            // Read nextJump data
            nextJump = new HashMap<>();
            int nextJumpSize = Integer.parseInt(br.readLine());
            for (int i = 0; i < nextJumpSize; i++) {
                line = br.readLine();
                String[] parts = line.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                int color = Integer.parseInt(parts[2]);
                int type = Integer.parseInt(parts[3]);
                int row2 = Integer.parseInt(parts[4]);
                int col2 = Integer.parseInt(parts[5]);
                int color2 = Integer.parseInt(parts[6]);
                int type2 = Integer.parseInt(parts[7]);
                String position = parts[8];
                if (board[row][col] != null) {
                    if (!nextJump.containsKey(getPiece(row, col))) {
                        nextJump.put(getPiece(row, col), new HashMap<>());
                    }
                    if (board[row2][col2] != null) {
                        if (!nextJump.get(getPiece(row, col)).containsKey(getPiece(row2, col2))) {
                            nextJump.get(getPiece(row, col)).put(getPiece(row2, col2), new ArrayList<>());
                        }
                        nextJump.get(getPiece(row, col)).get(getPiece(row2, col2)).add(position);
                    }
                }
            }
            // Close the BufferedReader
            br.close();
            fr.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

