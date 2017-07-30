package ch.idsia.ai.ga;

import java.util.Random;

import ch.idsia.ai.EA;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.SimpleMLPAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

public class GeneticAlgorithm implements EA{
    public static GeneticAlgorithm Instance;
    private Evolvable[] population;
    private Double[] fitness;
    private int populationSize;
    private Task task;

    public static float crossover_prob;
    public static float mutation_prob;  
    private int tournamentSize;  

    public GeneticAlgorithm(Task task, int populationSize, Evolvable initial){
        Instance = Instance != null? Instance : this;
        this.populationSize = populationSize;
        this.task = task;
        population = new Evolvable[populationSize];
        fitness = new Double[populationSize];
        crossover_prob = 0.7f;
        mutation_prob = 0.2f;
        tournamentSize = 10;
        populationInitialization(initial);
    }

    public void populationInitialization(Evolvable initial){
        for(int i = 0; i < populationSize; i++){
            population[i] = initial.getNewInstance(); //New neuronal net with same layers, but different values content.
        }
    }

    public void nextGeneration(){
    




    }

    public Evolvable[] tournamentSelection(){
        Evolvable[] selectedEvolvables = new Evolvable[tournamentSize]; //5 posibles candidatos
        Double[] selectedFitness = new Double[tournamentSize]; //5 fitness

        int index = 0, counter = 0;
        boolean contained = false;
        while(counter < tournamentSize){
            do{
                index = new Random().nextInt(populationSize);

                for(int i = 0; i < selectedEvolvables.length; i++){
                    if(selectedEvolvables[i] != null && selectedEvolvables[i].equals(population[index])){
                        contained = true;
                        break;
                    }
                }             
            }while(contained);
            selectedEvolvables[counter] = population[index];               
            selectedFitness[counter] = task.evaluate((Agent) population[index])[0]; //Individual fitness evaluated.
            counter++; 
        }
        selectedEvolvables = sortPopulationByFitness(selectedEvolvables, selectedFitness);

        Evolvable[] parents = new Evolvable[2];
        parents[0] = selectedEvolvables[0];
        parents[1] = selectedEvolvables[1];

        return parents;
    }

    public Evolvable[] sortPopulationByFitness(Evolvable[] tournamentSelection, Double[] fitnessSelection){       
        for(int i = 0; i < tournamentSelection.length; i++){
            for(int j = (i+1); j < tournamentSelection.length; j++){
                if(fitnessSelection[i] < fitnessSelection[j]){
                    Evolvable auxEv = tournamentSelection[i];
                    double auxFit = fitnessSelection[i];

                    tournamentSelection[i] = tournamentSelection[j];
                    fitnessSelection[i] = fitnessSelection[j];
                    tournamentSelection[j] = auxEv;
                    fitnessSelection[j] = auxFit;
                }
            }
        }     
        printTournamentInfo(tournamentSelection);

        return tournamentSelection;
    }
    public Evolvable[] getBests(){
        return new Evolvable[] {population[0]};
    }

    public double[] getBestFitnesses(){
        return new double[] {fitness[0]};
    }


    private void printTournamentInfo(Evolvable[] tournamentSelection){
        soutln("Tournament selection: ");
        for(int i = 0; i < tournamentSelection.length; i++){
            soutp((i+1) + ". " + getEvolvableInfo(tournamentSelection[i]));
        }
    }
    public String getEvolvableInfo(Evolvable e){
         return e.toString() + " " + task.evaluate((Agent) e)[0] + " fitness.";
    }
    
    /*****************************************************************************************************************************************
     *  To test genetic algorithm
     */
    public static void main(String[] args){
        EvaluationOptions options = new CmdLineOptions(args);
        options.setNumberOfTrials(1);
        options.setPauseWorld(true);
        options.setLevelDifficulty(0);
        
        Evolvable initial = new SimpleMLPAgent();
        
        options.setAgent((Agent)initial);
        options.setMaxFPS(true);
        options.setVisualization(false);

        Task task = new ProgressTask(options);

        GeneticAlgorithm ga = new GeneticAlgorithm(task, 100, initial);
        Evolvable[] parents = ga.tournamentSelection(); //2 parents from 'x' tournament selection size array
    }

    public void soutln(String c){
        System.out.println(c);
    }

    public void soutp(String c){
        System.out.print(c);
    }
}