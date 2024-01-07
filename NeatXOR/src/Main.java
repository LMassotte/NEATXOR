import classes.Brain;
import classes.NeatParameters;
import classes.nodes.Connection;

import java.util.*;
import java.util.stream.Collectors;

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
    public static double stepSizeForThreshold = 0.1;
    public static double speciationThreshold = 1.0;

    //used to keep data through generations
    public static double bestAdjustedFitnessInPopulation = 0;
    public static Brain bestBrain = null;
    public static List<Brain> generation;

    //used for next generation building (the offsprings are in the order of the species)
    public static List<Integer> offsprings;

    public static void main(String[] args) {

        //possible values for xor
        List<double[]> inputValuesList = new ArrayList<>();
        inputValuesList.add(new double[]{0.0, 0.0, 1.0});
        inputValuesList.add(new double[]{0.0, 1.0, 1.0});
        inputValuesList.add(new double[]{1.0, 0.0, 1.0});
        inputValuesList.add(new double[]{1.0, 1.0, 1.0});

        //for each generation
        for (int actualGeneration = 1; actualGeneration <= generationsNumber; actualGeneration++) {
            generation = new ArrayList<>();
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
                    brain.fitness += brain.getOutput(brain.outputNodeID);
                }
                // keep the best brain in a variable


                generation.add(brain);
            }
            // build the next generation
            // First use Speciation to give a speciesID to each brain in the generation global static variable.
            setSpeciesIDs();
            // Divide the fitness by the amount of brains having the speciesID
            adjustFitness();
            // Compute the offsprings (amount of members from each specie in the next generation)
            computeOffsprings();
            // Now we can adjust the speciation threshold according to the amount of species we have in this generation
            adjustThreshold();
            System.out.println("____________________ GENERATION " + generationsNumber + " ____________________");
            for(int i = 0; i < offsprings.size(); i++){
                System.out.println("The next generation will have " + offsprings.get(i) + " members of specie " + (i + 1));
            }
        }

        // find and display best brain in generation
        for (Brain generationBrain : generation) {
            if (generationBrain.adjustedFitness > bestAdjustedFitnessInPopulation) {
                bestBrain = new Brain(generationBrain.neatParameters, -1);
                bestBrain.copyFrom(generationBrain);
                bestAdjustedFitnessInPopulation = generationBrain.adjustedFitness;
            }
        }
        if (bestBrain != null) {
            System.out.println("Best brain is brain " + bestBrain.brainID + " in the (last) generation " + generationsNumber);
            System.out.println("It has a fitness = " + bestBrain.fitness + ", and an adjusted fitness = " + bestBrain.adjustedFitness);
            bestBrain.drawNetwork();
        }
    }

    private static Set<Integer> getInnovationIDSet(List<Connection> connections) {
        Set<Integer> innovationIDSet = new HashSet<>();
        for (Connection connection : connections) {
            innovationIDSet.add(connection.innovationID);
        }
        return innovationIDSet;
    }

    private static double getCompatibilityDifference(Brain brainLeader, Brain brainCompared) {
        // Get everything needed to compute compatibility difference
        double excessConnections = getExcessConnections(brainLeader, brainCompared);
        double disjointConnections = getDisjointConnections(brainLeader, brainCompared);
        double weightDifference = getWeightDifference(brainLeader, brainCompared);
        double highestConnectionsAmount = getHighestConnectionsAmount(brainLeader, brainCompared);

        double compatibilityDifference = (c1 * (excessConnections / highestConnectionsAmount)) + (c2 * (disjointConnections / highestConnectionsAmount)) + (c3 * (weightDifference / highestConnectionsAmount));

        return compatibilityDifference;
    }

    private static double getHighestConnectionsAmount(Brain brainLeader, Brain brainCompared) {
        return Math.max(brainLeader.neatParameters.connections.size(), brainCompared.neatParameters.connections.size());
    }

    private static double getWeightDifference(Brain brainLeader, Brain brainCompared) {
        double meanWeightDifference;
        List<Connection> leaderConnections = brainLeader.neatParameters.connections;
        List<Connection> comparedConnections = brainCompared.neatParameters.connections;

        // Get sets with every innovationIDs in connections lists
        Set<Integer> innovationIDSetLeader = getInnovationIDSet(leaderConnections);
        Set<Integer> innovationIDSetCompared = getInnovationIDSet(comparedConnections);

        // These will be used to store the connections with innovationIDs that can be found in both lists
        List<Connection> commonListLeader = new ArrayList<>();
        List<Connection> commonListCompared = new ArrayList<>();

        // Fill the common lists
        for (Connection connection : leaderConnections) {
            if (innovationIDSetCompared.contains(connection.innovationID)) {
                commonListLeader.add(connection);
            }
        }
        for (Connection connection : comparedConnections) {
            if (innovationIDSetLeader.contains(connection.innovationID)) {
                commonListCompared.add(connection);
            }
        }
        // Sort the lists to have the connections with same InnovationID at the same place
        commonListLeader.sort(Comparator.comparingInt(connection -> connection.innovationID));
        commonListCompared.sort(Comparator.comparingInt(connection -> connection.innovationID));

        // Once it's done, calculate the total weight difference.
        // Note : both lists will always have the same size
        double size = commonListLeader.size();
        double weightDifferencesSum = getWeightDifferencesSum(size, commonListLeader, commonListCompared);

        // Compute mean
        meanWeightDifference = weightDifferencesSum / size;
        // Take absolute value of the mean.
        meanWeightDifference = Math.abs(meanWeightDifference);

        return meanWeightDifference;
    }

    private static double getWeightDifferencesSum(double size, List<Connection> commonListLeader, List<Connection> commonListCompared) {
        double weightDifferencesSum = 0;
        // Compare the difference of weight for connections having the same Innovation ID.
        for (int i = 0; i < size; i++) {
            double difference;
            double leaderWeight = commonListLeader.get(i).weight;
            double comparedWeight = commonListCompared.get(i).weight;
            if (leaderWeight > comparedWeight) {
                difference = leaderWeight - comparedWeight;
            } else {
                difference = comparedWeight - leaderWeight;
            }
            weightDifferencesSum += difference;
        }
        return weightDifferencesSum;
    }

    private static double getDisjointConnections(Brain brainLeader, Brain brainCompared) {
        double result = 0;
        List<Connection> leaderConnections = brainLeader.neatParameters.connections;
        List<Connection> comparedConnections = brainCompared.neatParameters.connections;
        // get the highest innovation ID in the list that has the smallest highest one.
        int highestInnovationIDInSmallestList = Math.min(getHighestInnovationID(leaderConnections), getHighestInnovationID(comparedConnections));

        // Now, scan through both lists to get the amount of connections with Innovation IDs that are in only one of the two lists
        // First we check how many connections are in the leader list and not in the compared one.
        // get a list of all compared innovation IDs
        Set<Integer> innovationIDsSet = getInnovationIDSet(comparedConnections);
        for (Connection leaderConnection : leaderConnections) {
            if (leaderConnection.innovationID < highestInnovationIDInSmallestList) {
                if (!innovationIDsSet.contains(leaderConnection.innovationID)) {
                    ++result;
                }
            }
        }
        // Then we do the opposite
        innovationIDsSet = getInnovationIDSet(leaderConnections);
        for (Connection comparedConnection : comparedConnections) {
            if (comparedConnection.innovationID < highestInnovationIDInSmallestList) {
                if (!innovationIDsSet.contains(comparedConnection.innovationID)) {
                    ++result;
                }
            }
        }
        // After that we can return the result
        return result;
    }

    private static double getExcessConnections(Brain brainLeader, Brain brainCompared) {
        double result = 0;
        int highestInnovationIDLeader = getHighestInnovationID(brainLeader.neatParameters.connections);
        int highestInnovationIDCompared = getHighestInnovationID(brainCompared.neatParameters.connections);
        // If leader has higher innovationIDs than compared brain, count how many.
        if (highestInnovationIDLeader > highestInnovationIDCompared) {
            for (Connection leaderConnection : brainLeader.neatParameters.connections) {
                if (leaderConnection.innovationID > highestInnovationIDCompared) {
                    ++result;
                }
            }
        }
        // If compared brain has higher innovationIDs than leader brain, count how many.
        else if (highestInnovationIDCompared > highestInnovationIDLeader) {
            for (Connection comparedConnection : brainCompared.neatParameters.connections) {
                if (comparedConnection.innovationID > highestInnovationIDLeader) {
                    ++result;
                }
            }
        }
        // Note : if they have the same highest innovation ID, result will be 0 which is correct
        return result;
    }

    private static int getHighestInnovationID(List<Connection> connections) {
        int highestInnovationID = 0;
        for (Connection connection : connections) {
            if (connection.innovationID > highestInnovationID) {
                highestInnovationID = connection.innovationID;
            }
        }
        return highestInnovationID;
    }

    public static void setSpeciesIDs() {
        List<Brain> brainsWithoutSpecies = getBrainsWithoutSpecies(generation);
        int speciesCount = 1;

        while (!brainsWithoutSpecies.isEmpty()) {
            // get a random brain out of the generation and give it a speciesID
            Brain brainLeader = selectRandom(brainsWithoutSpecies);
            brainLeader.speciesID = speciesCount;

            // compare every other non-assigned brain with it. If CD > threshold, give it the same speciesID.
            for (Brain brain : brainsWithoutSpecies) {
                double cd = getCompatibilityDifference(brainLeader, brain);
                if (cd < speciationThreshold) {
                    generation.get(brain.brainID - 1).speciesID = speciesCount;
                }
            }

            // Update the list of brains without speciesID
            brainsWithoutSpecies = getBrainsWithoutSpecies(generation);
            ++speciesCount;
        }
    }

    private static Brain selectRandom(List<Brain> brains) {
        Random rand = new Random();
        int brainPosition = rand.nextInt(brains.size());
        return brains.get(brainPosition);
    }

    private static List<Brain> getBrainsWithoutSpecies(List<Brain> brains) {
        return brains.stream().filter(brain -> brain.speciesID == -1).collect(Collectors.toList());
    }

    private static void adjustFitness(){
        for (Brain brain : generation){
            List<Brain> sameSpecieBrains = getSameSpeciesBrain(brain.speciesID);
            long sameSpecieAmount = sameSpecieBrains.size();
            brain.adjustedFitness = brain.fitness / sameSpecieAmount;
        }
    }
    private static List<Brain> getSameSpeciesBrain(int specieID){
        return generation.stream().filter(obj -> obj.speciesID == specieID).toList();
    }

    private static int getDifferentSpeciesCount(){
        int highestSpecieID = 0;
        for(Brain brain : generation){
            if(brain.speciesID > highestSpecieID){
                highestSpecieID = brain.speciesID;
            }
        }

        // The highest specie ID is always the amount of different species
        return highestSpecieID;
    }

    private static double getGlobalAverageAdjustedFitness(){
        double sum = 0;
        for(Brain brain : generation){
            sum += brain.adjustedFitness;
        }

        return (sum / generation.size());
    }

    private static double getAverageAdjustedFitnessBySpecie(int specieID){
        double sum = 0;
        List<Brain> sameSpecieBrains = getSameSpeciesBrain(specieID);
        long sameSpecieAmount = sameSpecieBrains.size();
        for(Brain brain : sameSpecieBrains){
            sum += brain.adjustedFitness;
        }

        return (sum / sameSpecieAmount);
    }

    private static void computeOffsprings(){
        offsprings = new ArrayList<>();

        //global average adjusted fitness
        double globalMean = getGlobalAverageAdjustedFitness();

        // how many different species ?
        int differentSpeciesCounter = getDifferentSpeciesCount();
        // loop on this number so each specie will have its average adjusted fitness
        for(int i = 1; i <= differentSpeciesCounter; i++){
            List<Brain> specieMembers = getSameSpeciesBrain(i);
            double specieMean = getAverageAdjustedFitnessBySpecie(i);

            offsprings.add((int)(specieMean / globalMean * specieMembers.size()));
        }
    }

    private static void adjustThreshold(){
        long counter = getDifferentSpeciesCount();
        speciationThreshold = counter > targetSpeciesAmount ? speciationThreshold + stepSizeForThreshold : speciationThreshold - stepSizeForThreshold;
    }
}