package classes.neurons;

public class Neuron {
    private int id;
    protected double value;

    public Neuron(int id) {
        this.id = id;
        this.value = 0.0;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
