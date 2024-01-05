package classes;

import classes.nodes.Node;

import java.util.List;
import java.util.ArrayList;

public class Genome {
    private List<Node> nodes;
    private List<Connection> connections;

    public Genome(List<Node> nodes, List<Connection> connections) {
        this.nodes = nodes;
        this.connections = connections;
    }

    //renvoie les connections allant vers un neurone
    public List<Connection> getConnectionsForNeuron(int neuronId) {
        List<Connection> neuronConnections = new ArrayList<>();

        for (Connection connection : connections) {
            if (connection.getOutNeuron().getId() == neuronId) {
                neuronConnections.add(connection);
            }
        }

        return neuronConnections;
    }

    public List<Node> getNeurons() {
        return nodes;
    }

    public void setNeurons(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }
}
