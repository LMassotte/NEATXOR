package classes;

import classes.nodes.Node;

import java.util.List;

public class NeatParameters {
    static int populationSize;
    static int inputNodesNumber = 3;
    static int outputNodesNumber = 1;
    static int hiddenNodesNumber = 1;
    static List<Node> inputNodes;
    static List<Node> hiddenNodes;
    static List<Node> outputNodes;
    static List<Connection> connections;
    static double pourcentageConn;

    //will be incremented everytime a new node is created
    static int nodeIDsCounter = 1;
    //will be incremented everytime a new connection is created
    static int innovationIDsCounter = 5;


}
