import classes.Brain;
import classes.NeatParameters;
import classes.Specie;
import classes.nodes.Connection;
import helpers.ConnectionsHelper;
import helpers.ParametersHelper;
import helpers.SpeciesHelper;

import java.util.*;

public class Main {
    // general parameters
    public static int generationsNumber = 1;
    public static int brainIDsCounter = 1;
    public static int popSize = 50;
    public static int inputNodesNumber = 3;
    public static int outputNodesNumber = 1;
    public static int hiddenNodesNumber = 0;
    public static double percentageConn = 1.0;
    // goal
    public static double targetFitness = 3.7;
    public static int targetSpeciesAmount = 5;

    // used during speciation
    public static double c1 = 1.0, c2 = 1.0, c3 = 0.4;
    // NEAT IA YouTube channel : define a step size of 0.5
    // First threshold is very high, and for each generation where all the nn are in the same specie, decrement it by the step size.
    // He also defines a target number of species. If the amount of species gets higher than the target, he increments the threshold by the step size.
    // Ken uses a step size of 0.3.
    public static double stepSizeForThreshold = 0.01;
    public static double speciationThreshold = 0.1;

    //used to keep data through generations
    public static double bestAdjustedFitnessInPopulation = 0;
    public static Brain bestBrain = null;
    public static List<Brain> generationMembers;
    public static List<Specie> species;

    //used for next generation building (the offsprings are in the order of the species)
    public static List<Integer> offsprings;

    public static void main(String[] args) {

        //possible values for xor
        List<double[]> inputValuesList = new ArrayList<>();
        inputValuesList.add(new double[]{0.0, 0.0, 1.0});
        inputValuesList.add(new double[]{0.0, 1.0, 1.0});
        inputValuesList.add(new double[]{1.0, 0.0, 1.0});
        inputValuesList.add(new double[]{1.0, 1.0, 1.0});

        species = new ArrayList<>();
        offsprings = new ArrayList<>();

        //for each generation
        for (int actualGeneration = 1; actualGeneration <= generationsNumber; actualGeneration++) {
            generationMembers = new ArrayList<>();
            // make a generation play
            for (int i = 0; i < popSize; i++) {
                //build neatParameters and Brain
                NeatParameters neatParameters = new NeatParameters(popSize, inputNodesNumber, outputNodesNumber, hiddenNodesNumber, percentageConn);
                Brain brain = new Brain(neatParameters, brainIDsCounter);
                //increment counter so that each brain has a unique ID.
                ++brainIDsCounter;
                brain.initialize(actualGeneration);
                // each generation will play with the 4 possible values of XOR, weights are randomized
                for (double[] inputValues : inputValuesList) {
                    brain.loadInputs(inputValues);
                    brain.runNetwork();
                    // set fitness
                    brain.fitness += brain.getOutput(brain.outputNodeID);
                }
                // keep the best brain in a variable


                generationMembers.add(brain);
            }
            // build the next generation
            // First use Speciation to give a speciesID to each brain in the generation global static variable.
            SpeciesHelper.setSpeciesIDs(generationsNumber, generationMembers, c1, c2, c3, speciationThreshold);
            // Divide the fitness by the amount of brains having the speciesID
            ParametersHelper.adjustFitness(generationMembers);
            // Compute the offsprings (amount of members from each specie in the next generation)
            offsprings = ParametersHelper.computeOffsprings(generationMembers);
            // Now we can adjust the speciation threshold according to the amount of species we have in this generation
            speciationThreshold = ParametersHelper.adjustThreshold(generationMembers, speciationThreshold, targetSpeciesAmount, stepSizeForThreshold);
            // Update the species list. For existing species : update members and offsprings, recompute average fitness, increment gensSinceImproved if needed.
            // For new species : Add a new Specie to the list.
            SpeciesHelper.updateSpecies(species, generationMembers, offsprings);
            System.out.println("____________________ GENERATION " + generationsNumber + " ____________________");
            for (Specie specie : species) {
                System.out.println(specie.toString());
            }
        }
        // Update best brain in generation and display it
        updateAndDisplayBestBrain();
        //Show network topology
        bestBrain.drawNetwork();

    }

    private static void updateAndDisplayBestBrain() {
        // find and display best brain in generation
        for (Brain generationBrain : generationMembers) {
            if (generationBrain.adjustedFitness > bestAdjustedFitnessInPopulation) {
                bestBrain = new Brain(generationBrain.neatParameters, -1);
                bestBrain.copyFrom(generationBrain);
                bestAdjustedFitnessInPopulation = generationBrain.adjustedFitness;
            }
        }
        if (bestBrain != null) {
            System.out.println("In generation " + generationsNumber + ", " + "th best brain is brain " + bestBrain.brainID + " from specie " + bestBrain.speciesID);
            System.out.println("It has a fitness = " + bestBrain.fitness + ", and an adjusted fitness = " + bestBrain.adjustedFitness);
            System.out.println("__________________________________________________");
        }
    }
}