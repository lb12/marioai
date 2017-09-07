package ch.idsia.scenarios;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.SimpleMLPAgent;
import ch.idsia.ai.ga.GeneticAlgorithm;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import wox.serial.Easy;

public class EvolveGA {

    private static int generations;
    private static int populationSize;
    private static int difficulty;

    private static EvaluationOptions options;
    private static Evolvable initial;

    private static Task task;
    private static GeneticAlgorithm ga;

    private static double averageFitness;
    private static int previousDifficultyLevel;

    private static List<Evolvable> populationLoaded;

    public static void main(String[] args) {
        initializeOptions();
        initializeEvolutionVariables();
        loadPopulationFromFile();       
        evolveLevelDifficulty();
        showEndEvolutionMessage();
    }

    private static void initializeOptions() {
        options = new CmdLineOptions(new String[0]);
        options.setNumberOfTrials(1);
        options.setPauseWorld(false);
    }

    private static void initializeEvolutionVariables() {
        initial = new SimpleMLPAgent();
        averageFitness = 0;
        difficulty = previousDifficultyLevel = difficultyLevelToLoad();
        generations = 300;
        populationSize = 100;
        populationLoaded = new ArrayList<>(populationSize);
    }

    private static int difficultyLevelToLoad() {
        boolean isDirectoryEmpty = false;
        int difficultyLevelLoaded = 0;
        File directory;

        while (!isDirectoryEmpty) {
            directory = new File("Population/" + difficultyLevelLoaded);

            if (directory.isDirectory()) {
                if (!isDirectoryEmpty(directory)) {
                    difficultyLevelLoaded++;
                    continue;
                }
                difficultyLevelLoaded -= difficultyLevelLoaded > 0 ? 1 : 0; //Avoid -1 level when 0 level folder is empty
                isDirectoryEmpty = true;
            }
        }
        System.out.println("Dificultad cargada: " + difficultyLevelLoaded);
        return difficultyLevelLoaded;
    }

    private static boolean isDirectoryEmpty(File directory){
        return directory.list().length == 0;
    }

    private static void loadPopulationFromFile(){
        if(!isDirectoryEmpty(new File("Population/"+difficulty))){         
            for(int i = 0; i < populationSize; i++){
                populationLoaded.add((Evolvable) Easy.load("Population/"+difficulty+"/SimpleMLPAgent_" + i + ".xml"));
            }       
        }
    }

    private static void evolveLevelDifficulty() {
        while (difficulty < 11) {
            setLevelDifficulty();
            setFastFPSandVisualizationMode(true, false);
            task_ga_Instanciation();
            evolveGenerations();
            incrementDifficulty();
        }
    }

    private static void setLevelDifficulty() {
        options.setLevelDifficulty(difficulty);
    }

    private static void setFastFPSandVisualizationMode(boolean fastFPS, boolean visualizationSet) {
        options.setMaxFPS(fastFPS);
        options.setVisualization(visualizationSet);
    }

    private static void task_ga_Instanciation() {
        task = new ProgressTask(options);
        ga = populationLoaded.isEmpty() ? new GeneticAlgorithm(task, populationSize, initial) : new GeneticAlgorithm(task, populationSize, populationLoaded);
    }

    private static void evolveGenerations() {
        for (int currentGen = 0; currentGen < generations; currentGen++) {
            obtainNewPopulation(ga);

            double bestAgentResult = obtainBestCurrentAgentResult(currentGen);
            System.out.println("Generation " + currentGen + " best " + bestAgentResult);

            setFastFPSandVisualizationMode(true, false);

            savePopulation();
            if (bestAgentResult > 4000) {
                initial = ga.getBests()[0];
                break; // Go to next difficulty.
            }
        }
    }

    private static void obtainNewPopulation(GeneticAlgorithm ga) {
        while (ga.getNextPopulation().size() < populationSize) {
            ga.nextGeneration();
            System.out.println("Next population size " + ga.getNextPopulation().size());
        }
        ga.updatePopulation();
    }

    private static double obtainBestCurrentAgentResult(int currentGen) {
        Agent a = (Agent) ga.getBests()[0];
        a.setName(((Agent) initial).getName() + currentGen);

        return task.evaluate(a)[0];
    }

    private static void updateAverageFitness() {
        averageFitness = ga.getAveragePopulationFitness();
    }

    private static void updatePreviousDifficultyLevel() {
        previousDifficultyLevel = difficulty;
    }

    private static void savePopulation() {
        double fitness = ga.getAveragePopulationFitness();
        System.out
                .println("Previous difficulty level " + previousDifficultyLevel + " - Difficulty level " + difficulty);
        if (difficulty > previousDifficultyLevel || fitness > averageFitness) {
            for (int i = 0; i < populationSize; i++) {
                String path = "Population/" + difficulty + "/";
                String filename = "SimpleMLPAgent_" + i + ".xml";
                Evolvable agentSaved = ga.getCurrentPopulation().get(i);

                Easy.save(agentSaved, path + filename);
            }
            updatePreviousDifficultyLevel();
            updateAverageFitness();
        }
    }

    private static void incrementDifficulty() {
        difficulty++;
    }

    private static void showEndEvolutionMessage() {
        System.out.println("Evolution has finished.");
        System.exit(0);
    }
}