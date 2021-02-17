package eoss.moea;

import org.moeaframework.Analyzer;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;

import java.io.File;
import java.util.concurrent.Callable;

public class EOSS_Search  implements Callable<Algorithm> {

    public final Algorithm alg;
    public Analyzer analyzer;
    public Accumulator accumulator;
    public int max_evaluations;
    public boolean isStopped;
    public int run_number;


    public EOSS_Search(Algorithm alg, int max_evaluations, int run_number){
        this.alg = alg;
        this.accumulator = new Accumulator();
        this.analyzer = new Analyzer()
                .withProblem(this.alg.getProblem())
                .withIdealPoint(-10.1, -0.1)
                .withReferencePoint(0, 100)
                .includeHypervolume()
                .includeAdditiveEpsilonIndicator();
        this.max_evaluations = max_evaluations;
        this.isStopped  = false;
        this.run_number = run_number;
    }




    @Override
    public Algorithm call(){

        System.out.println("---------- GNC ALGORITHM BEGIN ----------");

        alg.step();


        // INITIAL POPULATION
        Population archive = new Population(((AbstractEvolutionaryAlgorithm)alg).getArchive());


        while (!alg.isTerminated() && (alg.getNumberOfEvaluations() < this.max_evaluations) && !isStopped){
            if (this.isStopped) {
                break;
            }

            // ALGORITHM STEP
            alg.step();

            // NEW POPULATION
            Population newArchive = ((AbstractEvolutionaryAlgorithm)alg).getArchive();
            System.out.println("---> Archive size: " + newArchive.size());

            // NEW DESIGN FUNCTIONALITY
            for (int i = 0; i < newArchive.size(); ++i){

                Solution newSol       = newArchive.get(i);
                boolean  alreadyThere = archive.contains(newSol);
                if (!alreadyThere){
                    System.out.println("---> NEW DESIGN FOUND, NEW HV");
                    this.analyzer.add("popEOSS", this.alg.getResult());
                }
            }

            // UPDATE REFERENCE POPULATION
            archive = new Population(newArchive);


            int num_evals = alg.getNumberOfEvaluations();
            if(num_evals > 50){
                double current_hv = this.analyzer.getAnalysis().get("popEOSS").get("Hypervolume").getMax();
                this.accumulator.add("NFE", (num_evals));
                this.accumulator.add("HV", current_hv);
            }

        }


        this.analyzer.printAnalysis();

        this.save_run();

        return this.alg;
    }













    public void save_run(){
        String save_dir = "/app/results/crossover/";
        String file_name = "run_"+this.run_number+".csv";

        try{
            File hv_file = new File(save_dir + file_name);
            this.accumulator.saveCSV(hv_file);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}
