import classes.Brain;
import classes.NeatParameters;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        int popSize = 50;
        int inputNodesNumber = 3, outputNodesNumber = 1, hiddenNodesNumber = 0;
        double percentageConn = 1.0;
        double bestFitnessInPopulation = 0;
        Brain bestBrain = null;

        //possible values for xor
        List<double[]> inputValuesList = new ArrayList<>();
        inputValuesList.add(new double[]{0.0, 0.0, 1.0});
        inputValuesList.add(new double[]{0.0, 1.0, 1.0});
        inputValuesList.add(new double[]{1.0, 0.0, 1.0});
        inputValuesList.add(new double[]{1.0, 1.0, 1.0});

        NeatParameters neatParameters = new NeatParameters(popSize, inputNodesNumber, outputNodesNumber, hiddenNodesNumber, percentageConn);
        Brain brain = new Brain(neatParameters);
        for(int i = 0; i < brain.neatParameters.populationSize; i++){
            brain.initialize();
            // each generation will play with the 4 possible values of XOR, weights are randomized
            for (double[] inputValues : inputValuesList) {
                brain.loadInputs(inputValues);
                brain.runNetwork();
                brain.fitness += brain.getOutput(brain.outputNodeID);
            }
            // keep the best brain in a variable
            if(brain.fitness > bestFitnessInPopulation){
                bestBrain = new Brain(neatParameters);
                bestBrain.copyFrom(brain);
                bestFitnessInPopulation = brain.fitness;
            }
            System.out.println("Best fitness for this population : " + bestFitnessInPopulation);
        }
        // display best brain
        if (bestBrain != null) {
            bestBrain.drawNetwork();
        }
    }
}