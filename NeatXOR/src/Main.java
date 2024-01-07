import classes.neuralNetworks.Brain;
import classes.NeatParameters;
import classes.neuralNetworks.Specie;
import classes.nodes.Connection;
import helpers.BrainsHelper;
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

        //for each generation
        for (int actualGeneration = 1; actualGeneration <= generationsNumber; actualGeneration++) {
            generationMembers = new ArrayList<>();
            brainIDsCounter = 1;
            // 1. GENERATION PLAYS

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
                // add brain to the generation's population
                generationMembers.add(brain);
            }

            // 2. COLLECT INFO ABOUT GENERATION AND UPDATE PARAMETERS

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
            // Display information about the generation that just played
//            DisplayGenerationInformation();
            // Update best brain in generation and display it
            bestBrain = BrainsHelper.updateBestBrain(bestBrain, generationMembers, bestAdjustedFitnessInPopulation);
            bestBrainsFromEachGeneration.add(bestBrain);
            // Draw best brain and display information about the generation
//            DrawBestBrain();

            // 3. CREATE THE NEXT GENERATION

            // Reset generation members and keep them in a temporary variable
            resetGenerationMembers();
            // For each existing specie, add offsprings to the next generation
            fillGenerationMembersWithOffsprings();

//            for(Brain brain : generationMembers){
//                System.out.println("Brain " + brain.brainID + " from specie " + brain.speciesID + " has an adjusted fitness of " + brain.adjustedFitness);
//            }
            // Look if generation is complete
            // Otherwise, fill it with the best creatures from a specie
            fillGenerationPopulation();
            //

        }
    }

    private static void fillGenerationPopulation(){
        // Because of the rounded numbers management of Java
        // I sometimes don't have enough brains in my population
        // Fill with random brains from the last generation
        List<Brain> emptyBrains = BrainsHelper.getBrainsWithoutSpecies(generationMembers);
        // counter will be used to find the index of the brains to replace
        int counter = popSize - emptyBrains.size();

        // While there are specieless brains
        while(!emptyBrains.isEmpty()){
            Random rand = new Random();
            int randomSpecieID = 0;
            // Pick a valid specieID
            while(randomSpecieID == 0){
                randomSpecieID = rand.nextInt(SpeciesHelper.getDifferentSpeciesCount(temporaryGenerationMembers));
            }
            // Get the fittest brain of the specie
            List<Brain> specieBrains = BrainsHelper.getSameSpeciesBrain(randomSpecieID, temporaryGenerationMembers);
            // Add it to the generation
            Brain brainToAdd = BrainsHelper.getFittestInList(specieBrains);
            generationMembers.set(counter, brainToAdd);

            // Update list of empty brains and counter
            emptyBrains = BrainsHelper.getBrainsWithoutSpecies(generationMembers);
            ++counter;
        }
    }

    private static void fillGenerationMembersWithOffsprings(){
        int newGenCounter = 0;
        for(Specie existingSpecie : species){
            // Repeat for the allowed number of offsprings for this specie
            for(int i = 0; i < existingSpecie.offspring; i++){
                // Select parents for this specie
                // Note : tournament winners can be the same brain multiple times => parents might have the same brainID !
                List<Brain> parents = existingSpecie.selectParentsForNextGen(tournamentSize, existingSpecie.members);

                // Clone the fittest parent
                // Fitness, adjusted fitness, specieID and nodes will be copied from this brain to the offspring
                Brain fittestParent = BrainsHelper.getFittestBrain(parents.get(0), parents.get(1));
                generationMembers.get(newGenCounter).fitness = fittestParent.fitness;
                generationMembers.get(newGenCounter).adjustedFitness = fittestParent.adjustedFitness;
                generationMembers.get(newGenCounter).neatParameters.inputNodes = fittestParent.neatParameters.inputNodes;
                generationMembers.get(newGenCounter).neatParameters.hiddenNodes = fittestParent.neatParameters.hiddenNodes;
                generationMembers.get(newGenCounter).neatParameters.outputNodes = fittestParent.neatParameters.outputNodes;
                generationMembers.get(newGenCounter).speciesID = fittestParent.speciesID;

                // Get the matching connections of the 2 parents.
                // connection's weight is randomly picked between the weights of the parents ones.
                List<Connection> matchingConnections = ConnectionsHelper.getMatchingConnectionsWithRandomlyPickedWeights(parents.get(0), parents.get(1));
                generationMembers.get(newGenCounter).neatParameters.connections = matchingConnections;

                // increment counter
                ++newGenCounter;
            }
        }
    }

    private static void resetGenerationMembers(){
        temporaryGenerationMembers = generationMembers;
        generationMembers = new ArrayList<>();
        brainIDsCounter = 1;

        // Add popSize brand-new brains to generationMembers List
        for(int i = 0; i < popSize; i++){
            NeatParameters neatParameters = new NeatParameters(popSize, inputNodesNumber, outputNodesNumber, hiddenNodesNumber, percentageConn);
            Brain brain = new Brain(neatParameters, brainIDsCounter);
            brain.initializeWithEmptyNodeAndConnectionLists();
            generationMembers.add(brain);
        }
    }

    private static void DisplayGenerationInformation(){
        System.out.println("____________________ GENERATION " + generationsNumber + " ____________________");
        for (Specie specie : species) {
            // Print all the information about the specie
            System.out.println(specie.toString());
        }
    }

    private static void DrawBestBrain(){
        if (bestBrain != null) {
            System.out.println("In generation " + generationsNumber + ", " + "the best brain is brain " + bestBrain.brainID + " from specie " + bestBrain.speciesID);
            System.out.println("It has a fitness = " + bestBrain.fitness + ", and an adjusted fitness = " + bestBrain.adjustedFitness);
            System.out.println("__________________________________________________");
            //Show network topology
            bestBrain.drawNetwork();
        }
    }
}