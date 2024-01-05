package classes.canvas;

import classes.Brain;
import classes.Connection;
import classes.NeatParameters;
import classes.nodes.Node;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NeuralNetworkCanvas extends JPanel {
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw all nodes
        drawNodes(g, inputNodes, Color.BLUE);
        drawNodes(g, hiddenNodes, Color.GREEN);
        drawNodes(g, outputNodes, Color.RED);

        // Draw connections
        drawConnections(g, connections);
    }

    private void drawNodes(Graphics g, List<Node> nodes, Color color) {
        g.setColor(color);
        int index = 1;

        for (Node node : nodes) {
            int x = node.nodeLayer * 100;
            int y = index * 50;

            g.fillOval(x, y, 20, 20); // node = circle
            g.drawString(String.valueOf(node.id), x + 8, y + 12); // node id
            ++index;
        }
    }

    private void drawConnections(Graphics g, List<Connection> connections) {
        g.setColor(Color.white);
        int inX = 0, inY = 0, outX = 0, outY = 0;
        for (Connection connection : connections) {
            Node inNode = findNodeById(connection.inNodeID);
            Node outNode = findNodeById(connection.outNodeID);

            // find position for inNode when it's an input node
            if(inNode.nodeType == 1 || inNode.nodeType == 3){
                inX = 110;
                inY = 10 +(findNodePosition(inputNodes, inNode.id) + 1) * 50;
            }
            // find position for inNode when it's a hidden node
            else if(inNode.nodeType == 0){
                inX = 210;
                inY = 10 +(findNodePosition(hiddenNodes, inNode.id) + 1) * 50;
            }
            // find position for inNode when it's an output node
            else if(inNode.nodeType == 2){
                inX = 310;
                inY = 10 +(findNodePosition(outputNodes, inNode.id) + 1) * 50;
            }
            // find position for outNode when it's an input node
            if(outNode.nodeType == 1 || outNode.nodeType == 3){
                outX = 110;
                outY = 10 +(findNodePosition(inputNodes, outNode.id) + 1) * 50;
            }
            // find position for outNode when it's a hidden node
            else if(outNode.nodeType == 0){
                outX = 210;
                outY = 10 +(findNodePosition(hiddenNodes, outNode.id) + 1) * 50;
            }
            // find position for outNode when it's an output node
            else if(outNode.nodeType == 2){
                outX = 310;
                outY = 10 +(findNodePosition(outputNodes, outNode.id) + 1) * 50;
            }

            g.drawLine(inX, inY, outX, outY);
        }
    }

    private Node findNodeById(int nodeId) {
        for (Node inputNode : inputNodes){
            if(inputNode.id == nodeId)
                return inputNode;
        }
        for (Node outputNode : outputNodes){
            if(outputNode.id == nodeId)
                return outputNode;
        }
        for (Node hiddenNode : hiddenNodes){
            if(hiddenNode.id == nodeId)
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
}
