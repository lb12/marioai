package ch.idsia.ai.ga;

import java.util.ArrayList;
import java.util.List;

import ch.idsia.ai.EA;
import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.SimpleMLPAgent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;

public class GeneticAlgorithm implements EA{

    private Evolvable[] population;
    private Double[] fitness;
    private int populationSize;

    public static float crossover_prob;
    public static float mutation_prob;    

    public GeneticAlgorithm(Task task, int populationSize, Evolvable initial){
        this.populationSize = populationSize;
        population = new Evolvable[populationSize];
        fitness = new Double[populationSize];
        crossover_prob = 0.7f;
        mutation_prob = 0.2f;
        populationInitialization(initial);
    }

    public void populationInitialization(Evolvable initial){
        System.out.println("Voy a inicializar la poblacion");
        for(int i = 0; i < populationSize; i++){
            population[i] = initial.getNewInstance(); //New neuronal net with same layers, but different values content.
        }
    }

    public void nextGeneration(){

    }

    public void sortPopulationByFitness(){
        for(int i = 0; i < population.length; i++){
            for(int j = (i+1); i < population.length; j++){
                if(fitness[i] < fitness[j]){
                    swap(i, j);
                }
            }
        }
    }

    public void swap(int i, int j){ //Bubble swap
        Evolvable auxEv = population[i];
        double auxFit = fitness[i];

        population[i] = population[j];
        fitness[i] = fitness[j];
        population[j] = auxEv;
        fitness[j] = auxFit;
    }

    public Evolvable[] getBests(){
        return new Evolvable[] {population[0]};
    }

    public double[] getBestFitnesses(){
        return new double[] {fitness[0]};
    }
    
    /**
     *  Para testear el algoritmo genetico.
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

        new GeneticAlgorithm(task, 100, initial);

    }
}