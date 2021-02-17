package eoss.moea;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;

public class EOSS_Solution  extends Solution {


    public EOSS_Solution(){
        super(1, 2, 0);


        int generated_design_id = 1;
        BinaryIntegerVariable var = new BinaryIntegerVariable(generated_design_id, 0, 10000);
        this.setVariable(0, var);
    }

    protected EOSS_Solution(Solution solution){
        super(solution);


    }




    @Override
    public Solution copy(){
        return new EOSS_Solution(this);
    }

}
