public class TTEntry {
    private int value;
    private short depth;
    private short flag;

    public TTEntry(int value, short depth){
        this.value = value;
        this.depth = depth;
    }

    public short getDepth() {
        return depth;
    }

    public int getValue() {
        return value;
    }

    public short getFlag() {
        return flag;
    }

    public void setFlag(short i) {
        flag = i;
    }

    public int hashCode(){
        return (value*100 + depth) * flag;
    }
}
