package ch.idsia.ai.ga;

import java.util.ArrayList;
import java.util.List;
public class Chromosome{

    private List<Float> genes;
    private double fitness;


    public Chromosome(){
        this.genes = new ArrayList<Float>();
    }

    public Chromosome(List<Float> genes){
        this.genes = genes;
    }

    public void getGenes(List<Float> genes){this.genes = genes;}
    public List<Float> getGenes(){return genes;}
    public void SetFitness(double fitness){this.fitness = fitness;}
    public double GetFitness(){return fitness;}
}