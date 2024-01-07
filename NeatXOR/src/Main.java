import classes.neuralNetworks.Brain;
import classes.NeatParameters;
import classes.neuralNetworks.Specie;
import helpers.BrainsHelper;
import helpers.ConnectionsHelper;
import helpers.ParametersHelper;
import helpers.SpeciesHelper;

import java.util.*;

public class Main {
    // general parameters
    public static boolean isElitist = false;
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

    // used during crossover
    public static int tournamentSize = 3;

    //used to keep data through generations
    public static double bestAdjustedFitnessInPopulation = 0;
    public static Brain bestBrain = null;
    public static List<Brain> bestBrainsFromEachGeneration;
    public static List<Brain> generationMembers;
    public static List<Brain> temporaryGenerationMembers;
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
        bestBrainsFromEachGeneration = new ArrayList<>();

        brainIDsCounter = 1;
        //for each generation
        for (int actualGeneration = 1; actualGeneration <= generationsNumber; actualGeneration++) {
            // 1. GENERATION PLAYS

            // Initialize first generation if needed
            if(actualGeneration == 1){
                generationMembers = new ArrayList<>();
                for (int i = 0; i < popSize; i++) {
                    //build neatParameters and Brain
                    NeatParameters neatParameters = new NeatParameters(popSize, inputNodesNumber, outputNodesNumber, hiddenNodesNumber, percentageConn);
                    Brain brain = new Brain(neatParameters, brainIDsCounter);
                    //increment counter so that each brain has a unique ID.
                    ++brainIDsCounter;
                    brain.initialize(actualGeneration);
                    // first generation will play with the 4 possible values of XOR, weights are randomized
                    for (double[] inputValues : inputValuesList) {
                        brain.loadInputs(inputValues);
                        brain.runNetwork();
                        // set fitness
                        brain.fitness += brain.getOutput(brain.outputNodeID);
                    }
                    // add brain to the generation's population
                    generationMembers.add(brain);
                }
            }
            // Work with post gen 1 generations
            else{
                for (int i = 0; i < popSize; i++) {
                    // This time the population is already initialized.
                    // It has been done in step 3 of the previous generation.
                    Brain generationalBrain = generationMembers.get(i);
                    // each generation will play with the 4 possible values of XOR, weights are randomized
                    for (int j = 0; j < generationalBrain.neatParameters.inputNodesNumber; j++) {
                        // run network
                        generationalBrain.runNetwork();
                        // set fitness
                        generationalBrain.fitness += generationalBrain.getOutput(generationalBrain.outputNodeID);
                    }
                    // add brain to the generation's population
                    generationMembers.set(i, generationalBrain);
                }
            }

            // 2. COLLECT INFO ABOUT GENERATION AND UPDATE PARAMETERS

            // First use Speciation to give a speciesID to each brain in the generation global static variable.
            SpeciesHelper.setSpeciesIDs(generationsNumber, generationMembers, c1, c2, c3, speciationThreshold);
            // Divide the fitness by the amount of brains having the speciesID
            ParametersHelper.adjustFitness(generationMembers);
            // Compute the offsprings (amount of members from each specie in the next generation)
            offsprings = ParametersHelper.computeOffsprings(generationMembers);
            // Adjust offsprings if the total isn't equal to the population size
            offsprings = ParametersHelper.adjustOffsprings(offsprings, popSize);
            // Now we can adjust the speciation threshold according to the amount of species we have in this generation
            speciationThreshold = ParametersHelper.adjustThreshold(generationMembers, speciationThreshold, targetSpeciesAmount, stepSizeForThreshold);
            // Update the species list. For existing species : update members and offsprings, recompute average fitness, increment gensSinceImproved if needed.
            // For new species : Add a new Specie to the list.
            SpeciesHelper.updateSpecies(species, generationMembers, offsprings);
            // Display information about the generation that just played
            DisplayGenerationInformation(actualGeneration);
            // Update best brain in generation and display it
            bestBrain = BrainsHelper.updateBestBrain(bestBrain, generationMembers, bestAdjustedFitnessInPopulation);
            bestBrainsFromEachGeneration.add(bestBrain);
            // Draw best brain and display information about the generation
//            DrawBestBrain(actualGeneration);

            // 3. CREATE THE NEXT GENERATION

            // Reset generation members and keep them in a temporary variable
            resetGenerationMembers();
            // GENERATION MEMBERS HAS BEEN RESET CAREFUL LOIC
            // Fill the generation with offsprings
            // Each specie has an offspring number => there will be x members of that specie
            // Total is always popSize
            fillWithOffsprings();
            // Till there, our new generation is fully in generationMembers
            // And the generation that was used just before is in temporaryGenerationMembers.
            // TODO: Update species !

            // 4. ELITISM
            if(isElitist){
                generationMembers.set(0, bestBrain);
            }

        }
    }

    private static void fillWithOffsprings(){
        // this value counts how many brains have been added to next gen.
        int addedToNextGenCounter = 0;

        // Loop on every existing species
        for(Specie existingSpecie: species){

            // select parents
            List<Brain> parents = existingSpecie.selectParentsForNextGen(tournamentSize, existingSpecie.members);

            // create offsprings and add them to the generation
            for(int i =0; i < existingSpecie.offspring; i++){
                //create a new empty Brain
                NeatParameters neatParameters = new NeatParameters(popSize, inputNodesNumber, outputNodesNumber, hiddenNodesNumber, percentageConn);
                Brain offspring = new Brain(neatParameters, brainIDsCounter);

                // Clone everything from the fittest parent
                offspring.copyFrom(BrainsHelper.getFittestBrain(parents.get(0), parents.get(1)));

                // Till now, the offspring is exactly the same as its fittest parent
                // We will now cross the connections.
                offspring.neatParameters.connections = ConnectionsHelper.getMatchingConnectionsWithRandomlyPickedWeights(parents.get(0), parents.get(1));

                // Finally, get a new id for the offspring
                // Since I know how many brains I added yet, I can increment this value by one.
                offspring.brainID = addedToNextGenCounter + 1;

                // Now the offspring should have everything it needs.
                // TODO: MUTATION
                // Add the offspring to the next generation population
                generationMembers.add(offspring);

                // And increment counter
                addedToNextGenCounter++;
            }
        }
    }

    private static void resetGenerationMembers(){
        temporaryGenerationMembers = generationMembers;
        generationMembers = new ArrayList<>();
    }

    private static void DisplayGenerationInformation(int actualGeneration){
        System.out.println("____________________ GENERATION " + actualGeneration + " ____________________");
        for (Specie specie : species) {
            // Print all the information about the specie
            System.out.println(specie.toString());
        }
    }

    private static void DrawBestBrain(int actualGeneration){
        if (bestBrain != null) {
            System.out.println("In generation " + actualGeneration + ", " + "the best brain is brain " + bestBrain.brainID + " from specie " + bestBrain.speciesID);
            System.out.println("It has a fitness = " + bestBrain.fitness + ", and an adjusted fitness = " + bestBrain.adjustedFitness);
            System.out.println("_______________________________________________________");
            //Show network topology
            bestBrain.drawNetwork();
        }
    }
}