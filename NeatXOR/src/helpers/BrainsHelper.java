package helpers;

import classes.neuralNetworks.Brain;

import java.util.List;
import java.util.Random;

public class BrainsHelper {

    public static void mutateBrains(List<Brain> brains){
        for(Brain brain : brains){
            // 80% chance that its connections weights are modified
            brain.mutateWeights();
            // 5% chance of having a new connection
            // This can also enable a disabled connection under specific conditions
            brain.mutateNewConnection();
            // 5% chances of having a new node
            brain.mutateNewNode();
        }
    }
    public static Brain selectRandomBrain(List<Brain> brains) {
        Random rand = new Random();
        if(!brains.isEmpty()){
            int brainPosition = rand.nextInt(brains.size());
            return brains.get(brainPosition);
        }
        else{
            return null;
        }
    }

    public static List<Brain> getSameSpeciesBrain(int specieID, List<Brain> generationMembers){
        return generationMembers.stream().filter(brain -> brain.speciesID == specieID).toList();
    }

    public static List<Brain> getBrainsWithoutSpecies(List<Brain> brains) {
        return brains.stream().filter(brain -> brain.speciesID == -1).toList();
    }

    public static Brain updateBestBrain(Brain bestBrain, List<Brain> generationMembers) {
        // find and display best brain in generation
        for (Brain generationBrain : generationMembers) {
            if (generationBrain.adjustedFitness > bestBrain.adjustedFitness) {
                bestBrain = new Brain(generationBrain.neatParameters, -1);
                bestBrain.copyFrom(generationBrain);
            }
        }
        return bestBrain;
    }

    public static Brain getFittestBrain(Brain brain1, Brain brain2){
        return brain1.adjustedFitness >= brain2.adjustedFitness ? brain1 : brain2;
    }

    public static Brain getFittestInList(List<Brain> brains){
        Brain fittestBrain = brains.get(0);
        for(Brain brain : brains){
            fittestBrain = brain.fitness > fittestBrain.fitness ? brain : fittestBrain;
        }
        return fittestBrain;
    }
}
