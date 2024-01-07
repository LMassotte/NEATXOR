package helpers;

import classes.neuralNetworks.Brain;
import classes.neuralNetworks.Specie;
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
        //global average adjusted fitness
        double globalMean = getGlobalAverageAdjustedFitness(generationMembers);

        // loop on the species so each will have its average adjusted fitness
        for(Specie specie : species){
            List<Brain> specieMembers = BrainsHelper.getSameSpeciesBrain(specie.specieID, generationMembers);
            double specieMean = getAverageAdjustedFitnessBySpecie(specie.specieID, generationMembers);
            // Update the offspring value of the specie
            species.stream().filter(item -> item.specieID == specie.specieID).toList().get(0).offspring = (int)(specieMean / globalMean * specieMembers.size());
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
