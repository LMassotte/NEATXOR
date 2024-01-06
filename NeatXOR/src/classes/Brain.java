package classes;

import classes.canvas.NeuralNetworkCanvas;
import classes.nodes.Node;

import javax.swing.*;
import java.util.ArrayList;

public class Brain {
    private NeatParameters neatParameters;

    public Brain(NeatParameters neatParameters) {
        this.neatParameters = neatParameters;
    }

    public void Initialize(){
        neatParameters.inputNodes = new ArrayList<>();
        neatParameters.outputNodes = new ArrayList<>();
        neatParameters.hiddenNodes = new ArrayList<>();
        neatParameters.connections = new ArrayList<>();

        AddNode(1, 1, 0, 0);
        AddNode(1, 1, 0, 0);
        AddNode(3, 1, 0, 0);
        AddNode(2, 3, 0, 0);
        AddNode(0, 2, 0, 0);

        AddConnection(1, 5, 3.1, true, false);
        AddConnection(2, 5, 7.9, true, false);
        AddConnection(3, 5, 1.9, true, false);
        AddConnection(5, 4, 1.9, true, false);
    }

    //always add a node through this to ensure that ids are different
    public void AddNode(int nodeType, int nodeLayer, double sumInput, double sumOutput){
        switch (nodeType){
            //input node
            case 1 :
                neatParameters.inputNodes.add(new Node(neatParameters.nodeIDsCounter, nodeType, nodeLayer,sumInput, sumOutput));
                ++neatParameters.nodeIDsCounter;
                break;

            //output node
            case 2 :
                neatParameters.outputNodes.add(new Node(neatParameters.nodeIDsCounter, nodeType, nodeLayer,sumInput, sumOutput));
                ++neatParameters.nodeIDsCounter;
                break;

            //bias node
            case 3 :
                neatParameters.inputNodes.add(new Node(neatParameters.nodeIDsCounter, nodeType, nodeLayer,sumInput, sumOutput));
                ++neatParameters.nodeIDsCounter;
                break;

            //hidden node
            case 0 :
                neatParameters.hiddenNodes.add(new Node(neatParameters.nodeIDsCounter, nodeType, nodeLayer,sumInput, sumOutput));
                ++neatParameters.nodeIDsCounter;
                break;

            default:
                break;
        }
    }

    //always add a connection through this to ensure that innovation ids are different
    public void AddConnection(int inNodeID, int outNodeID, double weight, boolean isEnabled, boolean isRecurrent){
        neatParameters.connections.add(new Connection(neatParameters.innovationIDsCounter, inNodeID, outNodeID, weight, isEnabled, isRecurrent));
        neatParameters.innovationIDsCounter += 1000;
    }

    public void DrawNetwork(){
        JFrame frame = new JFrame("Neural Network Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        NeuralNetworkCanvas canvas= new NeuralNetworkCanvas(
                neatParameters.inputNodes,
                neatParameters.hiddenNodes,
                neatParameters.outputNodes,
                neatParameters.connections
        );

        frame.getContentPane().add(canvas);
        frame.setSize(500, 500);

        frame.setVisible(true);
    }

    public void LoadInputs(){
        
    }

    public void Mutate(){

    }

    public void RunNetwork(){

    }

}
