package helpers;

import classes.neuralNetworks.Brain;
import classes.neuralNetworks.Specie;

import java.util.ArrayList;
import java.util.List;

public class SpeciesHelper {
    public static List<Specie> getSpeciesWithID(int specieID, List<Specie> species){
        return species.stream().filter(specie -> specie.specieID == specieID).toList();
    }

    public static void setSpeciesIDs(int generationsNumber, List<Brain> generationMembers, double c1, double c2, double c3, double speciationThreshold) {
        // For the first generation, the leaders of each specie are picked randomly out of the population that hasn't a specieID yet.
        // From Gen 2 onwards, leaders of species are picked out of the population that already has the specieID !
        // Gen 1
        if(generationsNumber == 1){
            setSpeciesIDsForBrainsWithoutSpecie(generationMembers, c1, c2, c3, speciationThreshold);
        }
        // Gen 2 -> Max Gen
        else{
            // Get a leader of each existing specie
            // Note to myself : the brains picked will always have a brainID bcs they're taken out of this generation population.

            List<Brain> leadersList = getLeadersList(generationMembers);
            // Reset specieID for each non-leader brain
            resetSpecieIDForNonLeaders(leadersList, generationMembers);
            // For each leader, checks which of the specieless brains could join its specie.
            // Compute the compatibility difference and if it's below threshold, assign leader's specie ID.
            for(Brain leaderBrain : leadersList){
                List<Brain> brainsWithoutSpecies = BrainsHelper.getBrainsWithoutSpecies(generationMembers);
                for (Brain brain : brainsWithoutSpecies) {
                    double cd = ConnectionsHelper.getCompatibilityDifference(leaderBrain, brain, c1, c2, c3);
                    if (cd < speciationThreshold) {
                        generationMembers.get(brain.brainID - 1).speciesID = leaderBrain.brainID;
                    }
                }
            }
            // Give a new specie to the brains who couldn't fit in any existing specie
            setSpeciesIDsForBrainsWithoutSpecie(generationMembers, c1, c2, c3, speciationThreshold);
        }
    }

    public static void updateSpecies(List<Specie> species, List<Brain> generationMembers){
        // Update the species list. For existing species : update members and offsprings, recompute average fitness, increment gensSinceImproved if needed.
        // For new species : Add a new Specie to the list.
        for(int i = 0; i < getDifferentSpeciesCount(generationMembers); i++){
            // If a specie with ID = "i + 1" is found, update it
            if(!SpeciesHelper.getSpeciesWithID(i + 1, species).isEmpty()){
                if(species.get(i) != null){
                    // Note : when using get, the first of the list is obtained with "i" = 0. When "i" refers to the specieID, it has to be incremented by one
                    species.get(i).members = BrainsHelper.getSameSpeciesBrain(i + 1, generationMembers);
                    species.get(i).offspring = 0;
                    species.get(i).computeAverageFitness();
                    double lastAverageAdjustedFitness = species.get(i).averageAdjusetdFitness;
                    species.get(i).computeAverageFitness();
                    species.get(i).hasImproved(lastAverageAdjustedFitness);
                }
                else{
                    // Note : when using get, the first of the list is obtained with "i" = 0. When "i" refers to the specieID, it has to be incremented by one
                    // Offspring will be updated in the next step
                    // TODO :  USE THE FITNESS SUM (0 by default)
                    species.add(new Specie(i + 1, BrainsHelper.getSameSpeciesBrain(i + 1, generationMembers), 0, 0));
                }
            }
            // Else, create it
            else{
                // Note : when using get, the first of the list is obtained with "i" = 0. When "i" refers to the specieID, it has to be incremented by one
                // Offspring will be updated in the next step
                // TODO :  USE THE FITNESS SUM (0 by default)
                species.add(new Specie(i + 1, BrainsHelper.getSameSpeciesBrain(i + 1, generationMembers), 0, 0));
            }
        }
    }

    public static int getDifferentSpeciesCount(List<Brain> generationMembers){
        int highestSpecieID = 0;
        for(Brain brain : generationMembers){
            if(brain.speciesID > highestSpecieID){
                highestSpecieID = brain.speciesID;
            }
        }

        // The highest specie ID is always the amount of different species
        return highestSpecieID;
    }



    public static List<Brain> getLeadersList(List<Brain> generationMembers){
        // Return a leader of each existing specie in the generation.
        List<Brain> leadersList = new ArrayList<>();
        int speciesCounter = SpeciesHelper.getDifferentSpeciesCount(generationMembers);
        for(int i = 1; i <= speciesCounter; i++){
            List<Brain> brainsOfSameSpecie = BrainsHelper.getSameSpeciesBrain(i, generationMembers);
            leadersList.add(BrainsHelper.selectRandomBrain(brainsOfSameSpecie));
//            if (!brainsOfSameSpecie.isEmpty()) {
//                leadersList.add(BrainsHelper.selectRandomBrain(brainsOfSameSpecie));
//            }
        }
        return leadersList;
    }

    public static void resetSpecieIDForNonLeaders(List<Brain> leadersList, List<Brain> generationMembers){
        for(Brain generationalBrain : generationMembers){
            boolean isLeader = false;
            for(Brain leaderBrain : leadersList){
                if (generationalBrain.brainID == leaderBrain.brainID) {
                    isLeader = true;
                    break;
                }
            }

            if(!isLeader){
                generationalBrain.speciesID = -1;
            }
        }
    }

    public static void setSpeciesIDsForBrainsWithoutSpecie(List<Brain> generationMembers, double c1, double c2, double c3, double speciationThreshold){
        List<Brain> brainsWithoutSpecies = BrainsHelper.getBrainsWithoutSpecies(generationMembers);
        // species counter is used to assign new species IDs. For gen 1, it will be one.
        // After gen 1, it counts how many species already exist and increments that number by one.
        int speciesCount = getDifferentSpeciesCount(generationMembers) + 1;

        while (!brainsWithoutSpecies.isEmpty()) {
            // get a random brain out of the generation and give it a speciesID
            Brain brainLeader = BrainsHelper.selectRandomBrain(brainsWithoutSpecies);
            brainLeader.speciesID = speciesCount;

            // compare every other non-assigned brain with it. If CD > threshold, give it the same speciesID.
            for (Brain brain : brainsWithoutSpecies) {
                double cd = ConnectionsHelper.getCompatibilityDifference(brainLeader, brain, c1, c2, c3);
                if (cd < speciationThreshold) {
                    generationMembers.get(brain.brainID - 1).speciesID = speciesCount;
                }
            }

            // Update the list of brains without speciesID
            brainsWithoutSpecies = BrainsHelper.getBrainsWithoutSpecies(generationMembers);
            ++speciesCount;
        }
    }


}
