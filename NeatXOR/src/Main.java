import classes.neuralNetworks.Brain;
import classes.NeatParameters;
import classes.neuralNetworks.Specie;
import helpers.BrainsHelper;
import helpers.ConnectionsHelper;
import helpers.ParametersHelper;
import helpers.SpeciesHelper;

import javax.swing.*;
import java.util.*;

public class Main {
    // jframe
    public static JFrame frame = new JFrame("Neural Network Visualization");
    // general parameters
    public static double fitnessMax = 4;
    public static boolean isElitist = false;
    public static int generationsNumber = 1000;
    public static int brainIDsCounter = 1;
    public static int popSize = 50;
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
    public static double speciationThreshold = 1.0;

    // used during crossover
    public static int tournamentSize = 3;

    //used to keep data through generations
    public static Brain bestBrain;
    public static List<Brain> generationMembers;
    public static List<Brain> temporaryGenerationMembers;
    public static List<Specie> species;

    public static void main(String[] args) {



        NeatParameters neatParameters = new NeatParameters(popSize, percentageConn);
        brainIDsCounter = 1;
        bestBrain = new Brain(neatParameters, 1);
        bestBrain.adjustedFitness = 0.0;

        species = new ArrayList<>();


        //for each generation
//        for (int actualGeneration = 1; actualGeneration <= generationsNumber; actualGeneration++) {
        for (int actualGeneration = 1; bestBrain.adjustedFitness < 3.85; actualGeneration++) {
            // 1. GENERATION PLAYS

            // Initialize first generation if needed
            if(actualGeneration == 1){
                generationMembers = new ArrayList<>();
                for (int i = 0; i < popSize; i++) {
                    //build neatParameters and Brain
                    neatParameters = new NeatParameters(popSize, percentageConn);
                    Brain brain = new Brain(neatParameters, brainIDsCounter);
                    //increment counter so that each brain has a unique ID.
                    ++brainIDsCounter;
                    brain.initialize(actualGeneration);
                    // first generation's members will play with the 4 possible values of XOR, weights are randomized

                    runXOR(brain);

                    // add brain to the generation's population
                    generationMembers.add(brain);
                }
            }
            // Work with post gen 1 generations
            else{
                // Population is already initialized (cfr 3. of last generation)
                for (int i = 0; i < popSize; i++) {
                    // XOR
                    runXOR(generationMembers.get(i));
                }
            }

            // 2. COLLECT INFO ABOUT GENERATION AND UPDATE PARAMETERS
            // First use Speciation to give a speciesID to each brain in the generation global static variable.
            SpeciesHelper.setSpeciesIDs(actualGeneration, generationMembers, species, c1, c2, c3, speciationThreshold);

            // Divide the fitness by the amount of brains having the speciesID
            ParametersHelper.adjustFitness(generationMembers);
            updateSpeciesMembersList();

            // Update the species list. For existing species : update members and offsprings, recompute average fitness, increment gensSinceImproved if needed.
            // For new species : Add a new Specie to the list.
            SpeciesHelper.addAndUpdateSpecies(species, generationMembers);
            // Number of species can grow very fast, I want the IDs to be continuous
            SpeciesHelper.normalizeSpeciesIDs(species);

            // Compute the offsprings (amount of members from each specie in the next generation)
            ParametersHelper.computeOffsprings(generationMembers, species);

            // Adjust offsprings if the total isn't equal to the population size
            ParametersHelper.adjustOffsprings(popSize, species);

            // Update species members list, because fitness have changed
            updateSpeciesMembersList();

            // Now we can adjust the speciation threshold according to the amount of species we have in this generation
            speciationThreshold = ParametersHelper.adjustThreshold(generationMembers, speciationThreshold, targetSpeciesAmount, stepSizeForThreshold);

            // Display information about the generation that just played
//            DisplayGenerationInformation(actualGeneration);

            // Update best brain in generation and display it
            bestBrain = BrainsHelper.updateBestBrain(bestBrain, generationMembers);

            // Draw best brain and display information about the generation
//            DrawBestBrain(actualGeneration);

            // 3. CREATE THE NEXT GENERATION

            // Reset generation members and keep them in a temporary variable
            resetGenerationMembers();
            // GENERATION MEMBERS HAS BEEN RESET CAREFUL LOIC
            // Fill the generation with offsprings
            // Each specie has an offspring number => there will be x members of that specie
            // Total is always popSize
            fillWithOffsprings(actualGeneration);
            // Till there, our new generation is fully in generationMembers

            // 4. MUTATION

            // Mutate brains (80% chance that its weights will be modified, 5% chance of having a new connection)
            BrainsHelper.mutateBrains(generationMembers);

            // Update species list with updated members
            updateSpeciesMembersList();

            // 4. ELITISM
            if(isElitist){
                generationMembers.set(0, bestBrain);
            }
//            generationMembers.get(0).drawNetwork(frame);
            DisplayBestBrain(actualGeneration);

        }
//        bestBrain.drawNetwork(frame);
    }

    private static void runXOR(Brain brain){
        //possible values for xor
        List<double[]> inputValuesList = new ArrayList<>();
        inputValuesList.add(new double[]{0.0, 0.0, 1.0});
        inputValuesList.add(new double[]{0.0, 1.0, 1.0});
        inputValuesList.add(new double[]{1.0, 0.0, 1.0});
        inputValuesList.add(new double[]{1.0, 1.0, 1.0});

        double error = 0.0;
        for (double[] inputValues : inputValuesList) {
            // outputValue is the expected result
            // If {0, 0} or {1, 1}, outputValue = 0
            // If {0, 1} or {1, 0}, outputValue = 1
            double outputValue = 0;
            if(inputValues[0] == 0 && inputValues[1] == 1){
                outputValue = 1;
            }
            if(inputValues[1] == 0 && inputValues[0] == 1){
                outputValue = 1;
            }
            // Ex for {0, 1, 1} :
            // 0 will be loaded in inputNodes.get(0) (first input node)
            // 1 will be loaded in inputNodes.get(1) (second input node)
            // Third input node is a bias, its value is always 1.0
            int index = 0;
            for(double value : inputValues){
                // Load value into node input
                brain.loadInput(value, index);
                ++ index;
            }
            // Once values are loaded, run the network.
            brain.runNetwork();

            // Update error :
            error += Math.abs(outputValue - brain.getOutput(brain.getOutputNodeID()));
        }
        // Fitness max - errors
        brain.fitness = fitnessMax - error;
    }

    private static void updateSpeciesMembersList(){
        for(Specie existingSpecie : species){
            List<Brain> speciesBrain = BrainsHelper.getSameSpeciesBrain(existingSpecie.specieID, generationMembers);
            existingSpecie.members = new ArrayList<>();
            existingSpecie.members.addAll(speciesBrain);
        }
    }

    private static void fillWithOffsprings(int actualGeneration){
        // this value counts how many brains have been added to next gen.
        int addedToNextGenCounter = 0;

        // Loop on every existing species
        for(Specie existingSpecie: species){
            if(!existingSpecie.members.isEmpty()){
                // select parents
                List<Brain> parents = existingSpecie.selectParentsForNextGen(tournamentSize, existingSpecie.members);

                // create offsprings and add them to the generation
                for(int i =0; i < existingSpecie.offspring; i++){
                    //create a new empty Brain
                    NeatParameters neatParameters = new NeatParameters(popSize, percentageConn);
                    Brain offspring = new Brain(neatParameters, brainIDsCounter);

                    // Clone everything from the fittest parent
                    offspring.copyFrom(BrainsHelper.getFittestBrain(parents.get(0), parents.get(1)));

                    // Till now, the offspring is exactly the same as its fittest parent
                    // We will now cross the connections.
                    offspring.neatParameters.connections = ConnectionsHelper.getMatchingConnectionsWithRandomlyPickedWeights(parents.get(0), parents.get(1));

                    // Finally, get a new id for the offspring
                    // Since I know how many brains I added yet, I can increment this value by one.
                    offspring.brainID = addedToNextGenCounter + 1;

                    // Reinitialize parameters that will be updated when it plays
                    offspring.fitness = 0.0;
                    offspring.adjustedFitness = 0.0;

                    // Now the offspring should have everything it needs.
                    // Add the offspring to the next generation population
                    generationMembers.add(offspring);

                    // And increment counter
                    addedToNextGenCounter++;
                }
            }
        }
//        for(Brain brain : generationMembers){
//            System.out.println(brain);
//        }
    }

    private static void resetGenerationMembers(){
        temporaryGenerationMembers = generationMembers;
        generationMembers = new ArrayList<>();
    }

    private static void DisplayGenerationInformation(int actualGeneration){
        System.out.println("____________________ GENERATION " + actualGeneration + " ____________________");
        for (Specie specie : species) {
            if(!specie.members.isEmpty()){
                // Print all the information about the specie
                System.out.println(specie);
            }
        }
    }

    private static void DisplayBestBrain(int actualGeneration){
        if (bestBrain != null) {
            System.out.println("In generation " + actualGeneration + ", " + "the best brain is brain " + bestBrain.brainID + " from specie " + bestBrain.speciesID);
            System.out.println("It has a fitness = " + bestBrain.fitness + ", and an adjusted fitness = " + bestBrain.adjustedFitness);
            System.out.println("_______________________________________________________");
        }
    }
}