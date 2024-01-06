import classes.Brain;
import classes.NeatParameters;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int popSize = 50;
        int inputNodesNumber = 3, outputNodesNumber = 1, hiddenNodesNumber = 0;
        double percentageConn = 1.0;

        //possible values for xor
        List<double[]> inputValuesList = new ArrayList<>();
        inputValuesList.add(new double[]{0.0, 0.0, 1.0});
        inputValuesList.add(new double[]{0.0, 1.0, 1.0});
        inputValuesList.add(new double[]{1.0, 0.0, 1.0});
        inputValuesList.add(new double[]{1.0, 1.0, 1.0});

        NeatParameters neatParameters = new NeatParameters(popSize, inputNodesNumber, outputNodesNumber, hiddenNodesNumber, percentageConn);
        Brain brain = new Brain(neatParameters);

        brain.initialize();
        for (double[] inputValues : inputValuesList) {
            brain.loadInputs(inputValues);
            brain.drawNetwork();
            brain.runNetwork();
            brain.fitness += brain.getOutput(brain.outputNodeID);
        }
        System.out.println("Final fitness : " + brain.fitness);
    }
}