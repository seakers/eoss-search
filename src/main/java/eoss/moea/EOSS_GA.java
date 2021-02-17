package eoss.moea;

import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;

import java.util.List;

public class EOSS_GA implements Runnable{

    public Problem problem;
    public double mutation_probability;
    public int num_evaluations;
    public List<Solution> solutions;
    public EpsilonMOEA eMOEA;
    public int run_number;
    public int initial_pop_size;

    public EOSS_GA(int num_evaluations, int initial_pop_size, double mutation_probability, int run_number){
        this.num_evaluations = num_evaluations;
        this.initial_pop_size = initial_pop_size;
        this.mutation_probability = mutation_probability;
        this.run_number = run_number;


    }



    public void run(){











    }








}
