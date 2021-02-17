package eoss.moea;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

public class EOSS_Problem extends AbstractProblem {



    public EOSS_Problem(){
        super(1, 2);



    }



    @Override
    public void evaluate(Solution solution){




    }








    @Override
    public Solution newSolution(){
        return new EOSS_Solution();
    }

}
