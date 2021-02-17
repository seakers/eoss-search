package eoss.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Design {

    public DesignSpace design_space;
    public Random rand;


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



    }


    // COPY CONSTRUCTOR
    public Design(Design design){
        this.design_space = design.design_space;
        this.rand = new Random();
        this.satellites = design.satellites;
        this.num_instruments = design.num_instruments;
        this.num_satellites = design.num_satellites;
    }


    // RANDOM DESIGN
    public Design(DesignSpace design_space){
        this.rand = new Random();
        this.design_space = design_space;
        this.random_design();
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
}
