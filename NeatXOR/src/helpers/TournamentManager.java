package helpers;

import classes.neuralNetworks.Brain;

import java.util.ArrayList;
import java.util.List;

public class TournamentManager {
    public static List<Brain> performTournamentSelection(int tournamentSize, int numberOfWinners, List<Brain> brainsOfSameSpecie){
        List<Brain> winners = new ArrayList<>();

        while(winners.size() < numberOfWinners){
            List<Brain> tournamentContestants = new ArrayList<>();
            for(int i = 0; i < tournamentSize; i++){
                // It's fine to use the same brain multiple times.
                tournamentContestants.add(BrainsHelper.selectRandomBrain(brainsOfSameSpecie));
            }
            Brain winner = runTournament(tournamentContestants);
            winners.add(winner);
        }

        // As winners can be the same brain, brainIDs might be the same. Be careful.
        return winners;
    }

    private static Brain runTournament(List<Brain> contestants){
        if (contestants.isEmpty()) {
            return null;
        }
        // Initialization
        Brain winner = contestants.get(0);

        // Winner is the contestant with the highest adjusted fitness
        for (int i = 1; i < contestants.size(); i++) {
            Brain currentContestant = contestants.get(i);

            if (currentContestant.adjustedFitness >= winner.adjustedFitness) {
                winner = currentContestant;
            }
        }
        return winner;
    }
}
