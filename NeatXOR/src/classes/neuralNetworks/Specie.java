package classes.neuralNetworks;

import java.util.List;

public class Specie {
    public int specieID;
    public List<Brain> members;
    public int offspring;
    public double averageFitness;
    public double averageAdjusetdFitness;
    public double fitnessSum;
    public int gensSinceImproved;

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
}
