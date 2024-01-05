package classes;

import classes.nodes.Node;

import java.util.ArrayList;

public class Brain {
    public void Initialise(){
        NeatParameters.inputNodes = new ArrayList<>();
        NeatParameters.outputNodes = new ArrayList<>();
        NeatParameters.hiddenNodes = new ArrayList<>();
        NeatParameters.connections = new ArrayList<>();

        AddNode(1, 1, 0, 0);
        AddNode(1, 1, 0, 0);
        AddNode(3, 1, 0, 0);
        AddNode(2, 3, 0, 0);
        AddNode(0, 2, 0, 0);

        AddConnection(1, 5, 3.1, true, false);
        AddConnection(2, 5, 7.9, true, false);
        AddConnection(3, 5, 1.9, true, false);
        AddConnection(3, 4, 9.9, true, false);
    }

    //always add a node through this to ensure that ids are different
    public void AddNode(int nodeType, int nodeLayer, double sumInput, double sumOutput){
        switch (nodeType){
            //input node
            case 1 :
                NeatParameters.inputNodes.add(new Node(NeatParameters.nodeIDsCounter, nodeType, nodeLayer,sumInput, sumOutput));
                ++NeatParameters.nodeIDsCounter;
                break;

            //output node
            case 2 :
                NeatParameters.outputNodes.add(new Node(NeatParameters.nodeIDsCounter, nodeType, nodeLayer,sumInput, sumOutput));
                ++NeatParameters.nodeIDsCounter;
                break;

            //bias node
            case 3 :
                NeatParameters.inputNodes.add(new Node(NeatParameters.nodeIDsCounter, nodeType, nodeLayer,sumInput, sumOutput));
                ++NeatParameters.nodeIDsCounter;
                break;

            //hidden node
            case 0 :
                NeatParameters.hiddenNodes.add(new Node(NeatParameters.nodeIDsCounter, nodeType, nodeLayer,sumInput, sumOutput));
                ++NeatParameters.nodeIDsCounter;
                break;

            default:
                break;
        }
    }

    //always add a connection through this to ensure that innovation ids are different
    public void AddConnection(int inNodeID, int outNodeID, double weight, boolean isEnabled, boolean isRecurrent){
        NeatParameters.connections.add(new Connection(NeatParameters.innovationIDsCounter, inNodeID, outNodeID, weight, isEnabled, isRecurrent));
        NeatParameters.innovationIDsCounter += 1000;
    }

    public void DrawNetwork(){

    }

    public void LoadInputs(){

    }

    public void Mutate(){

    }

    public void RunNetwork(){

    }

}
