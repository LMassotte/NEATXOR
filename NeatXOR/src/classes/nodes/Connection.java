package classes.nodes;

public class Connection {
    public int innovationID;
    public int inNodeID;
    public int outNodeID;
    public double weight;
    public boolean isEnabled;
    public boolean isRecurrent;

    public Connection(int innovationID, int inNodeID, int outNodeID, double weight, boolean isEnabled, boolean isRecurrent) {
        this.innovationID = innovationID;
        this.inNodeID = inNodeID;
        this.outNodeID = outNodeID;
        this.weight = weight;
        this.isEnabled = isEnabled;
        this.isRecurrent = isRecurrent;
    }

    @Override
    public String toString() {
        return "Connection " + this.innovationID + " goes from Node " + this.inNodeID + " to Node " + this.outNodeID + " and has a weight of " + this.weight;
    }
}
