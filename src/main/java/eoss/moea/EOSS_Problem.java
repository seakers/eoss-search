package eoss.moea;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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


    public JsonObject eval_metrics;
    public JsonObject initial_pop_metrics;
    public int num_evals;



    public EOSS_Problem(DesignSpace design_space, int run_number, String return_queue_url){
        super(1, 3);

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

        this.eval_metrics = this.init_eval_metrics();
        this.initial_pop_metrics = this.init_eval_metrics();
        this.num_evals = 0;
    }

    public JsonObject init_eval_metrics(){
        JsonObject metrics = new JsonObject();

        metrics.addProperty("1 instrument", 0);
        metrics.addProperty("2 instrument", 0);
        metrics.addProperty("3 instrument", 0);
        metrics.addProperty("4 instrument", 0);
        metrics.addProperty("5 instrument", 0);

        metrics.addProperty("1 satellite", 0);
        metrics.addProperty("2 satellite", 0);
        metrics.addProperty("3 satellite", 0);
        metrics.addProperty("4 satellite", 0);
        metrics.addProperty("5 satellite", 0);

        return metrics;
    }



    @Override
    public void evaluate(Solution solution){

        // CAST
        EOSS_Solution eoss_solution = (EOSS_Solution) solution;

        if(!eoss_solution.already_evaluated){

            // EVALUATION
            ArrayList<Double> results = this.evaluate_eoss(eoss_solution.design);
            this.record_eval_metrics(eoss_solution, this.eval_metrics);
            if(this.num_evals < 30){
                this.record_eval_metrics(eoss_solution, this.initial_pop_metrics);
            }

            // SET OBJECTIVE VALUES
            eoss_solution.setObjective(0, results.get(0)); // SCIENCE
            eoss_solution.setObjective(1, results.get(1)); // COST
            eoss_solution.setObjective(2, results.get(2)); // DATA CONTINUITY
            eoss_solution.already_evaluated = true;
            this.num_evals++;
        }
    }

    public void record_eval_metrics(EOSS_Solution solution, JsonObject eval_metrics){
        ArrayList<ArrayList<String>> satellites = solution.design.satellites;

        int num_satellites = satellites.size();

        if(num_satellites == 1){
            int current_evals = eval_metrics.get("1 satellite").getAsInt() + 1;
            eval_metrics.addProperty("1 satellite", current_evals);
        }
        if(num_satellites == 2){
            int current_evals = eval_metrics.get("2 satellite").getAsInt() + 1;
            eval_metrics.addProperty("2 satellite", current_evals);
        }
        if(num_satellites == 3){
            int current_evals = eval_metrics.get("3 satellite").getAsInt() + 1;
            eval_metrics.addProperty("3 satellite", current_evals);
        }
        if(num_satellites == 4){
            int current_evals = eval_metrics.get("4 satellite").getAsInt() + 1;
            eval_metrics.addProperty("4 satellite", current_evals);
        }
        if(num_satellites == 5){
            int current_evals = eval_metrics.get("5 satellite").getAsInt() + 1;
            eval_metrics.addProperty("5 satellite", current_evals);
        }

        ArrayList<String> instruments = new ArrayList<>();
        for(ArrayList<String> sat: satellites){
            for(String inst: sat){
                if(!instruments.contains(inst)){
                    instruments.add(inst);
                }
            }
        }

        int num_instruments = instruments.size();

        if(num_instruments == 1){
            int current_evals = eval_metrics.get("1 instrument").getAsInt() + 1;
            eval_metrics.addProperty("1 instrument", current_evals);
        }
        if(num_instruments == 2){
            int current_evals = eval_metrics.get("2 instrument").getAsInt() + 1;
            eval_metrics.addProperty("2 instrument", current_evals);
        }
        if(num_instruments == 3){
            int current_evals = eval_metrics.get("3 instrument").getAsInt() + 1;
            eval_metrics.addProperty("3 instrument", current_evals);
        }
        if(num_instruments == 4){
            int current_evals = eval_metrics.get("4 instrument").getAsInt() + 1;
            eval_metrics.addProperty("4 instrument", current_evals);
        }
        if(num_instruments == 5){
            int current_evals = eval_metrics.get("5 instrument").getAsInt() + 1;
            eval_metrics.addProperty("5 instrument", current_evals);
        }
    }


    public ArrayList<Double> evaluate_eoss(Design design){
        System.out.println("\n---------- EVALUATING DESIGN ----------");

        ArrayList<Double> results = new ArrayList<>();

        final Map<String, MessageAttributeValue> eval_message_attributes = design.getEvalMessageAttributes(this.return_queue_url);

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

        results.add(-science);
        results.add(cost);
        results.add(data_continuity);

        this.record_design(science, cost, data_continuity, design);

        return results;
    }


    public void record_design(double science, double cost, double data_continuity, Design design){
        JsonObject design_obj = new JsonObject();
        design_obj.addProperty("science", science);
        design_obj.addProperty("cost", cost);
        design_obj.addProperty("data_continuity", data_continuity);
        design_obj.add("design", design.get_design_json());
        this.designs.add(design_obj);
    }






    public void write_designs(){
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter("/app/results/moea2/designs/designs_paper_"+this.run_number+".json");
            gson.toJson(this.designs, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

//    public void write_metrics(){
//        try{
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            FileWriter outputfile = new FileWriter("/app/results/moea4/metrics/metrics_"+this.run_number+".json");
//            gson.toJson(this.eval_metrics, outputfile);
//            outputfile.flush();
//            outputfile.close();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    public void write_init_pop_metrics(){
//        try{
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            FileWriter outputfile = new FileWriter("/app/results/moea4/metrics/init_metrics_"+this.run_number+".json");
//            gson.toJson(this.initial_pop_metrics, outputfile);
//            outputfile.flush();
//            outputfile.close();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//    }






    @Override
    public Solution newSolution(){
        return new EOSS_Solution(this.design_space);
    }

}
