public class Capture extends AbstractMove{
    private final int[] row; 
    private final int[] col;
    private final int[] yDir;
    private final int[] xDir;
    private int size;
    private final short[] piecesCaptured;

    public Capture(){
        this.row = new int[9];
        this.col = new int[9];
        this.yDir = new int[9];
        this.xDir = new int[9];
        this.piecesCaptured = new short[9];
        this.size = 0;
        this.turnKingMove = -1;
    }

    public Capture(Capture capture){
        this.row = new int[9];
        this.col = new int[9];
        this.yDir = new int[9];
        this.xDir = new int[9];
        this.piecesCaptured = new short[9];
        this.size = capture.getSize();
        for(int i = 0; i < this.size; i++){
            this.row[i] = capture.getRow()[i];
            this.col[i] = capture.getCol()[i];
            this.yDir[i] = capture.getYDir()[i];
            this.xDir[i] = capture.getXDir()[i];
            this.piecesCaptured[i] = capture.getPiecesCaptured()[i];
        }
        this.turnKingMove = capture.getTurnKingMove();
    }

    public void setMoveInfo(int row, int col, int yDir, int xDir){
        this.row[size] = row;
        this.col[size] = col;
        this.yDir[size] = yDir;
        this.xDir[size] = xDir;
        this.size++;
    }

    public void setPiecesCaptured(short piece, int i){
        piecesCaptured[i] = piece;
    }

    public boolean getType(){return false;}
    public int[] getRow (){return row;}
    public int[] getCol(){return col;}
    public int[] getYDir(){return yDir;}
    public int[] getXDir(){return xDir;}
    public int getSize(){return size;}
    public short[] getPiecesCaptured(){return piecesCaptured;}

    public String toString(){
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < size; i++){
            s.append("[");
            s.append(row[i]);
            s.append("][");
            s.append(col[i]);
            s.append("]x(");
            s.append(yDir[i]);
            s.append(")(");
            s.append(xDir[i]);
            s.append(") ");
        }
        return s.toString();
    }
}
