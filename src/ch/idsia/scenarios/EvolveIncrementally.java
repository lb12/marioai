package ch.idsia.scenarios;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.AgentsPool;
import ch.idsia.ai.agents.ai.SimpleMLPAgent;
import ch.idsia.ai.ea.ES;
import ch.idsia.ai.ga.GeneticAlgorithm;
import ch.idsia.ai.tasks.MultiSeedProgressTask;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import wox.serial.Easy;

/**
 * Created by IntelliJ IDEA.
 * User: julian
 * Date: May 4, 2009
 * Time: 4:33:25 PM
 */
public class EvolveIncrementally {

    final static int generations = 100;
    final static int populationSize = 100;


    public static void main(String[] args) {
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setNumberOfTrials(1);
        options.setPauseWorld(false);
        Evolvable initial = new SimpleMLPAgent();
        if (args.length > 0) {
            initial = (Evolvable) AgentsPool.load (args[0]);
        }
//        AgentsPool.registerAgent ((Agent) initial);
        // maybe need
        AgentsPool.addAgent((Agent)initial);
        for (int difficulty = 0; difficulty < 11; difficulty++)
        {
            System.out.println("New EvolveIncrementally phase with difficulty = " + difficulty + " started.");
            options.setLevelDifficulty(difficulty);
            options.setMaxFPS(true);
            options.setVisualization(false); 
            Task task = new ProgressTask(options);
           // MultiSeedProgressTask task = new MultiSeedProgressTask(options);            
           // task.setNumberOfSeeds(3);
           // task.setStartingSeed(0);
            //ES es = new ES (task, initial, populationSize); //Esto lo he comentado para guardar las lineas de la ES
            GeneticAlgorithm ga = new GeneticAlgorithm(task, populationSize, initial);
            System.out.println("Evolving " + initial + " with task " + task);
            for (int gen = 0; gen < generations; gen++) {
                //es.nextGeneration(); //Esto lo he comentado para guardar las lineas de la ES
            	
                while(ga.getNextPopulation().size() < populationSize){ //Generate next population
                    ga.nextGeneration();
                    ga.soutln("Next population size " + ga.getNextPopulation().size());
                }
                ga.soutln("Finished generation " + gen);
                ga.updatePopulation();
                
                
                //double bestResult = es.getBestFitnesses()[0]; //Esto lo he comentado para guardar las lineas de la ES
                double bestResult = ga.getBestFitnesses()[0];
                
                System.out.println("Generation " + gen + " best " + bestResult);
                //options.setVisualization(gen % 5 == 0 || bestResult > 4000);                 
                //options.setMaxFPS(false);
                //options.setVisualization(true);
                //Agent a = (Agent) es.getBests()[0]; //Esto lo he comentado para guardar las lineas de la ES
                Agent a = (Agent) ga.getBests()[0];
                a.setName(((Agent)initial).getName() + gen);
                // AgentsPool.addAgent(a); //Esto ya estaba comentado
                // AgentsPool.setCurrentAgent(a); //Esto ya estaba comentado
                double result = task.evaluate(a)[0];
                System.out.println("Result from best Agent = " + result);
                //System.out.println(task.result.toString()); //Ya no me hace falta (solo en el multiseedprogresstask)
                options.setMaxFPS(false); 
                options.setVisualization(true);   //false             
                //Easy.save (es.getBests()[0], "evolved.xml"); //Esto lo he comentado para guardar las lineas de la ES
                Easy.save (ga.getBests()[0], "evolved.xml");
                if (result > 4000) {
                    //initial = es.getBests()[0]; //Esto lo he comentado para guardar las lineas de la ES
                     initial = ga.getBests()[0];
                    break; // Go to next difficulty.
                }                
            }
        }
        System.exit(0);
    }
}
