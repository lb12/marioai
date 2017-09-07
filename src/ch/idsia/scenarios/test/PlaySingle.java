package ch.idsia.scenarios.test;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.ProgressTask;
import ch.idsia.ai.tasks.Task;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import wox.serial.Easy;

public class PlaySingle{

    private static Task task;

    public static void main(String[] args){
        EvaluationOptions options = new CmdLineOptions(new String[0]);
        options.setPauseWorld(false);
        int difficulty = 0;

        Evolvable agentPlayed = (Evolvable) Easy.load("Population/" + difficulty + "/SimpleMLPAgent_0.xml");

        options.setLevelDifficulty(difficulty);
        options.setMaxFPS(false);
        options.setVisualization(true);

        task = new ProgressTask(options);
        
        evaluateAgent(agentPlayed);
    }

    private static void evaluateAgent(Evolvable agentPlayed) {
        Agent a = (Agent) agentPlayed;
        a.setName(((Agent) agentPlayed).getName() + 0);

        task.evaluate(a);
    }




}