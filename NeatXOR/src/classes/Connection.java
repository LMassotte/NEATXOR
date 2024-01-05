package classes;

public class Connection {
    private int inNeuronId;
    private int outNeuronId;
    private double weight;
    private boolean enabled;

    public Connection(int inNeuronId, int outNeuronId, double weight) {
        this.inNeuronId = inNeuronId;
        this.outNeuronId = outNeuronId;
        this.weight = weight;
        this.enabled = true;
    }

    public int getInNeuronId() {
        return inNeuronId;
    }

    public void setInNeuronId(int inNeuronId) {
        this.inNeuronId = inNeuronId;
    }

    public int getOutNeuronId() {
        return outNeuronId;
    }

    public void setOutNeuronId(int outNeuronId) {
        this.outNeuronId = outNeuronId;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
