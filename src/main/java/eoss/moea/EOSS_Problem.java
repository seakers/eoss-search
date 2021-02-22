package eoss.moea;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import eoss.aws.SqsWrapper;
import eoss.model.Design;
import eoss.model.DesignSpace;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EOSS_Problem extends AbstractProblem {

    public DesignSpace design_space;
    public int run_number;
    public JsonArray designs;

    // --- SQS
    public String vassar_queue_url;
    public String return_queue_url;
    public String sqs_endpoint;
    public SqsClient sqs;


    public EOSS_Problem(DesignSpace design_space, int run_number, String return_queue_url){
        super(1, 2);

        this.design_space = design_space;
        this.run_number = run_number;
        this.designs = new JsonArray();

        this.vassar_queue_url = SqsWrapper.vassar_queue_url;
        this.sqs_endpoint = SqsWrapper.endpoint_override;
        this.return_queue_url = return_queue_url;

        this.sqs = SqsClient.builder()
                .region(Region.US_EAST_2)
                .endpointOverride(URI.create(this.sqs_endpoint))
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }



    @Override
    public void evaluate(Solution solution){

        // CAST
        EOSS_Solution eoss_solution = (EOSS_Solution) solution;

        if(!eoss_solution.already_evaluated){

            // EVALUATION
            ArrayList<Double> results = this.evaluate_eoss(eoss_solution.design);

            // SET OBJECTIVE VALUES
            eoss_solution.setObjective(0, results.get(0)); // SCIENCE
            eoss_solution.setObjective(1, results.get(1)); // COST
            eoss_solution.setObjective(2, results.get(2)); // DATA CONTINUITY
            eoss_solution.already_evaluated = true;
        }
    }







    public ArrayList<Double> evaluate_eoss(Design design){
        System.out.println("\n---------- EVALUATING DESIGN ----------");

        ArrayList<Double> results = new ArrayList<>();

        final Map<String, MessageAttributeValue> eval_message_attributes = design.getEvalMessageAttributes(this.vassar_queue_url);

        SqsWrapper.sendEvalMessage(eval_message_attributes);

        // Get Results
        HashMap<String, String> response = SqsWrapper.getEvalMessageResponse(this.return_queue_url, design.ID, 1);
        while(response.isEmpty()){
            response = SqsWrapper.getEvalMessageResponse(this.return_queue_url, design.ID, 2);
        }

        double science            = Double.parseDouble(response.get("science"));
        double cost               = Double.parseDouble(response.get("cost"));
        double data_continuity    = Double.parseDouble(response.get("data_continuity"));

        System.out.println("---> SCIENCE: " + science);
        System.out.println("---> COST: " + cost);
        System.out.println("---> DATA CONTINUITY: " + data_continuity);
        System.exit(0);

        results.add(-science);
        results.add(cost);
        results.add(data_continuity);

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
