package ch.idsia.ai.ga;

import java.util.Random;
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

public class GeneticAlgorithm implements EA {
    public static GeneticAlgorithm Instance;
    private List<Evolvable> population;
    private List<Evolvable> nextPopulation;
    private int populationSize;
    private Task task;

    public final float crossover_prob;
    public final float mutation_prob;
    private int tournamentSize;

    public GeneticAlgorithm(Task task, int populationSize, Evolvable initial) {
        Instance = Instance != null ? Instance : this;
        this.populationSize = populationSize;
        this.task = task;
        population = new ArrayList<Evolvable>();
        nextPopulation = new ArrayList<Evolvable>();
        crossover_prob = 0.7f;
        mutation_prob = 0.2f;
        tournamentSize = 10;
        populationInitialization(initial);
    }

    public void populationInitialization(Evolvable initial) {
        for (int i = 0; i < populationSize; i++) {
            population.add(initial.getNewInstance()); //New neuronal net with same layers, but different values content.            
        }
    }

    @Override
    public void nextGeneration() {
        //soutln("Hago la seleccion por torneo");
        List<Evolvable> tournamentSelection = tournamentSelection();
        Evolvable[] parents = parentsSelection(tournamentSelection);

        float mutation_prob = new Random().nextFloat();
        float crossover_prob = new Random().nextFloat();

        Evolvable son = null;
        //soutln("Intento el crossover y la mutacion");
        if (crossover_prob <= this.crossover_prob) {
            son = parents[0].crossover((SimpleMLPAgent) parents[1]);
        }

        if (mutation_prob <= this.mutation_prob) {
            parents[0].mutate();
            if (son != null)
                son.mutate();
        }

        //soutln("Anyado un individuo a la nueva poblacion");
        if (son != null) {
            nextPopulation.add(son);
        } else {
            nextPopulation.add(parents[0]);
        }
        //soutln("Tamanyo nueva generacion " + nextPopulation.size());
    }

    @Override
    public Evolvable[] getBests() {
        return new Evolvable[] { population.get(0) };
    }

    @Override
    public double[] getBestFitnesses() {
        return new double[] { task.evaluate((Agent) population.get(0))[0] };
    }

    public List<Evolvable> getNextPopulation(){
        return nextPopulation;
    }

    public void updatePopulation() {
        population.clear();
        population.addAll(nextPopulation);
        nextPopulation.clear();
        sortPopulationByFitness();
    }

    public List<Evolvable> tournamentSelection() {
        List<Evolvable> selectedEvolvables = new ArrayList<>();
        Double[] selectedFitness = new Double[tournamentSize];

        int index = 0, counter = 0;
        while (counter < tournamentSize) {
            do {
                index = new Random().nextInt(populationSize);

            } while (selectedEvolvables.contains(population.get(index)));

            selectedEvolvables.add(population.get(index));
            selectedFitness[counter] = task.evaluate((Agent) population.get(index))[0]; //Individual fitness evaluated.
            counter++;
        }
        selectedEvolvables = sortPopulationByFitness(selectedEvolvables, selectedFitness);
        return selectedEvolvables;
    }

    public Evolvable[] parentsSelection(List<Evolvable> selectedEvolvables) {
        Evolvable[] parents = new Evolvable[2];

        parents[0] = selectedEvolvables.get(0);
        parents[1] = selectedEvolvables.get(1);

        return parents;
    }

    public void sortPopulationByFitness() {

        Double[] fitnessSelection = new Double[populationSize];

        for (int i = 0; i < populationSize; i++) {
            fitnessSelection[i] = task.evaluate((Agent) population.get(i))[0];
        }

        sortPopulationByFitness(population, fitnessSelection);
    }

    public List<Evolvable> sortPopulationByFitness(List<Evolvable> tournamentSelection, Double[] fitnessSelection) {
        for (int i = 0; i < tournamentSelection.size(); i++) {
            for (int j = (i + 1); j < tournamentSelection.size(); j++) {
                if (fitnessSelection[i] < fitnessSelection[j]) {
                    Evolvable auxEv = tournamentSelection.get(i);
                    double auxFit = fitnessSelection[i];
                    tournamentSelection.set(i, tournamentSelection.get(j));
                    fitnessSelection[i] = fitnessSelection[j];
                    tournamentSelection.set(j, auxEv);
                    fitnessSelection[j] = auxFit;
                }
            }
        }
        //printTournamentInfo(tournamentSelection);

        return tournamentSelection;
    }

    /*********************************************************************** INFO METHODS *****************************************************************************/
    private void printTournamentInfo(Evolvable[] tournamentSelection) {
        soutln("Tournament selection: ");
        for (int i = 0; i < tournamentSelection.length; i++) {
            soutp((i + 1) + ". " + getEvolvableInfo(tournamentSelection[i]) + "\n");
        }
    }

    public String getEvolvableInfo(Evolvable e) {
        return e.toString() + " " + task.evaluate((Agent) e)[0] + " fitness.";
    }

    /******************************************************************* TEST GENETIC ALGORITHM ************************************************************************/
    public static void main(String[] args) {
        EvaluationOptions options = new CmdLineOptions(args);
        options.setNumberOfTrials(1);
        options.setPauseWorld(true);
        options.setLevelDifficulty(0);

        Evolvable initial = new SimpleMLPAgent();

        options.setAgent((Agent) initial);
        options.setMaxFPS(true);
        options.setVisualization(false);

        Task task = new ProgressTask(options);

        GeneticAlgorithm ga = new GeneticAlgorithm(task, 100, initial);
        int generations = 5;

        for (int i = 1; i <= generations; i++) {
            ga.soutln("- GENERACION " + i + "\n");
            ga.soutln("Antes de crear la siguiente generacion. Muestro la poblacion.");
            ga.sortPopulationByFitness(); //Sort by fitness

            for (int k = 0; k < ga.populationSize; k++) { //Show individuals on console.
                ga.soutln(ga.getEvolvableInfo(ga.population.get(k)));
            }

            ga.soutln("\nVoy a crear la siguiente generacion.");
            while (ga.nextPopulation.size() < ga.populationSize) { //Generate next population
                ga.nextGeneration();
            }
            ga.updatePopulation();
        }

        ga.soutln("Generación final...");
        ga.sortPopulationByFitness();
        for (int k = 0; k < ga.populationSize; k++) { //Show individuals on console.
            ga.soutln(ga.getEvolvableInfo(ga.population.get(k)));
        }
    }

    public void soutln(String c) {
        System.out.println(c);
    }

    public void soutp(String c) {
        System.out.print(c);
    }
}