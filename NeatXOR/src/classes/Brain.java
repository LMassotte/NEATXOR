package classes;

import classes.canvas.NeuralNetworkCanvas;
import classes.nodes.Connection;
import classes.nodes.Node;

import javax.swing.*;
import java.util.Random;

import static java.lang.Math.exp;

public class Brain {
    public int brainID;
    public NeatParameters neatParameters;
    public int outputNodeID;
    public double fitness;
    public double adjustedFitness;
    public int speciesID;

    public Brain(NeatParameters neatParameters, int id) {

        this.neatParameters = neatParameters;
        this.brainID = id;
    }

    public void initialize(int generationNumber) {
        this.fitness = 0;
        this.adjustedFitness = 0;
        this.speciesID = -1;
        neatParameters.reinitializeParameters();

        if(generationNumber == 1){
            addNode(1, 1, 0, 0);
            addNode(1, 1, 0, 0);
            addNode(3, 1, 0, 0);
            addNode(2, 3, 0, 0);

            outputNodeID = neatParameters.outputNodes.get(neatParameters.outputNodes.size() - 1).id;

            addConnection(1, outputNodeID, true, false, 1);
            addConnection(2, outputNodeID, true, false, 1);
            addConnection(3, outputNodeID, true, false, 1);
        }
        else{
            //TODO : How to initialize a member of the population of the generation if it's not gen1
        }
    }

    //always add a node through this to ensure that ids are different
    public void addNode(int nodeType, int nodeLayer, double sumInput, double sumOutput) {
        switch (nodeType) {
            //input node
            case 1:
                neatParameters.inputNodes.add(new Node(neatParameters.nodeIDsCounter, nodeType, nodeLayer, sumInput, sumOutput));
                ++neatParameters.nodeIDsCounter;
                break;

            //output node
            case 2:
                neatParameters.outputNodes.add(new Node(neatParameters.nodeIDsCounter, nodeType, nodeLayer, sumInput, sumOutput));
                ++neatParameters.nodeIDsCounter;
                break;

            //bias node
            case 3:
                neatParameters.inputNodes.add(new Node(neatParameters.nodeIDsCounter, nodeType, nodeLayer, sumInput, sumOutput));
                ++neatParameters.nodeIDsCounter;
                break;

            //hidden node
            case 0:
                neatParameters.hiddenNodes.add(new Node(neatParameters.nodeIDsCounter, nodeType, nodeLayer, sumInput, sumOutput));
                ++neatParameters.nodeIDsCounter;
                break;

            default:
                break;
        }
    }

    //always add a connection through this to ensure that innovation ids are different
    public void addConnection(int inNodeID, int outNodeID, boolean isEnabled, boolean isRecurrent, int generationNumber) {
        double weight = 0;
        if(generationNumber == 1){
            Random random = new Random();
            weight = random.nextDouble() * 2 - 1;
        }
        neatParameters.connections.add(new Connection(neatParameters.innovationIDsCounter, inNodeID, outNodeID, weight, isEnabled, isRecurrent));
        neatParameters.innovationIDsCounter += 1000;
    }

    public void drawNetwork() {
        JFrame frame = new JFrame("Neural Network Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        NeuralNetworkCanvas canvas = new NeuralNetworkCanvas(
                neatParameters.inputNodes,
                neatParameters.hiddenNodes,
                neatParameters.outputNodes,
                neatParameters.connections
        );

        frame.getContentPane().add(canvas);
        frame.setSize(500, 500);

        frame.setVisible(true);
    }

    public void loadInputs(double[] inputValuesList) {
        int max = Math.max(inputValuesList.length, neatParameters.inputNodesNumber);
        for (int i = 0; i < max; i++) {
            neatParameters.inputNodes.get(i).sumInput = inputValuesList[i];
            //input nodes => sum input = sum output
            neatParameters.inputNodes.get(i).sumOutput = inputValuesList[i];
        }
    }

    public void mutate() {

    }

    public void runNetwork() {
        //Starts at layer 2, scan through the node arrays looking for layer 2 nodes
        // when found : set input to 0, then scan the connections looking for connections where outNode = this node
        // take the inNode of this connection and get its sumOutput, multiplies it by the connection weight
        // adds this value to sumInput, and do the same for each connection terminating at this node.
        // when sumInput is completed, apply activation function to it to get outputValue
        // activation function(x) = 1 / (1 + exp(-x))
        // once, done move to the next layer, till reaching the end
        int outputLayer = neatParameters.outputNodes.stream().findFirst().get().nodeLayer;

        for (int layerCount = 2; layerCount <= outputLayer; layerCount++) {
            //hidden nodes
            if (layerCount != outputLayer) {
                for (Node hiddenNode : neatParameters.hiddenNodes) {
                    if (hiddenNode.nodeLayer == layerCount) {
                        hiddenNode.sumInput = 0;
                        for (Connection connection : neatParameters.connections) {
                            if (hiddenNode.id == connection.outNodeID) {
                                Node connectionInputNode = findNodeById(connection.inNodeID);
                                assert connectionInputNode != null;
                                hiddenNode.sumInput += (connectionInputNode.sumOutput * connection.weight);
                            }
                        }
                        hiddenNode.sumOutput = 1 / (1 + exp(4.9 - hiddenNode.sumInput));
                    }
                }
            }
            //output nodes
            else {
                for (Node outputNode : neatParameters.outputNodes) {
                    if (outputNode.nodeLayer == layerCount) {
                        outputNode.sumInput = 0;
                        for (Connection connection : neatParameters.connections) {
                            if (outputNode.id == connection.outNodeID) {
                                Node connectionInputNode = findNodeById(connection.inNodeID);
                                assert connectionInputNode != null;
                                outputNode.sumInput += (connectionInputNode.sumOutput * connection.weight);
                            }
                        }
                        outputNode.sumOutput = 1 / (1 + exp(-outputNode.sumInput));
                    }
                }
            }
        }
        // print results
//        printSumOutputs();
    }

    private Node findNodeById(int nodeId) {
        for (Node inputNode : neatParameters.inputNodes) {
            if (inputNode.id == nodeId)
                return inputNode;
        }
        for (Node outputNode : neatParameters.outputNodes) {
            if (outputNode.id == nodeId)
                return outputNode;
        }
        for (Node hiddenNode : neatParameters.hiddenNodes) {
            if (hiddenNode.id == nodeId)
                return hiddenNode;
        }
        return null;
    }

    private void printSumOutputs() {
        for (Node node : neatParameters.inputNodes) {
            System.out.println("Input node " + node.id + ": input value = " + node.sumInput + " and output value = " + node.sumOutput);
        }
        for (Node node : neatParameters.hiddenNodes) {
            System.out.println("Hidden node " + node.id + ": input value = " + node.sumInput + " and output value = " + node.sumOutput);
        }
        for (Node node : neatParameters.outputNodes) {
            System.out.println("Output node " + node.id + ": input value = " + node.sumInput + " and output value = " + node.sumOutput);
        }
    }

    public double getOutput(int nodeID){
        return findNodeById(nodeID) != null ? findNodeById(nodeID).sumOutput : -1;
    }

    public void copyFrom(Brain other) {
        // copy parameters
        this.brainID = other.brainID;
        this.neatParameters = new NeatParameters(other.neatParameters.populationSize,
                other.neatParameters.inputNodesNumber,
                other.neatParameters.outputNodesNumber,
                other.neatParameters.hiddenNodesNumber,
                other.neatParameters.percentageConn);

        this.fitness = other.fitness;
        this.adjustedFitness = other.adjustedFitness;
        this.outputNodeID = other.outputNodeID;
        this.speciesID = other.speciesID;

        // copy input nodes
        this.neatParameters.inputNodes.clear();
        for (Node inputNode : other.neatParameters.inputNodes) {
            this.neatParameters.inputNodes.add(new Node(inputNode.id, inputNode.nodeType, inputNode.nodeLayer, inputNode.sumInput, inputNode.sumOutput));
        }

        // copy hidden nodes
        this.neatParameters.hiddenNodes.clear();
        for (Node hiddenNode : other.neatParameters.hiddenNodes) {
            this.neatParameters.hiddenNodes.add(new Node(hiddenNode.id, hiddenNode.nodeType, hiddenNode.nodeLayer, hiddenNode.sumInput, hiddenNode.sumOutput));
        }

        // copy output nodes
        this.neatParameters.outputNodes.clear();
        for (Node outputNode : other.neatParameters.outputNodes) {
            this.neatParameters.outputNodes.add(new Node(outputNode.id, outputNode.nodeType, outputNode.nodeLayer, outputNode.sumInput, outputNode.sumOutput));
        }

        // copy connections
        this.neatParameters.connections.clear();
        for (Connection connection : other.neatParameters.connections) {
            this.neatParameters.connections.add(new Connection(connection.innovationID, connection.inNodeID, connection.outNodeID, connection.weight, connection.isEnabled, connection.isRecurrent));
        }

        // copy id counters (can be deleted imo)
        this.neatParameters.nodeIDsCounter = other.neatParameters.nodeIDsCounter;
        this.neatParameters.innovationIDsCounter = other.neatParameters.innovationIDsCounter;
    }

}
