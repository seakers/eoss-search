package eoss.model;

import com.google.common.base.CharMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DesignSpace {



    public ArrayList<String> instruments;

    public ArrayList<Design> designs;



    public DesignSpace(ArrayList<String> instruments){
        this.instruments = instruments;
        this.designs = new ArrayList<>();
    }








    public Design get_random_design_from_space(){
        return this.designs.get((new Random()).nextInt(this.designs.size()));
    }


    public void enumerate_design_space(){

        // 1. Get instrument selection enumerations
        ArrayList<String> instrument_bit_strings = Utils.generate_binary_strings(this.instruments.size());


        // 2. For all instrument bit-strings, enumerate all partitions
        for(String bit_string: instrument_bit_strings){
            int num_true = CharMatcher.is('1').countIn(bit_string);
            ArrayList<ArrayList<ArrayList<Integer>>> all_partitions = Utils.enumeratePartitions(num_true);
            if(all_partitions.isEmpty()){
                continue;
            }
            ArrayList<ArrayList<Integer>> partitions = all_partitions.get(all_partitions.size()-1);



            // 3. Get all permutations
            for(ArrayList<Integer> partition_arch: partitions){
                int num_groups = Collections.max(partition_arch);

                ArrayList<ArrayList<ArrayList<Integer>>> all_permutations = Utils.enumeratePermutations(num_groups);
                if(all_partitions.isEmpty()){
                    continue;
                }
                ArrayList<ArrayList<Integer>> permutations = all_permutations.get(all_permutations.size()-1);


                // Derive the instruments of this architecture
                ArrayList<String> arch_instruments = new ArrayList<>();
                for(int x = 0; x < bit_string.length(); x++){
                    if(bit_string.charAt(x) == '1'){
                        arch_instruments.add(this.instruments.get(x));
                    }
                }

                // Derive the satellites of this architecture
                ArrayList<ArrayList<String>> arch_group_satellites = new ArrayList<>();
                int num_sats = Collections.max(partition_arch);
                for(int x = 0; x < num_sats; x++){
                    arch_group_satellites.add(new ArrayList<>());
                }
                for(int x = 0; x < partition_arch.size(); x++){
                    int group_idx = partition_arch.get(x) - 1;
                    arch_group_satellites.get(group_idx).add(arch_instruments.get(x));

                }


                for(ArrayList<Integer> permutation: permutations){
                    ArrayList<ArrayList<String>> arch_satellites = new ArrayList<>();
                    for(Integer pos: permutation){
                        arch_satellites.add(arch_group_satellites.get(pos-1));
                    }

                    this.designs.add(new Design(this, arch_satellites, num_true));
                }
            }
        }
    }

    public void print_design_space(){
        for(Design design: this.designs){
            design.print();
        }
        this.print_design_space_size();
    }

    public void print_design_space_size(){
        System.out.println("\n----- TOTAL NUMBER OF DESIGNS: " + this.designs.size());
    }












}
