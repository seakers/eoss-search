package eoss.moea;

import eoss.model.Design;
import eoss.model.DesignSpace;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

import java.util.Random;

public class EOSS_Crossover implements Variation {


    public double mutation_probability;
    public DesignSpace design_space;
    public Random rand;

    public EOSS_Crossover(DesignSpace design_space, double mutation_probability){
        this.rand = new Random();
        this.mutation_probability = mutation_probability;
        this.design_space = design_space;
    }


    @Override
    public Solution[] evolve(Solution[] parents){

        System.out.println("\n\n----> GNC CROSSOVER OPERATION: " + parents.length);
        System.out.println("--> " + parents[0]);

        // TWO PARENTS FOR CROSSOVER
        Solution parent1 = parents[0].copy();
        Solution parent2 = parents[1].copy();

        // CAST APPROPRIATELY
        EOSS_Solution papa = (EOSS_Solution) parent1;
        EOSS_Solution mama = (EOSS_Solution) parent2;

        // TRUE CROSSOVER
         Design child = new Design(papa.design, mama.design);

        // PSUEDO RANDOM
        // Design child = new Design(papa.design.design_space);

        // BEST RANDOM - ACTUALLY WORST
        // Design child = new Design(papa.design.design_space.get_random_design_from_space());

        if(this.getProbabilityResult(this.mutation_probability)){
            child.mutate();
        }

        EOSS_Solution child_soln = new EOSS_Solution(this.design_space, child);
        Solution[] soln = new Solution[] { child_soln };
        return soln;
    }


    // NUM PARENTS REQUIRED
    @Override
    public int getArity(){
        return 2;
    }

    public boolean getProbabilityResult(double probability){
        return (this.rand.nextDouble() <= probability);
    }
}
