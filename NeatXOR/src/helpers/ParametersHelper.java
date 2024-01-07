package helpers;

import classes.neuralNetworks.Brain;
import classes.neuralNetworks.Specie;

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

    public static void computeOffsprings(List<Brain> generationMembers, List<Specie> species){
        List<Integer> offsprings = new ArrayList<>();

        //global average adjusted fitness
        double globalMean = getGlobalAverageAdjustedFitness(generationMembers);

        // how many different species ?
        int differentSpeciesCounter = SpeciesHelper.getDifferentSpeciesCount(generationMembers);
        // loop on this number so each specie will have its average adjusted fitness
        for(int i = 1; i <= differentSpeciesCounter; i++){
            List<Brain> specieMembers = BrainsHelper.getSameSpeciesBrain(i, generationMembers);
            double specieMean = getAverageAdjustedFitnessBySpecie(i, generationMembers);
            // Update the offspring value of the specie
            int finalI = i;
            species.stream().filter(specie -> specie.specieID == finalI).toList().get(0).offspring = (int)(specieMean / globalMean * specieMembers.size());
        }
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

    public static void adjustOffsprings(int popSize, List<Specie> species){
        int total = 0;
        for(Specie specie : species){
            total += specie.offspring;
        }
        while(total < popSize){
            species.get(0).offspring += 1;
            total = ParametersHelper.getTotal(species);
        }
    }

    public static int getTotal(List<Specie> species){
        int total = 0;
        for(Specie specie : species){
            total += specie.offspring;
        }
        return total;
    }
}
