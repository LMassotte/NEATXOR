package classes.neuralNetworks;

import classes.NeatParameters;
import classes.canvas.NeuralNetworkCanvas;
import classes.nodes.Connection;
import classes.nodes.Node;

import javax.swing.*;
import java.util.*;

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

        addNode(1, 1, 0, 0);
        addNode(1, 1, 0, 0);
        addNode(3, 1, 0, 0);
        addNode(2, 3, 0, 0);

        outputNodeID = neatParameters.outputNodes.get(neatParameters.outputNodes.size() - 1).nodeID;

        if (generationNumber == 1) {
            addConnection(1, outputNodeID, true, false);
            addConnection(2, outputNodeID, true, false);
            addConnection(3, outputNodeID, true, false);
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
    public void addConnection(int inNodeID, int outNodeID, boolean isEnabled, boolean isRecurrent) {
        Random random = new Random();
        double weight = random.nextDouble() * 2 - 1;
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

    public void mutateNewConnection() {
        Random rand = new Random();
        int randomBrainMutationPercentage = rand.nextInt(100) + 1;
        // 5% chances of mutation
        if (randomBrainMutationPercentage <= 5) {
            // Merge all nodes of the brain in one list
            List<Node> brainNodes = new ArrayList<>();
            brainNodes.addAll(this.neatParameters.inputNodes);
            brainNodes.addAll(this.neatParameters.hiddenNodes);
            brainNodes.addAll(this.neatParameters.outputNodes);

            Node inNode = new Node();
            Node outNode = new Node();
            // Select 2 nodes as inNode and outNode th
            int counter = 0;
            while (counter < 20) {
                boolean validationFlag = true;
                boolean reenabledConnectionFlag = false;
                inNode = brainNodes.get(rand.nextInt(brainNodes.size()));
                outNode = brainNodes.get(rand.nextInt(brainNodes.size()));

                // First verification : not the same node
                if (inNode.nodeID == outNode.nodeID) {
                    validationFlag = false;
                }
                // Second verification : not on the same layer
                if (inNode.nodeLayer == outNode.nodeLayer) {
                    validationFlag = false;
                }
                // Third verification : output is in a deeper layer than input
                if (inNode.nodeLayer > outNode.nodeLayer) {
                    validationFlag = false;
                }
                // Fourth verification : no existing connection from this input to this output
                for (Connection connection : this.neatParameters.connections) {
                    if (connection.inNodeID == inNode.nodeID && connection.outNodeID == outNode.nodeID) {
                        // If there is a connection between the 2 nodes
                        // AND if it is actually disabled
                        // 25% chance of it being enabled again
                        // If it's re-enabled, break out of the foreach loop and reset inNode and outNode to -1 values.
                        if(!connection.isEnabled){
                            int enablingConnectionRandomChances = rand.nextInt(100) + 1;
                            if(enablingConnectionRandomChances <= 25){
                                inNode = new Node();
                                outNode = new Node();
                                connection.isEnabled = true;
                                reenabledConnectionFlag = true;
                                break;
                            }
                        }
                        validationFlag = false;
                    }
                    // Just to be sure, check if the connection hasn't been created backward
                    if (connection.inNodeID == outNode.nodeID && connection.outNodeID == inNode.nodeID) {
                        validationFlag = false;
                    }
                }
                // If eligible nodes have been found OR a connection has been re-enabled, break out of the loop.
                if (validationFlag || reenabledConnectionFlag) {
                    break;
                }
                if(counter == 19){
                    // Failed too much times.
                    // Reset nodes. All values are set to -1 including the nodeID.
                    // It won't be added to the connections list.
                    inNode = new Node();
                    outNode = new Node();
                }
                ++counter;
            }
            // If after 20 tries, both nodeIDs successfully passed through the validation tests, add a new connection
            // (Weight will be randomized, innovationID will take the next unique available)
            if(inNode.nodeID != -1 && outNode.nodeID != -1){
                addConnection(inNode.nodeID, outNode.nodeID, true, false);
//                System.out.println("New connection added through mutation : ");
                Connection tmpConnection = Collections.max(this.neatParameters.connections, Comparator.comparingInt(c -> c.innovationID));
//                System.out.println(tmpConnection);
            }
        }
    }

    public void mutateWeights() {
        Random rand = new Random();
        int randomBrainMutationPercentage = rand.nextInt(100) + 1;
        // 80% chances of mutation
        if (randomBrainMutationPercentage <= 80) {
            for (Connection connection : this.neatParameters.connections) {
                int randomConnectionMutationPercentage = rand.nextInt(100) + 1;
                if (randomConnectionMutationPercentage <= 90) {
                    int randomAddOrSubstract = rand.nextInt(2);
                    // Note: weight has to stay between -1 and 1
//                    System.out.println("Connection " + connection.innovationID + " had a weight of " + connection.weight);
                    // If it's getting too high, force lower it
                    if ((connection.weight + connection.weight * 0.2) > 0.99) {
                        connection.weight = connection.weight - connection.weight * 0.2;
                    }
                    // If it's getting too low, force higher it
                    else if (connection.weight - connection.weight * 0.2 < -0.99) {
                        connection.weight = connection.weight + connection.weight * 0.2;
                    }
                    // If it's balanced, randomly higher or lower it
                    else if (-0.79 < connection.weight && connection.weight < 0.79) {
                        connection.weight = randomAddOrSubstract == 0 ? connection.weight + connection.weight * 0.2 : connection.weight - connection.weight * 0.2;
                    }
//                    System.out.println(" and i modify it to " + connection.weight);
                } else {
                    connection.weight = rand.nextDouble() * 2 - 1;
                }
            }
        }
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
                            if (hiddenNode.nodeID == connection.outNodeID) {
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
                            if (outputNode.nodeID == connection.outNodeID) {
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
            if (inputNode.nodeID == nodeId)
                return inputNode;
        }
        for (Node outputNode : neatParameters.outputNodes) {
            if (outputNode.nodeID == nodeId)
                return outputNode;
        }
        for (Node hiddenNode : neatParameters.hiddenNodes) {
            if (hiddenNode.nodeID == nodeId)
                return hiddenNode;
        }
        return null;
    }

    private void printSumOutputs() {
        for (Node node : neatParameters.inputNodes) {
            System.out.println("Input node " + node.nodeID + ": input value = " + node.sumInput + " and output value = " + node.sumOutput);
        }
        for (Node node : neatParameters.hiddenNodes) {
            System.out.println("Hidden node " + node.nodeID + ": input value = " + node.sumInput + " and output value = " + node.sumOutput);
        }
        for (Node node : neatParameters.outputNodes) {
            System.out.println("Output node " + node.nodeID + ": input value = " + node.sumInput + " and output value = " + node.sumOutput);
        }
    }

    public double getOutput(int nodeID) {
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
            this.neatParameters.inputNodes.add(new Node(inputNode.nodeID, inputNode.nodeType, inputNode.nodeLayer, inputNode.sumInput, inputNode.sumOutput));
        }

        // copy hidden nodes
        this.neatParameters.hiddenNodes.clear();
        for (Node hiddenNode : other.neatParameters.hiddenNodes) {
            this.neatParameters.hiddenNodes.add(new Node(hiddenNode.nodeID, hiddenNode.nodeType, hiddenNode.nodeLayer, hiddenNode.sumInput, hiddenNode.sumOutput));
        }

        // copy output nodes
        this.neatParameters.outputNodes.clear();
        for (Node outputNode : other.neatParameters.outputNodes) {
            this.neatParameters.outputNodes.add(new Node(outputNode.nodeID, outputNode.nodeType, outputNode.nodeLayer, outputNode.sumInput, outputNode.sumOutput));
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

    @Override
    public String toString() {
        return "This brain (Brain " + this.brainID + ") of specie " + this.speciesID + " has " + this.neatParameters.inputNodes.size() + " inputs, "
                + this.neatParameters.hiddenNodesNumber + " hidden nodes and " + this.neatParameters.outputNodes.size() + " outputs."
                + "It has " + this.neatParameters.connections.size() + " connections.";
    }
}
