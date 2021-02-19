/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package eoss.search;

import eoss.model.Design;
import eoss.model.DesignSpace;
import eoss.moea.EOSS_GA;

import java.util.ArrayList;

public class App {

    public static void main(String[] args) {


        ArrayList<String> instruments = new ArrayList<>();
        instruments.add("VIIRS");
        instruments.add("BIOMASS");
        instruments.add("SMAP_MWR");
        instruments.add("SMAP_RAD");
        instruments.add("CMIS");

        DesignSpace design_space = new DesignSpace(instruments);
        design_space.enumerate_design_space();
        // design_space.print_design_space();


        int num_evaluations = 0;
        int initial_pop_size = 20;
        double mutation_prob = 0.2;
        int run_number = 0;


        EOSS_GA algorithm = new EOSS_GA(design_space, num_evaluations, initial_pop_size, mutation_prob, run_number);
        algorithm.print_solutions();




//        Design design = new Design(design_space);
//        design.print();






    }
}
