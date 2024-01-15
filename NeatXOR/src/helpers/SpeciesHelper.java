package helpers;

import classes.neuralNetworks.Brain;
import classes.neuralNetworks.Specie;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class SpeciesHelper {
    public static List<Specie> getSpeciesWithID(int specieID, List<Specie> species) {
        return species.stream().filter(specie -> specie.specieID == specieID).toList();
    }

    public static void setSpeciesIDs(int actualGeneration, List<Brain> generationMembers, List<Specie> species, double c1, double c2, double c3, double speciationThreshold) {
        // For the first generation, the leaders of each specie are picked randomly out of the population that hasn't a specieID yet.
        // From Gen 2 onwards, leaders of species are picked out of the population that already has the specieID !
        // Gen 1
        if (actualGeneration == 1) {
            setSpeciesIDsForBrainsWithoutSpecie(generationMembers, c1, c2, c3, speciationThreshold);
        }
        // Gen 2 -> Max Gen
        else {
            // Get a leader of each existing specie
            // Note to myself : the brains picked will always have a brainID bcs they're taken out of this generation population.

            List<Brain> leadersList = getLeadersList(generationMembers, species);
            // Reset specieID for each non-leader brain
            resetSpecieIDForNonLeaders(leadersList, generationMembers);
            // For each leader, checks which of the specieless brains could join its specie.
            // Compute the compatibility difference and if it's below threshold, assign leader's specie ID.
            for (Brain leaderBrain : leadersList) {
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

    public static void addAndUpdateSpecies(List<Specie> species, List<Brain> generationMembers) {
        // verify for each brain if species already has a specie with its specieID.
        // if not, create it
        for (Brain brain : generationMembers) {
            boolean specieAlreadyExists = false;
            List<Brain> brainsFromSpecie = new ArrayList<>();
            for (Specie specie : species) {
                if (brain.speciesID == specie.specieID) {
                    specieAlreadyExists = true;
                    break;
                }
            }
            if (!specieAlreadyExists) {
                brainsFromSpecie = BrainsHelper.getSameSpeciesBrain(brain.speciesID, generationMembers);
                species.add(new Specie(brain.speciesID, brainsFromSpecie, 0, 0));
            }
        }
        // Kill unneeded species
        for (Specie specie : species) {
            if (specie.members.isEmpty() || specie.gensSinceImproved >= 15) {
                specie.penalizeSpecie();
            }
        }

        // Now we can update the already existing species
        // Update members, recompute average fitness, increment gensSinceImproved if needed.
        // Offspring will be done later => set it to 0.
        for (Specie specie : species) {
            if (!specie.isDead) {
                specie.members = BrainsHelper.getSameSpeciesBrain(specie.specieID, generationMembers);
                specie.offspring = 0; // for now
                specie.computeAverageFitness();
                double lastAverageAdjustedFitness = specie.averageAdjusetdFitness;
                specie.computeAverageAdjustedFitness();
                specie.hasImproved(lastAverageAdjustedFitness);
            }
        }
    }

    public static int getDifferentSpeciesCount(List<Brain> generationMembers) {
        Set<Integer> speciesIDSet = new HashSet<>();
        for (Brain brain : generationMembers) {
            speciesIDSet.add(brain.speciesID);
        }

        // The highest specie ID is always the amount of different species
        return speciesIDSet.size();
    }


    public static List<Brain> getLeadersList(List<Brain> generationMembers, List<Specie> species) {
        // Return a leader of each existing specie in the generation.
        List<Brain> leadersList = new ArrayList<>();
        for (Specie specie : species) {
            if(!specie.members.isEmpty()){
                List<Brain> brainsOfSameSpecie = BrainsHelper.getSameSpeciesBrain(specie.specieID, generationMembers);
                Brain leaderBrain = BrainsHelper.selectRandomBrain(brainsOfSameSpecie);
                // TODO : fix leaderBrain == null issue
                leadersList.add(leaderBrain);
            }
        }
        return leadersList;
    }

    public static void resetSpecieIDForNonLeaders(List<Brain> leadersList, List<Brain> generationMembers) {
        for (Brain generationalBrain : generationMembers) {
            boolean isLeader = false;
            for (Brain leaderBrain : leadersList) {
                if (generationalBrain.brainID == leaderBrain.brainID) {
                    isLeader = true;
                    break;
                }
            }

            if (!isLeader) {
                generationalBrain.speciesID = -1;
            }
        }
    }

    public static void setSpeciesIDsForBrainsWithoutSpecie(List<Brain> generationMembers, double c1, double c2, double c3, double speciationThreshold) {
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

    public static void normalizeSpeciesIDs(List<Specie> species) {
        int counter = 1;
        for (Specie specie : species) {
            for (Brain memberOfSpecie : specie.members) {
                memberOfSpecie.speciesID = counter;
            }
            specie.specieID = counter;
            ++counter;
        }
    }
}
