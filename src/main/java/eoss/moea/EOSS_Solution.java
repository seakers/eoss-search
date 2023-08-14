package eoss.moea;

import eoss.model.Design;
import eoss.model.DesignSpace;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;

import java.util.Random;

public class EOSS_Solution extends Solution {


    public Design design;
    public DesignSpace design_space;
    public boolean already_evaluated;
    public Random rand;


    public EOSS_Solution(DesignSpace design_space, Design design){
        super(1, 3, 0);
        this.rand = new Random();
        this.design = design;
        this.design_space = design_space;
        this.already_evaluated = false;

        int generated_design_id = this.rand.nextInt(999999)+1;
        BinaryIntegerVariable var = new BinaryIntegerVariable(generated_design_id, 0, 1000000);
        this.setVariable(0, var);
    }

    public EOSS_Solution(DesignSpace design_space){
        super(1, 3, 0);
        this.rand = new Random();
        this.design = new Design(design_space);
        this.already_evaluated = false;
        this.design_space = design_space;

        int generated_design_id = this.rand.nextInt(999999)+1;
        BinaryIntegerVariable var = new BinaryIntegerVariable(generated_design_id, 0, 1000000);
        this.setVariable(0, var);
    }

    protected EOSS_Solution(Solution solution){
        super(solution);

        EOSS_Solution gnc_sol = (EOSS_Solution) solution;
        this.design = new Design(gnc_sol.design);
        this.already_evaluated = gnc_sol.already_evaluated;
        this.design_space = gnc_sol.design_space;
    }

    public void print() {
        this.design.print();
    }



    @Override
    public Solution copy(){
        return new EOSS_Solution(this);
    }

}
