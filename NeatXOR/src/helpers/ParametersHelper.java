package helpers;

import classes.neuralNetworks.Brain;

import java.util.ArrayList;
import java.util.List;

public class ParametersHelper {
    public static double getGlobalAverageAdjustedFitness(List<Brain> generationMembers){
        double sum = 0;
        for(Brain brain : generationMembers){
            sum += brain.adjustedFitness;
        }

        return (sum / generationMembers.size());
    }

    public static double getAverageAdjustedFitnessBySpecie(int specieID, List<Brain> generationMembers){
        double sum = 0;
        List<Brain> sameSpecieBrains = BrainsHelper.getSameSpeciesBrain(specieID, generationMembers);
        long sameSpecieAmount = sameSpecieBrains.size();
        for(Brain brain : sameSpecieBrains){
            sum += brain.adjustedFitness;
        }

        return (sum / sameSpecieAmount);
    }

    public static List<Integer> computeOffsprings(List<Brain> generationMembers){
        List<Integer> offsprings = new ArrayList<>();

        //global average adjusted fitness
        double globalMean = getGlobalAverageAdjustedFitness(generationMembers);

        // how many different species ?
        int differentSpeciesCounter = SpeciesHelper.getDifferentSpeciesCount(generationMembers);
        // loop on this number so each specie will have its average adjusted fitness
        for(int i = 1; i <= differentSpeciesCounter; i++){
            List<Brain> specieMembers = BrainsHelper.getSameSpeciesBrain(i, generationMembers);
            double specieMean = getAverageAdjustedFitnessBySpecie(i, generationMembers);

            offsprings.add((int)(specieMean / globalMean * specieMembers.size()));
        }
        return offsprings;
    }

    public static double adjustThreshold(List<Brain> generationMembers, double speciationThreshold, int targetSpeciesAmount, double stepSizeForThreshold){
        long counter = SpeciesHelper.getDifferentSpeciesCount(generationMembers);
        return counter > targetSpeciesAmount ? speciationThreshold + stepSizeForThreshold : speciationThreshold - stepSizeForThreshold;
    }

    public static void adjustFitness(List<Brain> generationMembers){
        for (Brain brain : generationMembers){
            List<Brain> sameSpecieBrains = BrainsHelper.getSameSpeciesBrain(brain.speciesID, generationMembers);
            long sameSpecieAmount = sameSpecieBrains.size();
            brain.adjustedFitness = brain.fitness / sameSpecieAmount;
        }
    }
}
