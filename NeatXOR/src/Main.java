import classes.Brain;
import classes.NeatParameters;

public class Main {
    public static void main(String[] args) {
        NeatParameters neatParameters = new NeatParameters(50, 3, 1, 1, 1.0);
        Brain brain = new Brain(neatParameters);
        brain.Initialize();
        brain.DrawNetwork();
    }
}