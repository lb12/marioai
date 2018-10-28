package ch.idsia.scenarios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.SimpleMLPAgent;
import ch.idsia.ai.ea.ES;
import ch.idsia.ai.ga.GeneticAlgorithm;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import wox.serial.Easy;

public class EvolveES {

    private static int generations;
    private static int populationSize;
    private static int difficulty;
    private static int currentGeneration;

    private static EvaluationOptions options;
    private static Evolvable initial;

    private static Task task;
    private static ES es;

    private static double averageFitness;
    private static double bestPastFitness;
    private static int pastGenerationMutated;
    private static int previousDifficultyLevel;

    private static List<Evolvable> populationLoaded;
    private static List<String> csvInfo;

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
        options.setLevelType(1);
    }

    private static void initializeEvolutionVariables() {
        initial = new SimpleMLPAgent();
        averageFitness = 0;
        bestPastFitness = 0;
        pastGenerationMutated = 0;
        difficulty = previousDifficultyLevel = difficultyLevelToLoad();
        //difficulty = previousDifficultyLevel = 10;
        loadGeneration();
        generations = 300;
        populationSize = 300;
        populationLoaded = new ArrayList<>(populationSize);
        csvInfo = new ArrayList<>();
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
                //difficultyLevelLoaded -= difficultyLevelLoaded > 0 ? 1 : 0; //Avoid -1 level when 0 level folder is empty
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
            task_es_Instanciation();
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

    private static void task_es_Instanciation() {
        task = new ProgressTask(options);
        es = new ES(task, initial, populationSize);
    }

    private static void evolveGenerations() {
    	long initialTime = System.currentTimeMillis();
        while (currentGeneration < generations) {
        	es.nextGeneration();

            double bestAgentResult = obtainBestCurrentAgentResult(currentGeneration);
            System.out.println("Generation " + currentGeneration + " best " + bestAgentResult);


            addCurrentFitnessInfo(currentGeneration);
            savePopulation();
            saveGeneration();
            saveCurrentFitnessGeneration();

            if (bestAgentResult > 4000 || currentGeneration == (generations-1)) {
            	long currentTime = System.currentTimeMillis();
            	System.out.println("Se ha tardado en pasar el nivel " + (currentTime - initialTime)/60000 + " minutos.");
            	System.out.println("Nivel cargado: " + (difficulty + 1));   
            	populationLoaded.clear();
                initial = initial.getNewInstance();
                currentGeneration = 0;
                break; // Go to next difficulty.
            }
            currentGeneration++;
        }
    }

    private static double obtainBestCurrentAgentResult(int currentGen) {
        Agent a = (Agent) es.getBests()[0];
        a.setName(((Agent) initial).getName() + currentGen);

        return task.evaluate(a)[0];
    }

    private static void updateAverageFitness() {
        averageFitness = es.getAveragePopulationFitness();
    }

    private static void updatePreviousDifficultyLevel() {
        previousDifficultyLevel = difficulty;
    }

    private static void addCurrentFitnessInfo(int currentGen){
        double[] popFitness = es.getFitness();

        String lower_Fitness = (popFitness[popFitness.length - 1] + "").replace('.', ',');
        String higher_fitness = (popFitness[0]  + "").replace('.', ',');
        String avg_fitness = (es.getAveragePopulationFitness() + "").replace('.', ',');
        

        csvInfo.add(currentGen + ";" + lower_Fitness + ";" + higher_fitness + ";" + avg_fitness);
    }

    private static void saveCurrentFitnessGeneration(){
        String fileName = "Population/" + difficulty + "/FitnessInfo.csv";    
        boolean alreadyExists = new File(fileName).exists();
        final String delim = ";";

		try {
            FileWriter fw = new FileWriter(fileName, alreadyExists);            

            if(!alreadyExists){
            fw.append("Generation").append(delim);
            fw.append("Lower fitness").append(delim);
            fw.append("Higher fitness").append(delim);
            fw.append("Average fitness").append("\n");
            }
            
			for(int i = 0; i < csvInfo.size(); i++){
                fw.append(csvInfo.get(i)).append("\n");
            }

            csvInfo.clear();
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private static void savePopulation() {
        double fitness = es.getAveragePopulationFitness();
        
        if (difficulty > previousDifficultyLevel || fitness > averageFitness) {
            savePopulationFile();
            updatePreviousDifficultyLevel();
            updateAverageFitness();
        }
    }

    private static void savePopulationFile(){
        for (int i = 0; i < populationSize; i++) {
            String path = "Population/" + difficulty + "/";
            String filename = "SimpleMLPAgent_" + i + ".xml";
            Evolvable agentSaved = es.getCurrentPopulation()[i];

            Easy.save(agentSaved, path + filename);
        }
    }

    private static void saveGeneration(){       
        String path = "Population/" + difficulty + "/";
        String filename = "GenerationSaved.txt";
        boolean alreadyExists = new File(path+filename).exists();

        if(alreadyExists){
            new File(path+filename).delete();
        }

        try {
            FileWriter fw = new FileWriter(path+filename);   

            fw.append((1 + currentGeneration) + "");

            fw.flush();
            fw.close();

        }catch(IOException ioException){
            ioException.printStackTrace();
        }
    }

    private static void loadGeneration(){
        String path = "Population/" + difficulty + "/";
        String filename = "GenerationSaved.txt";
        boolean alreadyExists = new File(path+filename).exists();

        if(!alreadyExists){
            currentGeneration = 0;            
        }else{
	        try {
                FileReader fr = new FileReader(path+filename);   
                BufferedReader br = new BufferedReader(fr);
                String read = br.readLine();
                   if(read != null){
                       currentGeneration = Integer.parseInt(read);
                   }	            
	            fr.close();
	
	        }catch(IOException ioException){
	            ioException.printStackTrace();
	        }
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