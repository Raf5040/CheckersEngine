// Abstract class which serves to encompass the Move and Capture classes
public abstract class AbstractMove {

    protected int turnKingMove;

    public void setTurnKingMove(int i){
        turnKingMove = i;
    }

    public int getTurnKingMove(){
        return turnKingMove;
    }

    public void reset(){
        turnKingMove = -1;
    }

    public abstract boolean getType();
}
