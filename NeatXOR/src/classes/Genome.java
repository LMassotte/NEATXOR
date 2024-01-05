package classes;

import classes.neurons.Neuron;

import java.util.List;

public class Genome {
    private List<Neuron> neurons;
    private List<Connection> connections;

    public Genome(List<Neuron> neurons, List<Connection> connections) {
        this.neurons = neurons;
        this.connections = connections;
    }

    public List<Neuron> getNeurons() {
        return neurons;
    }

    public void setNeurons(List<Neuron> neurons) {
        this.neurons = neurons;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }
}
