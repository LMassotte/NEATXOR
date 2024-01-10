package helpers;

import classes.neuralNetworks.Brain;
import classes.nodes.Connection;

import java.util.*;

public class ConnectionsHelper {
    public static Connection getRandomConnectionInList(List<Connection> connections){
        Random rand = new Random();
        if(!connections.isEmpty()){
            int connectionPosition = rand.nextInt(connections.size());
            return connections.get(connectionPosition);
        }
        else{
            return null;
        }
    }
    public static Set<Integer> getInnovationIDSet(List<Connection> connections) {
        Set<Integer> innovationIDSet = new HashSet<>();
        for (Connection connection : connections.stream().filter(co -> co.isEnabled).toList()) {
            innovationIDSet.add(connection.innovationID);
        }
        return innovationIDSet;
    }

    public static int getHighestInnovationID(List<Connection> connections) {
        int highestInnovationID = 0;
        for (Connection connection : connections.stream().filter(co -> co.isEnabled).toList()) {
            if (connection.innovationID > highestInnovationID) {
                highestInnovationID = connection.innovationID;
            }
        }
        return highestInnovationID;
    }

    public static double getCompatibilityDifference(Brain brainLeader, Brain brainCompared, double c1, double c2, double c3) {
        // Get everything needed to compute compatibility difference
        double excessConnections = getExcessConnections(brainLeader, brainCompared);
        double disjointConnections = getDisjointConnections(brainLeader, brainCompared);
        double weightDifference = getWeightDifference(brainLeader, brainCompared);
        double highestConnectionsAmount = getHighestConnectionsAmount(brainLeader, brainCompared);

        return (c1 * (excessConnections / highestConnectionsAmount))
                + (c2 * (disjointConnections / highestConnectionsAmount))
                + (c3 * (weightDifference / highestConnectionsAmount));
    }

    public static double getHighestConnectionsAmount(Brain brainLeader, Brain brainCompared) {
        return Math.max(brainLeader.neatParameters.connections.size(), brainCompared.neatParameters.connections.size());
    }

    public static List<Connection> getMatchingConnectionsWithRandomlyPickedWeights(Brain brain1, Brain brain2){
        // Get all the matching connections
        // Weight is the only difference between the 2 lists
        // Pick randomly the weight between brain1 and brain2

        List<Connection> matchingConnectionsRandomWeights = new ArrayList<>();
        List<Connection> matchingConnectionsBrain1Weights = getMatchingConnectionsWithBrain1Weights(brain1, brain2);
        List<Connection> matchingConnectionsBrain2Weights = getMatchingConnectionsWithBrain2Weights(brain1, brain2);
        int index = 0;
        for(Connection connection : matchingConnectionsBrain1Weights){
            Random random = new Random();
            int randomInteger = random.nextInt(2);
            if(randomInteger == 0){
                matchingConnectionsRandomWeights.add(connection);
            }
            else{
                matchingConnectionsRandomWeights.add(matchingConnectionsBrain2Weights.get(index));
            }
            ++index;
        }

        return matchingConnectionsRandomWeights;
    }

    private static List<Connection> getMatchingConnectionsWithBrain1Weights(Brain brain1, Brain brain2){
        List<Connection> matchingConnections = new ArrayList<>();
        List<Connection> brain1Connections = brain1.neatParameters.connections;
        List<Connection> brain2Connections = brain2.neatParameters.connections;

        Set<Integer> innovationIDSetBrain2 = ConnectionsHelper.getInnovationIDSet(brain2Connections);

        for (Connection connection : brain1Connections.stream().filter(co -> co.isEnabled).toList()) {
            if (innovationIDSetBrain2.contains(connection.innovationID)) {
                matchingConnections.add(connection);
            }
        }

        return matchingConnections;
    }

    private static List<Connection> getMatchingConnectionsWithBrain2Weights(Brain brain1, Brain brain2){
        List<Connection> matchingConnections = new ArrayList<>();
        List<Connection> brain1Connections = brain1.neatParameters.connections;
        List<Connection> brain2Connections = brain2.neatParameters.connections;

        Set<Integer> innovationIDSetBrain1 = ConnectionsHelper.getInnovationIDSet(brain1Connections);

        for (Connection connection : brain2Connections.stream().filter(co -> co.isEnabled).toList()) {
            if (innovationIDSetBrain1.contains(connection.innovationID)) {
                matchingConnections.add(connection);
            }
        }

        return matchingConnections;
    }

    public static double getWeightDifference(Brain brainLeader, Brain brainCompared) {
        double meanWeightDifference;
        List<Connection> leaderConnections = brainLeader.neatParameters.connections;
        List<Connection> comparedConnections = brainCompared.neatParameters.connections;

        // Get sets with every innovationIDs in connections lists
        Set<Integer> innovationIDSetLeader = ConnectionsHelper.getInnovationIDSet(leaderConnections);
        Set<Integer> innovationIDSetCompared = ConnectionsHelper.getInnovationIDSet(comparedConnections);

        // These will be used to store the connections with innovationIDs that can be found in both lists
        List<Connection> commonListLeader = new ArrayList<>();
        List<Connection> commonListCompared = new ArrayList<>();

        // Fill the common lists
        for (Connection connection : leaderConnections.stream().filter(co -> co.isEnabled).toList()) {
            if (innovationIDSetCompared.contains(connection.innovationID)) {
                commonListLeader.add(connection);
            }
        }
        for (Connection connection : comparedConnections.stream().filter(co -> co.isEnabled).toList()) {
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

    public static double getWeightDifferencesSum(double size, List<Connection> commonListLeader, List<Connection> commonListCompared) {
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

    public static double getDisjointConnections(Brain brainLeader, Brain brainCompared) {
        double result = 0;
        List<Connection> leaderConnections = brainLeader.neatParameters.connections.stream().filter(co -> co.isEnabled).toList();
        List<Connection> comparedConnections = brainCompared.neatParameters.connections.stream().filter(co -> co.isEnabled).toList();
        // get the highest innovation ID in the list that has the smallest highest one.
        int highestInnovationIDInSmallestList = Math.min(getHighestInnovationID(leaderConnections), getHighestInnovationID(comparedConnections));

        // Now, scan through both lists to get the amount of connections with Innovation IDs that are in only one of the two lists
        // First we check how many connections are in the leader list and not in the compared one.
        // get a list of all compared innovation IDs
        Set<Integer> innovationIDsSet = ConnectionsHelper.getInnovationIDSet(comparedConnections);
        for (Connection leaderConnection : leaderConnections.stream().filter(co -> co.isEnabled).toList()) {
            if (leaderConnection.innovationID < highestInnovationIDInSmallestList) {
                if (!innovationIDsSet.contains(leaderConnection.innovationID)) {
                    ++result;
                }
            }
        }
        // Then we do the opposite
        innovationIDsSet = ConnectionsHelper.getInnovationIDSet(leaderConnections);
        for (Connection comparedConnection : comparedConnections.stream().filter(co -> co.isEnabled).toList()) {
            if (comparedConnection.innovationID < highestInnovationIDInSmallestList) {
                if (!innovationIDsSet.contains(comparedConnection.innovationID)) {
                    ++result;
                }
            }
        }
        // After that we can return the result
        return result;
    }

    public static double getExcessConnections(Brain brainLeader, Brain brainCompared) {
        double result = 0;
        int highestInnovationIDLeader = getHighestInnovationID(brainLeader.neatParameters.connections);
        int highestInnovationIDCompared = getHighestInnovationID(brainCompared.neatParameters.connections);
        // If leader has higher innovationIDs than compared brain, count how many.
        if (highestInnovationIDLeader > highestInnovationIDCompared) {
            for (Connection leaderConnection : brainLeader.neatParameters.connections.stream().filter(co -> co.isEnabled).toList()) {
                if (leaderConnection.innovationID > highestInnovationIDCompared) {
                    ++result;
                }
            }
        }
        // If compared brain has higher innovationIDs than leader brain, count how many.
        else if (highestInnovationIDCompared > highestInnovationIDLeader) {
            for (Connection comparedConnection : brainCompared.neatParameters.connections.stream().filter(co -> co.isEnabled).toList()) {
                if (comparedConnection.innovationID > highestInnovationIDLeader) {
                    ++result;
                }
            }
        }
        // Note : if they have the same highest innovation ID, result will be 0 which is correct
        return result;
    }
}
