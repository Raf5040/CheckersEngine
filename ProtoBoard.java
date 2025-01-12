import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class ProtoBoard {
    private final short[][] board;
    private final BitSet[][] zobristBoard;
    private final BitSet zobristHash;
    private final ArrayList<BitSet> zobristHashHistory;
    private final static int BIT_SET_SIZE = 128;

    public ProtoBoard(){
        // initialize instance variables
        board = new short[8][8];
        zobristBoard = new BitSet[32][4];
        zobristHashHistory = new ArrayList<>();
        zobristHash = new BitSet(BIT_SET_SIZE);
        // randomize zobrist board
        for(int i = 0; i < zobristBoard.length; i++){
            for(int j = 0; j < zobristBoard[i].length; j++){
                BitSet bitSet = new BitSet(BIT_SET_SIZE);
                for(int k = 0; k < bitSet.size(); k++){
                    boolean b = Math.random() < 0.5;
                    bitSet.set(k, b);
                }
                zobristBoard[i][j] = bitSet;
            }
        }
        // set up proto board and zobrist hash
        for (int i = 0; i < board.length; i++) {
            for (int j = 1 - (i % 2); j < board[i].length; j += 2) {
                if (i <= 2) {
                    //black is true/positive
                    board[i][j] = 2;
                    zobristHash.xor(zobristBoard[(i * 4) + j/2][0]);
                } else if (i >= 5) {
                    //red is false/negative
                    board[i][j] = -2;
                    zobristHash.xor(zobristBoard[(i * 4) + j/2][1]);
                }
            }
        }
    }

    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    public BitSet getZobristHash(){
        return zobristHash;
    }

    public short getSquare(int row, int col){
        return board[row][col];
    }

    public void setSquare(int row, int col, short piece){
        board[row][col] = piece;
    }

    public void makeKing(int row, int col) {
        if (Math.abs(board[row][col]) == 2) {
            board[row][col] = (short) (board[row][col] * 2.5);
        }
    }

    public void makeNormal(int row, int col) {
        if (Math.abs(board[row][col]) == 5) {
            board[row][col] = (short) (board[row][col] / 2.5);
        }
    }

    public static boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public short convertPieceValue(short piece){
        if(piece == 2)
            return 0;
        else if(piece == 5)
            return 2;
        else if(piece == -2)
            return 1;
        else if(piece == -5)
            return 3;
        else return -1;
    }

    // General instructions for piece movement
    // yDir == +1 for down. yDir == -1 for up
    // xDir == +1 for right. xDir == -1 for left

    public short movePiece(int row, int col, int yDir, int xDir) {
        short piece = board[row][col];
        // update board array
        board[row + yDir][col + xDir] = piece;
        board[row][col] = 0;
        return piece;
    }

    public boolean canMovePiece(int row, int col, int yDir, int xDir) {
        short piece  = board[row][col];
        if(Math.abs(piece) == 2 && piece * yDir < 0) {
            return false;
        }
        return (inBounds(row + yDir, col + xDir) && board[row + yDir][col + xDir] == 0);
    }

    public short capturePiece(int row, int col, int yDir, int xDir) {
        short piece = board[row][col];
        //update board array
        short capturedPiece = board[row + yDir][col + xDir];
        board[row + yDir][col + xDir] = 0;
        board[row + 2 * yDir][col + 2 * xDir] = piece;
        board[row][col] = 0;
        return capturedPiece;
    }

    public boolean canCapturePiece(int row, int col, int yDir, int xDir) {
        int piece = board[row][col];
        if(Math.abs(piece) == 2 && piece * yDir < 0) {
            return false;
        }
        if (inBounds(row + 2*yDir, col + 2*xDir)) {
            return (board[row + yDir][col + xDir] != 0 && board[row + yDir][col + xDir] * piece < 0)
                    && (board[row + 2 * yDir][col + 2 * xDir] == 0);
        } else {
            return false;
        }
    }

    public void makeMove(AbstractMove abstractMove){
        BitSet newZobristHash = new BitSet(BIT_SET_SIZE);
        if(abstractMove.getType()){
            Move move = (Move) abstractMove;
            int row = move.getRow();
            int col = move.getCol();
            int yDir = move.getYDir();
            int xDir = move.getXDir();
            // make move and some prehashwork
            short pieceIndex = convertPieceValue(movePiece(row, col, yDir, xDir));
            newZobristHash.xor(zobristBoard[row*4 + col/2][pieceIndex]);
            // turn piece to king if necessary
            if((move.getTurnKingMove() == -1 && Math.abs(board[row+yDir][col+xDir]) == 2)
                    && (row + yDir == 7 || row + yDir == 0)){
                move.setTurnKingMove(0);
                makeKing(row+yDir, col+xDir);
                pieceIndex+=2;
            }
            // update hash
            newZobristHash.xor(zobristBoard[(row+yDir)*4 + (col+xDir)/2][pieceIndex]);
        }
        else{
            Capture capture = (Capture) abstractMove;
            for (int i = 0; i < capture.getSize(); i++) {
                int row = capture.getRow()[i];
                int col = capture.getCol()[i];
                int yDir = capture.getYDir()[i];
                int xDir = capture.getXDir()[i];
                // some prehashwork
                short pieceIndex = convertPieceValue(board[row][col]);
                if(i == 0) {
                    //BitSet newZobristHash = new BitSet(128);
                    newZobristHash.xor(zobristBoard[row * 4 + col / 2][pieceIndex]);
                }
                // do capture
                short capturedPiece = capturePiece(row, col, yDir, xDir);
                capture.setPiecesCaptured(capturedPiece, i);
                capturedPiece = convertPieceValue(capturedPiece);
                // turn piece to king if necessary
                if((capture.getTurnKingMove() == -1 && Math.abs(board[row+2*yDir][col+2*xDir]) == 2)
                        && (row + 2*yDir == 7 || row + 2*yDir == 0)){
                    capture.setTurnKingMove(i);
                    makeKing(row+2*yDir, col+2*xDir);
                    pieceIndex+=2;
                }
                // update hash
                if(i == capture.getSize() - 1) {
                    newZobristHash.xor(zobristBoard[(row + 2 * yDir) * 4 + (col + 2 * xDir) / 2][pieceIndex]);
                }
                newZobristHash.xor(zobristBoard[(row+yDir)*4 + (col+xDir)/2][capturedPiece]);
            }
        }
        // update hash
        zobristHash.xor(newZobristHash);
        zobristHashHistory.add(newZobristHash);
    }

    public void unmakeMove(AbstractMove abstractMove){
        if(abstractMove.getType()){
            Move move = (Move) abstractMove;
            int row = move.getRow();
            int col = move.getCol();
            int yDir = move.getYDir();
            int xDir = move.getXDir();
            movePiece(row + yDir, col + xDir, -yDir, -xDir);
            if(move.getTurnKingMove() == 0){
                makeNormal(row, col);
            }
            move.reset();
        }
        else{
            Capture capture = (Capture) abstractMove;
            for (int i = capture.getSize() - 1; i >= 0; i--) {
                int row = capture.getRow()[i];
                int col = capture.getCol()[i];
                int yDir = capture.getYDir()[i];
                int xDir = capture.getXDir()[i];
                convertPieceValue(movePiece(row + 2*yDir, col + 2*xDir, -2*yDir, -2*xDir));
                short capturedPiece = capture.getPiecesCaptured()[i];
                setSquare(row + yDir, col + xDir, capturedPiece);
                if(capture.getTurnKingMove() == i){
                    makeNormal(row, col);
                }
            }
            capture.reset();
        }
        // update hash
        zobristHash.xor(zobristHashHistory.get(zobristHashHistory.size() - 1));
        zobristHashHistory.remove(zobristHashHistory.size() - 1);
    }

    // method makes only last capture in Capture Object
    public void makeLastCapture(Capture capture){
        int i = capture.getSize() - 1;
        int row = capture.getRow()[i];
        int col = capture.getCol()[i];
        int yDir = capture.getYDir()[i];
        int xDir = capture.getXDir()[i];
        // do capture
        short capturedPiece = capturePiece(row, col, yDir, xDir);
        capture.setPiecesCaptured(capturedPiece, i);
        // turn piece to king if necessary
        if((capture.getTurnKingMove() == -1 && Math.abs(board[row+2*yDir][col+2*xDir]) == 2)
                && (row + 2*yDir == 7 || row + 2*yDir == 0)){
            capture.setTurnKingMove(i);
            makeKing(row+2*yDir, col+2*xDir);
        }
    }

    // method unmakes only last capture in Capture Object
    public void unmakeLastCapture(Capture capture) {
        int i = capture.getSize() - 1;
        int r = capture.getRow()[i] + 2*capture.getYDir()[i];
        int c = capture.getCol()[i] + 2*capture.getXDir()[i];
        movePiece(r, c, -2*capture.getYDir()[i], -2*capture.getXDir()[i]);
        setSquare(capture.getRow()[i] + capture.getYDir()[i],
                capture.getCol()[i] + capture.getXDir()[i], capture.getPiecesCaptured()[i]);
        if(capture.getTurnKingMove() == i){
            makeNormal(capture.getRow()[i], capture.getCol()[i]);
        }
        if(i == 0){
            capture.reset();
        }
    }

    //makes array of all legal moves for all pieces of color
    public ArrayList<AbstractMove> generateLegalMoves(int color) {
        ArrayList<AbstractMove> legalMoves = new ArrayList<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 1 - (i % 2); j < board[i].length; j += 2) {
                if (board[i][j] * color > 0 ) {
                    if (canCapturePiece(i, j, 1, 1)){
                        Capture capture = new Capture();
                        captureHelper(legalMoves, capture, i, j, 1, 1);
                    }
                    if (canCapturePiece(i, j, -1, 1)){
                        Capture capture = new Capture();
                        captureHelper(legalMoves, capture, i, j, -1, 1);
                    }
                    if (canCapturePiece(i, j, 1, -1)){
                        Capture capture = new Capture();
                        captureHelper(legalMoves, capture, i, j, 1, -1);
                    }
                    if (canCapturePiece(i, j, -1, -1)){
                        Capture capture = new Capture();
                        captureHelper(legalMoves, capture, i, j, -1, -1);
                    }
                }
            }
        }

        if (legalMoves.isEmpty()) {
            for (int i = 0; i < board.length; i++) {
                for (int j = 1 - (i % 2); j < board[i].length; j += 2) {
                    if (board[i][j] * color > 0) {
                        if (canMovePiece(i, j, 1, 1)) {
                            legalMoves.add(new Move(i, j, 1, 1));
                        }
                        if (canMovePiece(i, j, 1, -1)) {
                            legalMoves.add(new Move(i, j, 1, -1));
                        }
                        if (canMovePiece(i, j, -1, 1)) {
                            legalMoves.add(new Move(i, j, -1, 1));
                        }
                        if (canMovePiece(i, j, -1, -1)) {
                            legalMoves.add(new Move(i, j, -1, -1));
                        }
                    }
                }
            }
        }
        return legalMoves;
    }

    //helper method for generateLegalMoves
    //assumes capture can happen
    private void captureHelper(ArrayList<AbstractMove> legalMoves, Capture capture, int i, int j, int yDir, int xDir) {
        capture.setMoveInfo(i, j, yDir, xDir);
        boolean b = true;

        makeLastCapture(capture);
        if (canCapturePiece(i + 2 * yDir, j + 2 * xDir, 1, 1)) {
            Capture captureCopy = new Capture(capture);
            b = false;
            captureHelper(legalMoves, captureCopy, i + 2 * yDir, j + 2 * xDir, 1, 1);
        }
        if (canCapturePiece(i + 2 * yDir, j + 2 * xDir, -1, 1)) {
            Capture captureCopy = new Capture(capture);
            b = false;
            captureHelper(legalMoves, captureCopy, i + 2 * yDir, j + 2 * xDir, -1, 1);
        }
        if (canCapturePiece(i + 2 * yDir, j + 2 * xDir, 1, -1)) {
            Capture captureCopy = new Capture(capture);
            b = false;
            captureHelper(legalMoves, captureCopy, i + 2 * yDir, j + 2 * xDir, 1, -1);
        }
        if (canCapturePiece(i + 2 * yDir, j + 2 * xDir, -1, -1)) {
            Capture captureCopy = new Capture(capture);
            b = false;
            captureHelper(legalMoves, captureCopy, i + 2 * yDir, j + 2 * xDir, -1, -1);
        }
        unmakeLastCapture(capture);

        if (b) {
            capture.reset(); //makes sure capture being added is a clean slate
            legalMoves.add(capture);
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            s.append("[");
            for (int j = 0; j < board[i].length; j++) {
                short piece = board[i][j];
                if (piece >= 0) {
                    s.append(" ");
                }
                s.append(piece);
                if (j != board[i].length - 1)
                    s.append(",");
            }
            s.append("]");
            if (i != board.length - 1)
                s.append("\n");
        }
        return s.toString();
    }
    }
