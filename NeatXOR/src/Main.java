import classes.Brain;
import classes.NeatParameters;
import classes.nodes.Connection;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    //general parameters
    public static int generationsNumber = 1;
    public static int brainIDsCounter = 1;
    public static int popSize = 50;
    public static int inputNodesNumber = 3;
    public static int outputNodesNumber = 1;
    public static int hiddenNodesNumber = 0;
    public static double percentageConn = 1.0;

    //used during speciation
    public static double c1 = 1.0, c2 = 1.0, c3 = 0.4;
    public static double speciationThreshold = 0.1;

    //used to keep data through generations
    public static double bestFitnessInPopulation = 0;
    public static Brain bestBrain = null;
    public static List<Brain> generation;

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
                if (brain.fitness > bestFitnessInPopulation) {
                    bestBrain = new Brain(neatParameters, -1);
                    bestBrain.copyFrom(brain);
                    bestFitnessInPopulation = brain.fitness;
                }

                generation.add(brain);
            }
            // build the next generation
            // First use Speciation to give a speciesID to each brain in the generation global static variable.
            setSpeciesIDs();
            for (Brain generationBrain : generation) {
                System.out.println("Brain " + generationBrain.brainID + " in generation " + generationsNumber + " has a species ID = " + generationBrain.speciesID);
            }
        }

        // display best brain
        if (bestBrain != null) {
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
                System.out.println("CD : " + cd);
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
}