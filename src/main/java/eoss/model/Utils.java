package eoss.model;

import java.util.ArrayList;
import java.util.Collections;

public class Utils {




    public static ArrayList<String> generate_binary_strings(int length){

        // Generate all binary strings of length (num_active) in an ArrayList
        ArrayList<String> bit_strings = new ArrayList<>();
        int[] arr = new int[length];
        Utils.generateAllBinaryStrings(length, arr, 0, bit_strings);
        return bit_strings;
    }


    // Generate all binary strings of length n
    private static void generateAllBinaryStrings(int n, int arr[], int i, ArrayList<String> bit_strings) {
        if (i == n)
        {
            String bit_string = "";
            for (int c = 0; c < n; c++)
            {
                bit_string += Integer.toString(arr[c]);
            }
            bit_strings.add(bit_string);
            return;
        }

        arr[i] = 0;
        Utils.generateAllBinaryStrings(n, arr, i + 1, bit_strings);

        arr[i] = 1;
        Utils.generateAllBinaryStrings(n, arr, i + 1, bit_strings);
    }



    public static ArrayList<ArrayList<ArrayList<Integer>>> enumeratePartitions(int num_elements){

        // outer level: each element contains all partition architectures for 'idx' elements
        // middle level: all partition architectures for 'idx' elements
        // inner level: one architecture
        ArrayList<ArrayList<ArrayList<Integer>>> architectures = new ArrayList<>();

        if(num_elements == 0){
            return architectures;
        }

        // This is the 0th element - all partition architectures for 0 elements (empty)
        architectures.add(new ArrayList<>());

        // This is the 1st element - all partition architectures for 1 element
        ArrayList<ArrayList<Integer>> one_element = new ArrayList<>();
        ArrayList<Integer> one_element_a1 = new ArrayList<>();
        one_element_a1.add(1);
        one_element.add(one_element_a1);
        architectures.add(one_element);

        if(num_elements == 1){
            return architectures;
        }

        // This is the 2nd element - all partition architectures for 2 elements
        ArrayList<ArrayList<Integer>> two_elements = new ArrayList<>();
        ArrayList<Integer> two_elements_a1 = new ArrayList<>();
        two_elements_a1.add(1);
        two_elements_a1.add(1);
        ArrayList<Integer> two_elements_a2 = new ArrayList<>();
        two_elements_a2.add(1);
        two_elements_a2.add(2);
        two_elements.add(two_elements_a1);
        two_elements.add(two_elements_a2);
        architectures.add(two_elements);


        // For our third+ elements - all partition architecture for 3+ elements
        for(int i = 3; i <= num_elements; i++){
            architectures.add(new ArrayList<ArrayList<Integer>>());

            int num_prev_archs = architectures.get(i-1).size();
            for(int a = 0; a < num_prev_archs; a++){

                ArrayList<Integer> arch = architectures.get(i-1).get(a);

                Integer mx = Collections.max(arch) + 1;
                for(int j = 1; j <= mx; j++){
                    ArrayList<Integer> new_arch = new ArrayList(arch);
                    new_arch.add(j);
                    architectures.get(architectures.size()-1).add(new_arch);
                }
            }
        }

        return architectures;
    }



    // Takes the number of groups
    public static ArrayList<ArrayList<ArrayList<Integer>>> enumeratePermutations(int num_elements){
        ArrayList<ArrayList<ArrayList<Integer>>> architectures = new ArrayList<>();

        if(num_elements == 0){
            return architectures;
        }

        // This is the 0th element - all partition architectures for 0 elements (empty)
        architectures.add(new ArrayList<>());

        // This is the 1st element - all partition architectures for 1 element
        ArrayList<ArrayList<Integer>> one_element = new ArrayList<>();
        ArrayList<Integer> one_element_a1 = new ArrayList<>();
        one_element_a1.add(1);
        one_element.add(one_element_a1);
        architectures.add(one_element);

        if(num_elements == 1){
            return architectures;
        }

        // This is the 2nd element - all partition architectures for 2 elements
        ArrayList<ArrayList<Integer>> two_elements = new ArrayList<>();
        ArrayList<Integer> two_elements_a1 = new ArrayList<>();
        two_elements_a1.add(1);
        two_elements_a1.add(2);
        ArrayList<Integer> two_elements_a2 = new ArrayList<>();
        two_elements_a2.add(2);
        two_elements_a2.add(1);
        two_elements.add(two_elements_a1);
        two_elements.add(two_elements_a2);
        architectures.add(two_elements);

        for(int i = 3; i <= num_elements; i++){
            architectures.add(new ArrayList<ArrayList<Integer>>());

            int num_prev_archs = architectures.get(i-1).size();
            for(int a = 0; a < num_prev_archs; a++){

                ArrayList<Integer> arch = architectures.get(i-1).get(a);
                Integer mx = arch.size() + 1;

                for(int j = 1; j <= mx; j++){
                    ArrayList<Integer> copy_arch = new ArrayList(arch);
                    ArrayList<Integer> new_arch = new ArrayList<>();

                    if(j == 1){
                        new_arch.add(mx);
                        new_arch.addAll(copy_arch.subList(j-1, copy_arch.size()));
                    }
                    else if(j == mx){
                        new_arch.addAll(copy_arch.subList(0, j-1));
                        new_arch.add(mx);
                    }
                    else{
                        new_arch.addAll(copy_arch.subList(0, j-1));
                        new_arch.add(mx);
                        new_arch.addAll(copy_arch.subList(j-1, copy_arch.size()));
                    }
                    architectures.get(architectures.size()-1).add(new_arch);
                }
            }

        }

        return architectures;
    }







}
