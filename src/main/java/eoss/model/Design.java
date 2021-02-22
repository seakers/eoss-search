package eoss.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.*;

public class Design {

    public DesignSpace design_space;
    public Random rand;

    public String ID;
    public ArrayList<ArrayList<String>> satellites;

    public int num_instruments;
    public int num_satellites;

    /*
        CROSSOVER - Crosses over designs at the satellite level
        MUTATION - Mutates instruments in satellites / satellite ordering
     */


    // CROSSOVER OPERATOR
    public Design(Design papa, Design mama){
        this.rand = new Random();
        this.ID = UUID.randomUUID().toString();



    }



    public void mutate(){

    }



    // COPY CONSTRUCTOR
    public Design(Design design){
        this.ID = UUID.randomUUID().toString();
        this.design_space = design.design_space;
        this.rand = new Random();
        this.satellites = design.satellites;
        this.num_instruments = design.num_instruments;
        this.num_satellites = design.num_satellites;
    }


    // RANDOM DESIGN
    public Design(DesignSpace design_space){
        this.ID = UUID.randomUUID().toString();
        this.rand = new Random();
        this.design_space = design_space;
        this.random_design();
    }

    // PRE-CONSTRUCTED DESIGN
    public Design(DesignSpace design_space, ArrayList<ArrayList<String>> satellites, int num_instruments){
        this.ID = UUID.randomUUID().toString();
        this.design_space = design_space;
        this.satellites = satellites;
        this.num_instruments = num_instruments;
        this.num_satellites = satellites.size();
    }





    public void random_design(){
        this.satellites = new ArrayList<>();
        this.num_instruments = this.rand.nextInt(this.design_space.instruments.size())+1;
        this.num_satellites = this.rand.nextInt(this.num_instruments) + 1;

        // Get design instruments
        ArrayList<String> design_instruments = new ArrayList<>();
        while(design_instruments.size() < this.num_instruments){
            String rand_inst = this.design_space.instruments.get(this.rand.nextInt(this.design_space.instruments.size()));
            if(!design_instruments.contains(rand_inst)){
                design_instruments.add(rand_inst);
            }
        }

        // Randomly put design instruments into num_satellites containers
        this.satellites = this.random_partitioning(design_instruments, this.num_satellites);
        Collections.shuffle(this.satellites);
    }

    public ArrayList<ArrayList<String>> random_partitioning(ArrayList<String> instruments, int num_partitions){
        ArrayList<ArrayList<String>> partitions = new ArrayList<>();

        // Instantiate empty partitions
        for(int x = 0; x < num_partitions; x++){
            partitions.add(new ArrayList<>());
        }

        // Randomly assign instruments to partitions
        for(String instrument: instruments){
            partitions.get(this.rand.nextInt(num_partitions)).add(instrument);
        }

        // Repair partitions
        for(ArrayList<String> partition: partitions){
            if(partition.isEmpty()){
                String free_instrument = this.get_free_instrument(partitions);
                partition.add(free_instrument);
            }
        }
        return partitions;
    }

    public String get_free_instrument(ArrayList<ArrayList<String>> partitions){
        boolean found = false;
        String free_inst = "";
        while(!found){
            ArrayList<String> partition = partitions.get(this.rand.nextInt(partitions.size()));
            if(partition.size() > 1){
                int rand_idx = this.rand.nextInt(partition.size());
                free_inst = partition.get(rand_idx);
                partition.remove(free_inst);
                found = true;
            }
        }
        return free_inst;
    }

    public void print(){
        System.out.println("\n--> DESIGN");
        for(int x = 0; x < this.satellites.size(); x++){
            System.out.println("--> SATELLITE " + x + ": " + this.satellites.get(x));
        }
    }

    public void print(ArrayList<ArrayList<String>> satellites){
        for(int x = 0; x < satellites.size(); x++){
            System.out.println("--> SATELLITE " + x + ": " + satellites.get(x));
        }
    }





    public String get_design_string(){
        JsonArray desing_root = new JsonArray();
        for(ArrayList<String> satellite: this.satellites){
            JsonObject sat = new JsonObject();
            JsonArray sat_insts = new JsonArray();
            for(String inst: satellite){
                JsonObject sat_inst = new JsonObject();
                sat_inst.addProperty("name", inst);
                sat_insts.add(sat_inst);
            }
            sat.add("elements", sat_insts);
            desing_root.add(sat);
        }

        return (new GsonBuilder().setPrettyPrinting().create()).toJson(desing_root);
    }

    public void print_design_string(){
        System.out.println("\n---------- DESIGN ----------");
        System.out.println(this.get_design_string());
    }





    public Map<String, MessageAttributeValue> getEvalMessageAttributes(String eval_queue_url){

        final Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put("msgType",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("add")
                        .build()
        );
        messageAttributes.put("input",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(this.get_design_string())
                        .build()
        );
        messageAttributes.put("rQueue",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(eval_queue_url)
                        .build()
        );
        messageAttributes.put("UUID",
                MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue(this.ID)
                        .build()
        );

        return messageAttributes;
    }



}
