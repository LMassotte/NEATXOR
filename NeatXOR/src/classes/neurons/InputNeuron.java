package classes.neurons;

public class InputNeuron extends Neuron{
    public InputNeuron(int id) {
        super(id);
    }

    void setInputValue(double inputValue) {
        this.value = inputValue;
    }

    void calculateValue() {
        // Les neurones d'entrée ont leur valeur définie directement par les données d'entrée.
    }
}
