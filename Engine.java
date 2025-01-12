import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;

// Engine plays the move

public class Engine {
    // stores computer's evaluation of the position
    private int eval;
    // stores best move in the position
    private AbstractMove bestMove;
    // stores a hash map with a large amount of previously visited positions
    private final LinkedHashMap<BitSet, TTEntry> transpositionTable;

    // initializes engine
    public Engine() {
        eval = Integer.MIN_VALUE;
        bestMove = null;
        // transposition table hash map capped at 5 million entries
        transpositionTable = new LinkedHashMap(){
            protected boolean removeEldestEntry(Map.Entry eldest){
                return size() > 5000000;
            }
        };
    }

    // method determines best move
    // for given color at given depth at given position
    public void nextBestMove(ProtoBoard protoBoard, int color, int depth) {
        // generates all legal moves
        ArrayList<AbstractMove> childMoves = protoBoard.generateLegalMoves(color);

        // executes move if there is only one legal move
        if(childMoves.size() == 1){
            bestMove = childMoves.get(0);
        }
        // goes through each move to decide the best
        else {
            eval = Integer.MIN_VALUE;
            for (AbstractMove child : childMoves) {
                protoBoard.makeMove(child);
                int currentValue = -negamax(protoBoard, depth - 1, -Integer.MAX_VALUE, Integer.MAX_VALUE, -color);
                if (currentValue > eval) {
                    eval = currentValue;
                    bestMove = child;
                }
                // if some moves are equally good, decide which to play by chance
                else if (currentValue == eval){
                    if((int) (Math.random()*3) > 1){
                        bestMove = child;
                    }
                }
                protoBoard.unmakeMove(child);
            }
        }
    }

    // recursive method implements negamax with alpha beta pruning and
    // a transposition table
    private int negamax(ProtoBoard protoBoard, int n, int a, int b, int color){
        int alphaOrig = a;
        // get the hash key of current position
        BitSet key = protoBoard.getZobristHash();
        TTEntry ttEntry = transpositionTable.get(key);
        // check if position is in transposition table, and return
        if((ttEntry != null && ttEntry.getDepth() >= n)
                && (ttEntry.getDepth())%2 == n%2){
            if(ttEntry.getFlag() == 0)
                return ttEntry.getValue();
            else if(ttEntry.getFlag() == -1)
                a = Math.max(a, ttEntry.getValue());
            else if(ttEntry.getFlag() == 1)
                b = Math.min(b, ttEntry.getValue());
            if (a >= b)
                return ttEntry.getValue();
        }

        // generate legal moves
        ArrayList<AbstractMove> legalMoves = protoBoard.generateLegalMoves(color);

        // if recursion has finished, evaluate position and return
        if (n == 0) {
            return color * ((int) evaluatePosition(protoBoard));
        }
        // if there are no legal moves return an evaluation proportional to
        // how far away that situation is
        else if (legalMoves.isEmpty()){
            return n * -1000000;
        }

        // set value to baseline 0
        int value = Integer.MIN_VALUE;

        // goes through each legal move and recurs
        for (AbstractMove child : legalMoves) {
            protoBoard.makeMove(child);
            value = Math.max(value, -negamax(protoBoard, n - 1, -b, -a, -color));
            a = Math.max(a, value);
            protoBoard.unmakeMove(child);
            if (a >= b) {
                break;
            }
        }

        // stores current position in transposition table
        key = (BitSet) key.clone();
        ttEntry = new TTEntry(value, (short) n);
        if(value <= alphaOrig)
            ttEntry.setFlag((short) 1);
        else if(value >= b){
            ttEntry.setFlag((short) -1);
        }
        else
            ttEntry.setFlag((short) 0);
        transpositionTable.put(key, ttEntry);

        return value;
    }

    // evaluates position based solely on material
    private double evaluatePosition(ProtoBoard protoBoard) {
        int value = 0;
        // positive if black is winning. negative if red is winning.
        for (int i = 0; i < Board.rows; i++) {
            for (int j = 1 - (i % 2); j < Board.cols; j += 2) {
                double pieceValue = protoBoard.getSquare(i, j);

                // checks piece exists
                if(pieceValue != 0) {
                    // checks piece is regular piece
                    if (Math.abs(pieceValue) == 2) {
                        // checks piece is in the center and adjusts value
                        if ((i == 3 && j == 2) || (i == 4 && j == 5)) {
                            pieceValue *= 1.2;
                        } else if ((i == 3 && j == 4) || (i == 4 && j == 3)) {
                            pieceValue *= 1.1;
                        }

                    }
                }
                value += (int) pieceValue;
            }
        }
        return value;
    }

    public int getEval() {
        return eval;
    }

    public AbstractMove getBestMove() {
        return bestMove;
    }
}
