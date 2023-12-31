package classes;

import classes.nodes.Connection;
import classes.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class NeatParameters {
    public int populationSize;
    public List<Node> inputNodes;
    public List<Node> hiddenNodes;
    public  List<Node> outputNodes;
    public List<Connection> connections;
    public double percentageConn;

    //will be incremented everytime a new node is created
    public int nodeIDsCounter;
    //will be incremented everytime a new connection is created
    public int innovationIDsCounter;

    public NeatParameters(int populationSize, double percentageConn){
        this.populationSize = populationSize;
        this.percentageConn = percentageConn;
        this.inputNodes = new ArrayList<>();
        this.outputNodes = new ArrayList<>();
        this.hiddenNodes = new ArrayList<>();
        this.connections = new ArrayList<>();

        this.nodeIDsCounter = 1;
        this.innovationIDsCounter = 1;
    }

    public void reinitializeParameters(){
        this.inputNodes = new ArrayList<>();
        this.outputNodes = new ArrayList<>();
        this.hiddenNodes = new ArrayList<>();
        this.connections = new ArrayList<>();

        this.nodeIDsCounter = 1;
        this.innovationIDsCounter = 1;
    }

    @Override
    public String toString() {
        return "I have " + inputNodes.size() + " input nodes, " + hiddenNodes.size() + " hidden nodes and " + outputNodes.size() + " output nodes.";
    }
}
