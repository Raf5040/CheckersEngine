public class Move extends AbstractMove {
    private final int row; // row to move from
    private final int col; // column to move from
    private final int yDir; // y direction to move to
    private final int xDir; // x direction to move to

    // constructor
    public Move(int row, int col, int yDir, int xDir){
        this.row = row;
        this.col = col;
        this.yDir = yDir;
        this.xDir = xDir;
        this.turnKingMove = -1;
    }

    public boolean getType(){
        return true;
    }
    public int getRow(){
        return row;}
    public int getCol(){return col;
    }
    public int getYDir(){
        return yDir;
    }
    public int getXDir(){
        return xDir;
    }

    public String toString(){
        return "[" +
                row +
                "][" +
                col +
                "]-(" +
                yDir +
                ")(" +
                xDir +
                ") ";
    }
}
