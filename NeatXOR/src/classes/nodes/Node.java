package classes.nodes;

public class Node {
    public int nodeID;
    public int nodeType;
    public int nodeLayer;
    public double sumInput;
    public double sumOutput;

    public Node(){
        this.nodeID = -1;
        this.nodeType = -1;
        this.nodeLayer = -1;
        this.sumInput = -1;
        this.sumOutput = -1;
    }

    public Node(int nodeID, int nodeType, int nodeLayer, double sumInput, double sumOutput) {
        this.nodeID = nodeID;
        this.nodeType = nodeType;
        this.nodeLayer = nodeLayer;
        this.sumInput = sumInput;
        this.sumOutput = sumOutput;
    }
}
