package classes.canvas;

import classes.Connection;
import classes.nodes.Node;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class NeuralNetworkCanvas extends JPanel {
    private final int maxLayer;
    private final int maxNodesCount;
    private final double nodeHeight;
    private final List<Node> inputNodes;
    private final List<Node> hiddenNodes;
    private final List<Node> outputNodes;
    private final List<Connection> connections;

    public NeuralNetworkCanvas(List<Node> inputNodes, List<Node> hiddenNodes, List<Node> outputNodes, List<Connection> connections) {
        this.inputNodes = inputNodes;
        this.hiddenNodes = hiddenNodes;
        this.outputNodes = outputNodes;
        this.connections = connections;

        inputNodes.sort(Comparator.comparingInt(node -> node.id));
        outputNodes.sort(Comparator.comparingInt(node -> node.id));
        hiddenNodes.sort(Comparator.comparingInt(node -> node.id));
        connections.sort(Comparator.comparingInt(connection -> connection.innovationID));

        // these are the dimensions of the grid
        maxLayer = getMaxLayer(outputNodes);
        maxNodesCount = Math.max(inputNodes.size(), Math.max(outputNodes.size(), hiddenNodes.size()));

        nodeHeight = 20.0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        // Draw connections
        drawConnections(g, connections);
        // Draw all nodes
        drawNodes(g, inputNodes);
        //1 = input layer, max layer = output layer. This loop manages hidden nodes in different layers
        for (int i = 2; i < maxLayer; i++) {
            int finalI = i;
            List<Node> hiddenNodesSublist = hiddenNodes.stream().filter(node -> node.nodeLayer == finalI).toList();
            drawNodes(g, hiddenNodesSublist);
        }
        drawNodes(g, outputNodes);
    }

    private void drawNodes(Graphics g, List<Node> nodes) {
        g.setColor(Color.white);

        double maxHeight = maxNodesCount * 50 + (nodeHeight / 2);

        double index = 1;
        for (Node node : nodes) {
            double distanceInter = (maxHeight - ((double) nodes.size() * nodeHeight)) / ((double) nodes.size() + 1);

            double x = node.nodeLayer * 100;
            double y = (distanceInter * index) + (nodeHeight / 2) + (nodeHeight * (index - 1));
            g.fillOval((int) x, (int) y, 20, 20); // node = circle
            g.drawString(String.valueOf(node.id), (int) x + 8, (int) y + 12); // node id
            ++index;
        }
    }

    private void drawConnections(Graphics g, List<Connection> connections) {

        int inX = 0, inY = 0, outX = 0, outY = 0;
        double maxHeight = maxNodesCount * 50 + 10;

        for (Connection connection : connections) {
            Color color = connection.isRecurrent ? Color.blue : connection.isEnabled ? Color.green : Color.red;
            g.setColor(color);
            Node inNode = findNodeById(connection.inNodeID);
            Node outNode = findNodeById(connection.outNodeID);

            // find position for inNode when it's an input node
            assert inNode != null;
            if (inNode.nodeType == 1 || inNode.nodeType == 3) {
                int nodePosition = (findNodePosition(inputNodes, inNode.id) + 1);
                inX = inNode.nodeLayer * 100 + 10;
                double distanceInter = (maxHeight - ((double) inputNodes.size() * 20)) / ((double) inputNodes.size() + 1);
                inY = (int) (distanceInter * nodePosition) + 10 + (20 * (nodePosition - 1)) + 10;
            }
            // find position for inNode when it's a hidden node
            else if (inNode.nodeType == 0) {
                List<Node> sameLayerHiddenNodes = hiddenNodes.stream().filter(node -> node.nodeLayer == inNode.nodeLayer).toList();
                int nodePosition = (findNodePosition(sameLayerHiddenNodes, inNode.id) + 1);
                inX = inNode.nodeLayer * 100 + 10;
                double distanceInter = (maxHeight - ((double) sameLayerHiddenNodes.size() * 20)) / ((double) sameLayerHiddenNodes.size() + 1);
                inY = (int) (distanceInter * nodePosition) + 10 + (20 * (nodePosition - 1)) + 10;
            }
            // find position for inNode when it's an output node
            else if (inNode.nodeType == 2) {
                int nodePosition = (findNodePosition(outputNodes, inNode.id) + 1);
                inX = inNode.nodeLayer * 100 + 10;
                double distanceInter = (maxHeight - ((double) outputNodes.size() * 20)) / ((double) outputNodes.size() + 1);
                inY = (int) (distanceInter * nodePosition) + 10 + (20 * (nodePosition - 1)) + 10;
            }
            // find position for outNode when it's an input node
            assert outNode != null;
            if (outNode.nodeType == 1 || outNode.nodeType == 3) {
                int nodePosition = (findNodePosition(inputNodes, outNode.id) + 1);
                outX = outNode.nodeLayer * 100 + 10;
                double distanceInter = (maxHeight - ((double) inputNodes.size() * 20)) / ((double) inputNodes.size() + 1);
                outY = (int) (distanceInter * nodePosition) + 10 + (20 * (nodePosition - 1)) + 10;
            }
            // find position for outNode when it's a hidden node
            else if (outNode.nodeType == 0) {
                List<Node> sameLayerHiddenNodes = hiddenNodes.stream().filter(node -> node.nodeLayer == outNode.nodeLayer).toList();
                int nodePosition = (findNodePosition(sameLayerHiddenNodes, outNode.id) + 1);
                outX = outNode.nodeLayer * 100 + 10;
                double distanceInter = (maxHeight - ((double) sameLayerHiddenNodes.size() * 20)) / ((double) sameLayerHiddenNodes.size() + 1);
                outY = (int) (distanceInter * nodePosition) + 10 + (20 * (nodePosition - 1)) + 10;
            }
            // find position for outNode when it's an output node
            else if (outNode.nodeType == 2) {
                int nodePosition = (findNodePosition(outputNodes, outNode.id) + 1);
                outX = outNode.nodeLayer * 100 + 10;
                double distanceInter = (maxHeight - ((double) outputNodes.size() * 20)) / ((double) outputNodes.size() + 1);
                outY = (int) (distanceInter * nodePosition) + 10 + (20 * (nodePosition - 1)) + 10;
            }

            g.drawLine(inX, inY, outX, outY);
        }
    }

    private Node findNodeById(int nodeId) {
        for (Node inputNode : inputNodes) {
            if (inputNode.id == nodeId)
                return inputNode;
        }
        for (Node outputNode : outputNodes) {
            if (outputNode.id == nodeId)
                return outputNode;
        }
        for (Node hiddenNode : hiddenNodes) {
            if (hiddenNode.id == nodeId)
                return hiddenNode;
        }
        return null;
    }

    private static int findNodePosition(List<Node> nodeList, int targetId) {
        for (int i = 0; i < nodeList.size(); i++) {
            if (nodeList.get(i).id == targetId) {
                return i;
            }
        }
        return -1;
    }

    private int getMaxLayer(List<Node> nodes) {
        int maxLayer = 0;
        for (Node node : nodes) {
            maxLayer = Math.max(maxLayer, node.nodeLayer);
        }
        return maxLayer;
    }
}
