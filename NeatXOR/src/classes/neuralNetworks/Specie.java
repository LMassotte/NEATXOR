package classes.neuralNetworks;

import helpers.BrainsHelper;
import helpers.SpeciesHelper;
import helpers.TournamentManager;

import java.util.ArrayList;
import java.util.List;

public class Specie {
    public int specieID;
    public List<Brain> members;
    public int offspring;
    public double averageFitness;
    public double averageAdjusetdFitness;
    public double fitnessSum;
    public int gensSinceImproved;
    public boolean isDead;

    public Specie(int id, List<Brain> members, int offspring, double fitnessSum){
        this.specieID = id;
        this.members = members;
        this.offspring = offspring;
        computeAverageFitness();
        computeAverageAdjustedFitness();
        this.gensSinceImproved = 0;
    }

    public void computeAverageFitness(){
        double sum = 0;
        for(Brain brain : members){
            sum += brain.fitness;
        }
        this.averageFitness = sum / members.size();
    }

    public void computeAverageAdjustedFitness(){
        double sum = 0;
        for(Brain brain : members){
            sum += brain.adjustedFitness;
        }
        this.averageAdjusetdFitness = sum / members.size();
    }

    public void penalizeSpecie(){
        // If a specie hasn't improved in 15 generations, it's killed
        if(gensSinceImproved >= 15){
            this.offspring = 0;
            this.isDead = true;
        }
    }

    public boolean hasImproved(double lastGenerationAverageAdjustedFitness){
        return this.averageAdjusetdFitness > lastGenerationAverageAdjustedFitness;
    }

    @Override
    public String toString() {
        return "The specie " + this.specieID + " has "
                + this.members.size() + " members. \n Its average fitness is "
                + this.averageFitness + " and average adjusted fitness is "
                + this.averageAdjusetdFitness +". \n It hasn't evolved since "
                + this.gensSinceImproved + " generations. \n"
                + " The next generation will have " + this.offspring + " members of this specie. \n";
    }

    public List<Brain> selectParentsForNextGen(int tournamentSize, List<Brain> specieMembers){
        // Select 2 parents across this specie population.
        // Using tournament selection which will keep the best brains out of tournament contestants.
        // Contestants are selected randomly across the specie population.
        // "Best" means "has the best adjusted fitness".
        List<Brain> parents = TournamentManager.performTournamentSelection(tournamentSize, 2, specieMembers);

        return parents;
    }
}
