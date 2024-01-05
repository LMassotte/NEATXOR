package classes.canvas;
import classes.Connection;
import classes.nodes.Node;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class NeuralNetworkCanvas extends JPanel {
    private int maxLayer;
    private int maxNodesCount;
    private List<Node> inputNodes;
    private List<Node> hiddenNodes;
    private List<Node> outputNodes;
    private List<Connection> connections;

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
        drawNodes(g, hiddenNodes);
        drawNodes(g, outputNodes);
    }

    private void drawNodes(Graphics g, List<Node> nodes) {
        g.setColor(Color.white);

        double maxHeight = maxNodesCount * 50 + 10;

        double index = 1;
        for (Node node : nodes) {
            double distanceInter = (maxHeight - ((double) nodes.size() * 20)) / ((double) nodes.size() + 1);

            double x = node.nodeLayer * 100;
            double y = (distanceInter * index) + 10 + (20 * (index -1));
            System.out.println(y);
            g.fillOval((int) x, (int) y, 20, 20); // node = circle
            g.drawString(String.valueOf(node.id), (int)x + 8, (int)y + 12); // node id
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
        if (inNode.nodeType == 1 || inNode.nodeType == 3) {
            int nodePosition = (findNodePosition(inputNodes, inNode.id) + 1);
            inX = 110;
            double distanceInter = (maxHeight - ((double) inputNodes.size() * 20)) / ((double) inputNodes.size() + 1);
            inY = (int)(distanceInter * nodePosition) + 10 + (20 * (nodePosition -1)) + 10;
        }
        // find position for inNode when it's a hidden node
        else if (inNode.nodeType == 0) {
            int nodePosition = (findNodePosition(hiddenNodes, inNode.id) + 1);
            inX = 210;
            double distanceInter = (maxHeight - ((double) hiddenNodes.size() * 20)) / ((double) hiddenNodes.size() + 1);
            inY = (int)(distanceInter * nodePosition) + 10 + (20 * (nodePosition -1)) + 10;
        }
        // find position for inNode when it's an output node
        else if (inNode.nodeType == 2) {
            int nodePosition = (findNodePosition(outputNodes, inNode.id) + 1);
            inX = 310;
            double distanceInter = (maxHeight - ((double) outputNodes.size() * 20)) / ((double) outputNodes.size() + 1);
            inY = (int)(distanceInter * nodePosition) + 10 + (20 * (nodePosition -1)) + 10;
        }
        // find position for outNode when it's an input node
        if (outNode.nodeType == 1 || outNode.nodeType == 3) {
            int nodePosition = (findNodePosition(inputNodes, outNode.id) + 1);
            outX = 110;
            double distanceInter = (maxHeight - ((double) inputNodes.size() * 20)) / ((double) inputNodes.size() + 1);
            outY = (int)(distanceInter * nodePosition) + 10 + (20 * (nodePosition -1)) + 10;
        }
        // find position for outNode when it's a hidden node
        else if (outNode.nodeType == 0) {
            int nodePosition = (findNodePosition(hiddenNodes, outNode.id) + 1);
            outX = 210;
            double distanceInter = (maxHeight - ((double) hiddenNodes.size() * 20)) / ((double) hiddenNodes.size() + 1);
            outY = (int)(distanceInter * nodePosition) + 10 + (20 * (nodePosition -1)) + 10;
        }
        // find position for outNode when it's an output node
        else if (outNode.nodeType == 2) {
            int nodePosition = (findNodePosition(outputNodes, outNode.id) + 1);
            outX = 310;
            double distanceInter = (maxHeight - ((double) outputNodes.size() * 20)) / ((double) outputNodes.size() + 1);
            outY = (int)(distanceInter * nodePosition) + 10 + (20 * (nodePosition -1)) + 10;
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
