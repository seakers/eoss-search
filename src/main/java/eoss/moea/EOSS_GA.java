package eoss.moea;

import eoss.model.DesignSpace;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.ParetoObjectiveComparator;
import org.moeaframework.core.operator.InjectedInitialization;
import org.moeaframework.core.operator.TournamentSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class EOSS_GA implements Runnable{

    public Problem problem;
    public double mutation_probability;
    public int num_evaluations;
    public List<Solution> solutions;
    public EpsilonMOEA eMOEA;
    public int run_number;
    public DesignSpace design_space;

    public int initial_pop_size;

    public EOSS_GA(DesignSpace design_space, int num_evaluations, int initial_pop_size, double mutation_probability, int run_number, String return_queue_url){
        this.num_evaluations = num_evaluations;
        this.initial_pop_size = initial_pop_size;
        this.mutation_probability = mutation_probability;
        this.run_number = run_number;
        this.design_space = design_space;
        this.solutions = new ArrayList<>(initial_pop_size);

        // CREATE PROBLEM
        this.problem = new EOSS_Problem(this.design_space, run_number, return_queue_url);

        // INITIAL SOLUTIONS
        for(int x = 0; x < initial_pop_size; x++){
            EOSS_Solution eoss_solution = new EOSS_Solution(this.design_space, this.design_space.get_random_design_from_space());
            eoss_solution.already_evaluated = false;
            this.solutions.add(eoss_solution);
        }
    }

    public void print_solutions(){
        for(Solution solution: this.solutions){
            ((EOSS_Solution) solution).print();
        }
    }


    public void initialize(){

        InjectedInitialization initialization = new InjectedInitialization(this.problem, this.solutions.size(), this.solutions);

        double[]                   epsilonDouble = new double[]{0.001, 1};
        Population                 population    = new Population();
        EpsilonBoxDominanceArchive archive       = new EpsilonBoxDominanceArchive(epsilonDouble);

        ChainedComparator comp        = new ChainedComparator(new ParetoObjectiveComparator());
        TournamentSelection selection = new TournamentSelection(2, comp);

        EOSS_Crossover crossover = new EOSS_Crossover(this.design_space, this.mutation_probability);

        this.eMOEA = new EpsilonMOEA(this.problem, population, archive, selection, crossover, initialization);
    }




    public void run(){



        this.initialize();

        // SUBMIT MOEA
        ExecutorService pool   = Executors.newFixedThreadPool(1);
        CompletionService<Algorithm> ecs    = new ExecutorCompletionService<>(pool);
        ecs.submit(new EOSS_Search(this.eMOEA, this.num_evaluations, this.run_number));


        try {
            org.moeaframework.core.Algorithm alg = ecs.take().get();
            NondominatedPopulation result = alg.getResult();

        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        ((EOSS_Problem) this.problem).write_designs();

        pool.shutdown();
        System.out.println("--> FINISHED");


    }








}
