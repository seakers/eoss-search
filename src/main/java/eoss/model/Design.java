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
        this.design_space = papa.design_space;

        // Crossover at the mission level
        ArrayList<ArrayList<String>> child_sats = this.crossover_satellites(papa.design_space, papa, mama);

        this.satellites = this.repair_satellites(child_sats);
        this.num_satellites = this.satellites.size();
        this.num_instruments = this.find_num_instruments(this.satellites);



    }

    public ArrayList<ArrayList<String>> crossover_satellites(DesignSpace ds, Design papa, Design mama){
        ArrayList<ArrayList<String>> child_sats = new ArrayList<>();

        int num_papa_sats = papa.satellites.size();
        int num_mama_sats = mama.satellites.size();

        int min_sats = Math.min(num_papa_sats, num_mama_sats);
        int x = 0;
        for(x = 0; x < min_sats; x++){
            if(this.rand.nextBoolean()){
                child_sats.add(papa.satellites.get(x));
            }
            else{
                child_sats.add(mama.satellites.get(x));
            }
        }

        while(x < num_papa_sats){
            if(this.rand.nextBoolean()){
                child_sats.add(papa.satellites.get(x));
            }
            x++;
        }

        while(x < num_mama_sats){
            if(this.rand.nextBoolean()){
                child_sats.add(mama.satellites.get(x));
            }
            x++;
        }
        return child_sats;
    }


    // Make sure there are no duplicate instruments
    public ArrayList<ArrayList<String>> repair_satellites(ArrayList<ArrayList<String>> child_sats){
        ArrayList<ArrayList<String>> new_child_sats = new ArrayList<>();
        ArrayList<String> seen_instruments = new ArrayList<>();

        for(int x = 0; x < child_sats.size(); x++){
            ArrayList<String> satellite = child_sats.get(x);
            ArrayList<String> new_satellite = new ArrayList<>();

            for(int y = 0; y < satellite.size(); y++){
                String instrument = satellite.get(y);
                if(!seen_instruments.contains(instrument)){
                    new_satellite.add(instrument);
                    seen_instruments.add(instrument);
                }
            }

            if(!new_satellite.isEmpty()){
                new_child_sats.add(new_satellite);
            }
        }


        if(new_child_sats.isEmpty()){
            ArrayList<String> rand_sat = new ArrayList<>();
            rand_sat.add(this.design_space.instruments.get(this.rand.nextInt(this.design_space.instruments.size())));
            new_child_sats.add(rand_sat);
        }

        return new_child_sats;
    }


    public int find_num_instruments(ArrayList<ArrayList<String>> sats){
        ArrayList<String> seen_insts = new ArrayList<>();
        for(ArrayList<String> sat: sats){
            for(String inst: sat){
                if(!seen_insts.contains(inst)){
                    seen_insts.add(inst);
                }
            }
        }
        return seen_insts.size();
    }

    public void mutate(){
        double prob = this.rand.nextDouble();
        if(prob < 0.2){
            this.mutate_swap_instrument();
        }
        else if(prob < 0.4){
            this.mutate_add_mission();
        }
        else if(prob < 0.6){
            this.mutate_swap_mission_order();
        }
        else if(prob < 0.8){
            this.mutate_add_instrument();
        }
        else{
            this.mutate_remove_instrument();
        }
    }

    public void mutate_swap_mission_order(){
        if(this.satellites.size() == 1){
            return;
        }
        int swap_idx_1 = this.rand.nextInt(this.satellites.size());
        int swap_idx_2 = swap_idx_1;
        while(swap_idx_1 == swap_idx_2){
            swap_idx_2 = this.rand.nextInt(this.satellites.size());
        }

        ArrayList<ArrayList<String>> new_sats = new ArrayList<>();

        for(int x = 0; x < this.satellites.size(); x++){
            if(x == swap_idx_1){
                new_sats.add(this.satellites.get(swap_idx_2));
            }
            else if(x == swap_idx_2){
                new_sats.add(this.satellites.get(swap_idx_1));
            }
            else{
                new_sats.add(this.satellites.get(x));
            }
        }
        this.satellites = new_sats;
    }

    public void mutate_add_mission(){
        ArrayList<String> desing_insts = new ArrayList<>();
        for(ArrayList<String> satellite: this.satellites){
            for(String instrument: satellite){
                desing_insts.add(instrument);
            }
        }

        ArrayList<String> missing_insts = new ArrayList<>();
        for(String space_inst: this.design_space.instruments){
            if(!desing_insts.contains(space_inst)){
                missing_insts.add(space_inst);
            }
        }

        if(missing_insts.isEmpty()){
            return;
        }

        ArrayList<String> new_mission = new ArrayList<>();
        for(String missing_inst: missing_insts){
            if(this.rand.nextBoolean()){
                new_mission.add(missing_inst);
            }
        }

        if(new_mission.isEmpty()){
            new_mission.add(missing_insts.get(this.rand.nextInt(missing_insts.size())));
        }

        int new_mission_idx = this.rand.nextInt(this.satellites.size());
        this.satellites.add(new_mission_idx, new_mission);
    }

    public void mutate_add_instrument(){
        ArrayList<String> desing_insts = new ArrayList<>();
        for(ArrayList<String> satellite: this.satellites){
            for(String instrument: satellite){
                desing_insts.add(instrument);
            }
        }

        ArrayList<String> missing_insts = new ArrayList<>();
        for(String space_inst: this.design_space.instruments){
            if(!desing_insts.contains(space_inst)){
                missing_insts.add(space_inst);
            }
        }

        if(missing_insts.isEmpty()){
            return;
        }

        String inst_to_add = missing_insts.get(this.rand.nextInt(missing_insts.size()));
        int sat_idx = this.rand.nextInt(this.satellites.size());
        this.satellites.get(sat_idx).add(inst_to_add);
    }

    public void mutate_remove_instrument(){

        int sat_idx = this.rand.nextInt(this.satellites.size());
        if(this.satellites.get(sat_idx).size() == 1){
            this.satellites.remove(sat_idx);
        }
        else{
            int inst_idx = this.rand.nextInt(this.satellites.get(sat_idx).size());
            this.satellites.get(sat_idx).remove(inst_idx);
        }


    }

    public void mutate_swap_instrument(){
        if(this.satellites.size() == 1){
            return;
        }

        // Satellites to swap instruments
        int swap_idx_1 = this.rand.nextInt(this.satellites.size());
        int swap_idx_2 = swap_idx_1;
        while(swap_idx_1 == swap_idx_2){
            swap_idx_2 = this.rand.nextInt(this.satellites.size());
        }

        String inst_swap_1 = this.satellites.get(swap_idx_1).get(this.rand.nextInt(this.satellites.get(swap_idx_1).size()));
        String inst_swap_2 = this.satellites.get(swap_idx_2).get(this.rand.nextInt(this.satellites.get(swap_idx_2).size()));


        this.satellites.get(swap_idx_1).remove(inst_swap_1);
        this.satellites.get(swap_idx_1).add(inst_swap_2);

        this.satellites.get(swap_idx_2).remove(inst_swap_2);
        this.satellites.get(swap_idx_2).add(inst_swap_1);
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

        // Random number of instruments
        this.num_instruments = this.rand.nextInt(this.design_space.instruments.size())+1;

        // Random number of groups
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

    public JsonArray get_design_json(){
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

        return desing_root;
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




    public boolean get_probability_result(double probability){
        return (this.rand.nextDouble() < probability);
    }
}
