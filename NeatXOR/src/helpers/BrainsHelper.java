package helpers;

import classes.Brain;

import java.util.List;
import java.util.Random;

public class BrainsHelper {
    public static Brain selectRandomBrain(List<Brain> brains) {
        Random rand = new Random();
        int brainPosition = rand.nextInt(brains.size());
        return brains.get(brainPosition);
    }

    public static List<Brain> getSameSpeciesBrain(int specieID, List<Brain> generationMembers){
        return generationMembers.stream().filter(brain -> brain.speciesID == specieID).toList();
    }

    public static List<Brain> getBrainsWithoutSpecies(List<Brain> brains) {
        return brains.stream().filter(brain -> brain.speciesID == -1).toList();
    }
}
