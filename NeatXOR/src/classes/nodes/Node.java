package classes.nodes;

import classes.NeatParameters;

public class Node {
    public int id;
    public int nodeType;
    public int nodeLayer;
    public double sumInput;
    public double sumOutput;

    public Node(int id, int nodeType, int nodeLayer, double sumInput, double sumOutput) {
        this.id = id;
        this.nodeType = nodeType;
        this.nodeLayer = nodeLayer;
        this.sumInput = sumInput;
        this.sumOutput = sumOutput;
    }
}
