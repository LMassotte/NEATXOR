package classes;

import classes.nodes.Node;

import java.util.ArrayList;
import java.util.List;

public class NeatParameters {
    int populationSize;
    int inputNodesNumber;
    int outputNodesNumber;
    int hiddenNodesNumber;
    List<Node> inputNodes;
    List<Node> hiddenNodes;
    List<Node> outputNodes;
    List<Connection> connections;
    double pourcentageConn;

    //will be incremented everytime a new node is created
    static int nodeIDsCounter;
    //will be incremented everytime a new connection is created
    static int innovationIDsCounter;

    public NeatParameters(int populationSize, int inputNodesNumber, int outputNodesNumber, int hiddenNodesNumber, double pourcentageConn){
        this.populationSize = populationSize;
        this.inputNodesNumber = inputNodesNumber;
        this.outputNodesNumber = outputNodesNumber;
        this.hiddenNodesNumber = hiddenNodesNumber;
        this.pourcentageConn = pourcentageConn;
        this.inputNodes = new ArrayList<>();
        this.outputNodes = new ArrayList<>();
        this.hiddenNodes = new ArrayList<>();
        this.connections = new ArrayList<>();

        nodeIDsCounter = 1;
        innovationIDsCounter = 5;
    }
}
