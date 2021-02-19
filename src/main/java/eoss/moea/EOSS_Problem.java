package eoss.moea;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import eoss.model.Design;
import eoss.model.DesignSpace;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import java.io.FileWriter;
import java.util.ArrayList;

public class EOSS_Problem extends AbstractProblem {

    public DesignSpace design_space;
    public int run_number;
    public JsonArray designs;
    public String eval_queue_url;


    public EOSS_Problem(DesignSpace design_space, int run_number){
        super(1, 2);

        this.design_space = design_space;
        this.run_number = run_number;
        this.designs = new JsonArray();
        this.eval_queue_url = "http://localhost:4576/queue/vassar_queue";
    }



    @Override
    public void evaluate(Solution solution){

        // CAST
        EOSS_Solution eoss_solution = (EOSS_Solution) solution;

        if(!eoss_solution.already_evaluated){

            // EVALUATION
            ArrayList<Double> results = this.evaluate_eoss(eoss_solution.design);

            // SET OBJECTIVE VALUES
            eoss_solution.setObjective(0, -results.get(0));
            eoss_solution.setObjective(1, results.get(1));
            eoss_solution.already_evaluated = true;
        }
    }



    public ArrayList<Double> evaluate_eoss(Design design){
        ArrayList<Double> results = new ArrayList<>();

        double science = 0.0;
        double cost = 0.0;






        results.add(science);
        results.add(cost);
        return results;
    }






    public void write_designs(){
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter("/app/results/design/designs_paper_"+this.run_number+".json");
            gson.toJson(this.designs, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }






    @Override
    public Solution newSolution(){
        return new EOSS_Solution(this.design_space);
    }

}
