package optas.optimizer;

import cern.colt.list.DoubleArrayList;
import Jama.CholeskyDecomposition;
import Jama.Matrix;
import cern.jet.stat.Descriptive;
import jams.JAMS;
import java.util.Arrays;
import optas.core.AbstractFunction;
import optas.core.ObjectiveAchievedException;
import optas.core.SampleLimitException;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.SampleFactory;
import optas.test.LeafRiverExample;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.*;
import umontreal.iro.lecuyer.util.Num;

public class DREAM extends Optimizer {

    //wrap it up for OPTAS
    //"Number of Markov Chains / sequences.")
    private int nChains = -1;
    
    public void setNumberOfMarkovChains(int MCMCPar_seq){
        this.nChains = MCMCPar_seq;
    }
    public int getNumberOfMarkovChains(){
        return nChains;
    }
    
    //"Crossover values used to generate proposals (geometric series).")
    private int nCR = -1;
    
    public void setCrossoverValue(int nCR){
        this.nCR = nCR;
    }
    public double getCrossoverValue(){
        return nCR;
    }
        
    //"Number of DEpairs.")
    private int DEpairs = -1;
    
    public void setDEPairs(int DEpairs){
        this.DEpairs = DEpairs;
    }
    public int getDEPairs(){
        return DEpairs;
    }
    
    //"Number of steps in sem.")
    private int MCMCsteps = -1;
    
    public void setMCMCsteps(int MCMC_steps){
        this.MCMCsteps = MCMC_steps;
    }
    public int getsetMCMCsteps(){
        return MCMCsteps;
    }
        
    //"Random error for ergodicity.")
    private double MCMCeps = Double.NaN;
    
    public void setMCMCeps(double eps){
        this.MCMCeps = eps;
    }
    public double getMCMCeps(){
        return MCMCeps;
    }
    
    //"Random error for ergodicity.")
    public boolean transformObjectiveIntoLogLikelihood = true;
    
    public void setTransformObjectiveIntoLogLikelihood(boolean transformObjectiveIntoLogLikelihood){
        this.transformObjectiveIntoLogLikelihood = transformObjectiveIntoLogLikelihood;
    }
    public boolean isTransformObjectiveIntoLogLikelihood(){
        return transformObjectiveIntoLogLikelihood;
    }
    
    public double sigma = 1.0;
    public void setSigma(double sigma){
        this.sigma = sigma;
    }
    
    public double getSigma(){
        return sigma;
    }
    
    private double gamma = 0.0;
    public void setGamma(double gamma){
        this.gamma = gamma;
    }
    
    public double getGamma(){
        return gamma;
    }
    
    //"Dimension of the problem.")
    private double MCMCPar_n = Double.NaN;
    //"Number of Markov Chains / sequences.")
    private double MCMCPar_seq = Double.NaN;
    //"Maximum number of function evaluations.")
    private double MCMCPar_ndraw = Double.NaN;
    //"Crossover values used to generate proposals (geometric series).")
    private double MCMCPar_nCR = Double.NaN;
    //"Kurtosis parameter Bayesian Inference Scheme.")
    private double MCMCPar_Gamma = Double.NaN;
    //"Number of DEpairs.")
    private double MCMCPar_DEpairs = Double.NaN;
    //"Number of steps in sem.")
    private double MCMCPar_steps = Double.NaN;
    //"Random error for ergodicity.")
    private double MCMCPar_eps = Double.NaN;
    //"What kind of test to detect outlier chains?.")
    private String MCMCPar_outlierTest = "IQR_test";
    //"Adaptive tuning of crossover values.")
    private String Extra_pCR = "Update";
    //"Adaptive tuning of crossover values.")
    private String Extra_reduced_sample_collection;
    //"Give the parameter ranges (minimum values).")
    private double[] ParRange_minn;
    //"Give the parameter ranges (maximum values).")
    private double[] ParRange_maxn;
    //"Define the boundary handling.")
    private String Extra_BoundHandling;
    //"Save in memory or not.")
    private String Extra_save_in_memory = "Yes";


    //"Define likelihood function -- Sum of Squared Error.")
    private String Extra_DR = "No";
    private double Extra_DRscale;
    //"The hist_logp.")
    private double[][] hist_logp;
    private int int_MCMCPar_DEpairs;
    private int int_MCMCPar_n;
    private int int_MCMCPar_seq;
    private int int_MCMCPar_steps;
    private double[][] x;
    private double[][] X;
    private double[] log_p_CD;
    private double[][] p_CD;
    private double sequences[][][];
    private double seq_x_gel[][][];
    private double[][] output_CR;
    private double[] pCR;
    private int Iter;
    private int dim1_seq_gel;
    private int dim2_seq_gel;
    private int dim3_seq_gel;
    private int iloc;
    private int teller;
    private int new_teller;
    private double[][] x_old;
    private double[] p_old;
    private double[] log_p_old;
    private double[][] CR;
    private int[] DEversion;
    private int gen_number;
    private double[][] Table_JumpRate;
    private double[][] x_new;
    private double[][] newgen;
    private int int_MCMCPar_nCR;
    private double[] delta_tot;
    private int counter;
    private double[][] output_AR;
    private double[] accept;
    private int int_MCMCPar_ndraw;
    private double[] lCR_out_AdaptpCR;
    private double[] pCR_out_AdaptpCR;
    private double[] lCR;
    private int aggiorna_outlier;
    private double[][] output_outlier;
    private double[][] output_R_stat;
        
    RandomStream randomstream;

    @Override
    public OptimizerDescription getDescription(){
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(
                DREAM.class.getSimpleName(), DREAM.class.getName(), 500, false);
        
        desc.addParameter(new NumericOptimizerParameter("nChains",
                JAMS.i18n("number_of_Monte_Carlo_Markov_Chains._May_be_choosen_as_n"), 3, 2, 100));
        
        desc.addParameter(new NumericOptimizerParameter("nCr",
                JAMS.i18n("Crossover_values_used_to_generate_proposals_(geometric_series)"), 3, 1, 100));
        
        desc.addParameter(new NumericOptimizerParameter("DEpairs",
                JAMS.i18n("Pairs_of_differential_evolution"), 1, 1, 100));
        
        desc.addParameter(new NumericOptimizerParameter("gamma",
                JAMS.i18n("Curtosis_parameter"), 0, 0, 1));
        
        desc.addParameter(new NumericOptimizerParameter("sigma",
                JAMS.i18n("Uncertainty_Parameter_Of_Objective_Function"), 0.1, 0, 1000));
        
        desc.addParameter(new NumericOptimizerParameter("MCMCsteps",
                JAMS.i18n("number_of_steps_in_sem"), 10, 1, 1000));
        
        desc.addParameter(new NumericOptimizerParameter("MCMCeps",
                JAMS.i18n("Random_error_for_ergodicity"), 2e-1, 0, 10));
        
        desc.addParameter(new BooleanOptimizerParameter("transformObjectiveIntoLogLikelihood",
                JAMS.i18n("Transform_Objective_Into_Log_Likelihood"), true));
        
        return desc;
    }
    
    @Override
    public boolean init(){
        super.init();
    
        MCMCPar_n = Double.NaN;    
        MCMCPar_seq = Double.NaN;
        MCMCPar_ndraw = Double.NaN;
        MCMCPar_nCR = Double.NaN;
        MCMCPar_Gamma = Double.NaN;
        MCMCPar_DEpairs = Double.NaN;
        MCMCPar_steps = Double.NaN;
        MCMCPar_eps = Double.NaN;
        MCMCPar_outlierTest = "IQR_test";
        Extra_pCR = "Update";
        Extra_reduced_sample_collection = "No";
        Extra_BoundHandling = "Reflect";
        Extra_save_in_memory = "Yes";
        Extra_DR = "No";
        Extra_DRscale = 10;

        hist_logp = null;
        int_MCMCPar_DEpairs = -1;
        int_MCMCPar_n = -1;
        int_MCMCPar_seq = -1;
        int_MCMCPar_steps = -1;
        x = null;
        X = null;
        log_p_CD = null;
        p_CD = null;
        sequences = null;
        seq_x_gel = null;
        output_CR = null;
        pCR = null;
        Iter = 0;
        dim1_seq_gel = 0;
        dim2_seq_gel = 0;
        dim3_seq_gel = 0;
        iloc = 0;
        teller = 0;
        new_teller = 0;
        x_old = null;
        p_old = null;
        log_p_old = null;
        CR = null;
        DEversion = null;
        gen_number = 0;
        Table_JumpRate = null;
        x_new = null;
        newgen= null;
        int_MCMCPar_nCR= 0;
        delta_tot= null;
        counter= 0;
        output_AR= null;
        accept= null;
        int_MCMCPar_ndraw= 0;
        lCR_out_AdaptpCR= null;
        pCR_out_AdaptpCR= null;
        lCR= null;
        aggiorna_outlier= 0;
        output_outlier= null;
        output_R_stat= null;
        
        //this is a must condition
        nChains = n;

        if (this.nCR == -1){
            nCR = 3;
        }
        if (this.MCMCsteps == -1){
            MCMCsteps = 10;
        }
        if (this.DEpairs == -1){
            DEpairs = 1;
        }
        //this is also a must condition which contradicts the other one
        if (this.nChains<2*DEpairs+1){
            this.nChains = 2*DEpairs+1;
        }
        if (Double.isNaN(this.MCMCeps)){
            MCMCeps = 2e-1;
        }

        //copy values
        //parameter
        MCMCPar_nCR = (double)nCR;
        MCMCPar_steps = (double)MCMCsteps;
        MCMCPar_seq = (double)nChains;
        MCMCPar_DEpairs = (double)DEpairs;
        MCMCPar_eps = MCMCeps;
        MCMCPar_Gamma = gamma;
        //values from super class
        MCMCPar_n = getFunction().getInputDimension();
        MCMCPar_ndraw = getMaxn();
        
        ParRange_minn = new double[n];
        ParRange_maxn = new double[n];
        for (int i=0;i<n;i++){
            ParRange_minn[i] = getFunction().getRange()[i][0];
            ParRange_maxn[i] = getFunction().getRange()[i][1];
        }
        
        randomstream = new MRG32k3a();
        aggiorna_outlier = 0;
        int_MCMCPar_DEpairs = (int) MCMCPar_DEpairs;
        int_MCMCPar_n = (int) MCMCPar_n;
        int_MCMCPar_seq = (int) MCMCPar_seq;
        int_MCMCPar_steps = (int) MCMCPar_steps;
        int_MCMCPar_nCR = (int) MCMCPar_nCR;
        int_MCMCPar_ndraw = (int) MCMCPar_ndraw;


        InitVariables(MCMCPar_DEpairs, MCMCPar_eps, MCMCPar_Gamma,
                MCMCPar_n, MCMCPar_nCR, MCMCPar_ndraw,
                MCMCPar_seq, MCMCPar_steps, Extra_BoundHandling,
                Extra_DRscale, Extra_reduced_sample_collection);

        return true;
    }
    
    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException{    
        // Step 1: Sample s points in the parameter space
        x = LHS(ParRange_minn, ParRange_maxn, int_MCMCPar_seq);
        
        // Step 2: Calculate posterior density associated with each value in x

        computeDensity(x);

        X = new double[x.length][(x[0].length + 2)];
        for (int row = 0; row < x.length; row++) {
            for (int col = 0; col < x[0].length; col++) {
                X[row][col] = x[row][col];
                X[row][x[0].length] = p_CD[row][0];
                X[row][x[0].length + 1] = log_p_CD[row];
            }
        }

        if (Extra_save_in_memory.equals("Yes")) {
            sequences = InitSequences(X);
        }

        // Save N_CR in memory and initialize delta_tot
        output_CR[0][0] = Iter;
        for (int ind = 1; ind < pCR.length + 1; ind++) {
            output_CR[0][ind] = pCR[ind - 1];
        }

        delta_tot = new double[int_MCMCPar_nCR];
        // Save history log density of individual chains

        double[] X_x_histlogp = new double[int_MCMCPar_seq];
        for (int i = 0; i < X.length; i++) {
            X_x_histlogp[i] = X[i][int_MCMCPar_n + 1];
        }
        hist_logp[0][0] = Iter;
        for (int i = 1; i < int_MCMCPar_seq + 1; i++) {
            hist_logp[0][i] = X_x_histlogp[i - 1];
        }
        dim1_seq_gel = iloc;
        dim2_seq_gel = int_MCMCPar_n;
        dim3_seq_gel = int_MCMCPar_seq;

        seq_x_gel = new double[dim1_seq_gel][dim2_seq_gel][dim3_seq_gel];
        // double somma=0;
        for (int i = 0; i < dim1_seq_gel; i++) {
            // System.out.println(i);
            for (int j = 0; j < dim2_seq_gel; j++) {
                for (int z = 0; z < dim3_seq_gel; z++) {
                    // System.out.print(sequences[i][j][z]+ " ");
                    seq_x_gel[i][j][z] = sequences[i][j][z];
                }
                // System.out.println();
            }
        }
        double[] rstat = Gelman(seq_x_gel);
        output_R_stat[0][0] = Iter;
        // System.out.println(output_R_stat.length+";"+output_R_stat[0].length);
        for (int pp = 1; pp < output_R_stat[0].length; pp++) {
            output_R_stat[0][pp] = rstat[pp - 1];
        }

        // Now start iteration ...
        while (Iter < MCMCPar_ndraw) {
            // Loop a number of times
            for (gen_number = 0; gen_number < int_MCMCPar_steps; gen_number++) {
                // Initialize DR properties
                // int accept2 = 0;
                int ItExtra = 0;
                new_teller = new_teller + 1;
                // Define the current locations and associated posterior
                // densities
                GetLocation(X); // esce x_old px_old log_pxold
                ItExtra = 0;
                // Now generate candidate in each sequence using current point
                // and members of X
                // double [][]CRold=new double[CR.length][CR[0].length];
                // for(int row=0;row<CR.length;row++){
                // for(int col=0;col<CR[0].length;col++){
                // CRold[row][col]=CR[row][col];
                // }
                // }
                offde(X);// da qui esce x_new e si aggiorn CR
                // for(int row=0;row<CR.length;row++){
                // for(int col=0;col<CR[0].length;col++){
                // System.out.print(CR[row][col]-CRold[row][col]+" ");
                // }
                // System.out.println();
                // }
                computeDensity(x_new);
                double[][] p_xnew1 = p_CD;
                double[] log_p_xnew = log_p_CD;
                double[] p_xnew = new double[p_xnew1.length];
                for (int k = 0; k < p_xnew1.length; k++) {
                    p_xnew[k] = p_xnew1[k][0];
                }
                // Now apply the acceptance/rejectance rule for the chain itself
                metrop(x_new, p_xnew, log_p_xnew, x_old, p_old, log_p_old);

                // Define the location in the sequence
                if (Extra_save_in_memory.equals("Yes")) {
                    // Define idx based on iloc
                    iloc = iloc + 1;
                }
                // Now update the locations of the Sequences with the current
                // locations
                double[][] newgentraspose = new double[newgen[0].length][newgen.length];
                for (int row = 0; row < newgentraspose.length; row++) {
                    for (int col = 0; col < newgentraspose[0].length; col++) {
                        newgentraspose[row][col] = newgen[col][row];
                    }
                }
                // System.out.println("newgen");
                // for (int row=0;row<newgentraspose.length;row++){
                // for(int col=0;col<newgentraspose[0].length;col++){
                // System.out.print(newgentraspose[row][col]+" ");
                // }
                // System.out.println();
                // }

                for (int d2 = 0; d2 < sequences[0].length; d2++) {
                    for (int d3 = 0; d3 < sequences[0][0].length; d3++) {
                        sequences[iloc - 1][d2][d3] = newgentraspose[d2][d3];
                    }
                }
                // And update X using current members of Sequences
                // for (int row=0;row<X.length;row++){
                // for(int col=0;col<X[0].length;col++){
                // System.out.print(X[row][col]+" ");
                // }
                // System.out.println();
                // }
                //
                X = newgen;
                // for (int row=0;row<X.length;row++){
                // for(int col=0;col<X[0].length;col++){
                // System.out.print(X[row][col]+" ");
                // }
                // System.out.println();
                // }
                newgen = new double[x_old.length][x_old[0].length + 2];
                if (Extra_pCR.equals("Update")) {
                    // Calculate the standard deviation of each dimension of X
                    double[] st = new double[int_MCMCPar_n];
                    double[] r1 = new double[int_MCMCPar_n];
                    for (int col = 0; col < int_MCMCPar_n; col++) {
                        double[] colonna = new double[int_MCMCPar_seq];
                        double somma = 0;
                        for (int row = 0; row < int_MCMCPar_seq; row++) {
                            colonna[row] = X[row][col];
                            somma = somma + colonna[row];
                        }
                        double media = somma / int_MCMCPar_seq;
                        DoubleArrayList arrlis = new DoubleArrayList(colonna);
                        st[col] = Descriptive.sampleVariance(arrlis, media);
                        r1[col] = Math.sqrt(st[col]);
                        // r1[col]=Descriptive.sampleStandardDeviation(int_MCMCPar_seq,
                        // st[col]);
                    }

                    double[] delta_normX = new double[x_old.length];
                    double[][] diff2 = new double[x_old.length][x_old[0].length];
                    for (int row = 0; row < x_old.length; row++) {
                        double sommanor = 0;
                        for (int col = 0; col < x_old[0].length; col++) {
                            diff2[row][col] = ((x_old[row][col] - X[row][col]) / r1[col])
                                    * ((x_old[row][col] - X[row][col]) / r1[col]);
                            // diff2[row][col]=((x_old[row][col]-X[row][col])/r1[row])*((x_old[row][col]-X[row][col])/r1[row]);
                            sommanor = sommanor + diff2[row][col];
                        }
                        delta_normX[row] = sommanor;
                    }

                    double[] CR_vet = new double[int_MCMCPar_seq];
                    for (int jj = 0; jj < int_MCMCPar_seq; jj++) {
                        CR_vet[jj] = CR[jj][gen_number];
                    }

                    delta_tot = CalcDelta(delta_tot, delta_normX, CR_vet);
                    // for(int row=0;row<delta_tot.length;row++){
                    // System.out.print("deltatot= "+delta_tot[row]+" ");
                    // }
                    // System.out.println();

                }
                // Update hist_logp
                // System.out.println("righe e colonne"+hist_logp.length+";"+hist_logp[0].length);
                hist_logp[counter][0] = Iter;

                // double []Xforhistlogp=new double[]
                for (int pp = 0; pp < int_MCMCPar_seq; pp++) {
                    hist_logp[counter][pp + 1] = X[pp][int_MCMCPar_n + 1];
                }
                // Save some important output -- Acceptance Rate
                output_AR[counter][0] = Iter;
                double summaccept = 0;
                for (int y = 0; y < accept.length; y++) {
                    summaccept += accept[y];
                }
                output_AR[counter][1] = 100 * summaccept
                        / (int_MCMCPar_seq + ItExtra);
                // Update Iteration and counter
                Iter = Iter + int_MCMCPar_seq + ItExtra;
                counter = counter + 1;

                // System.out.println("ciao");
            }// for
            // Store Important Diagnostic information -- Probability of
            // individual crossover values
            output_CR[teller][0] = Iter;
            for (int y = 1; y < pCR.length + 1; y++) {
                output_CR[teller][y] = pCR[y - 1];
            }
            // Do this to get rounded iteration numbers
            if (teller == 1) {
                int_MCMCPar_steps += 1;
            }
            double v = (int) (0.1 * int_MCMCPar_ndraw);
            if (Iter <= v) {
                if (Extra_pCR.equals("Update")) {
                    // Update pCR values
                    AdaptpCR(CR, delta_tot, lCR);
                    pCR = pCR_out_AdaptpCR;
                    lCR = lCR_out_AdaptpCR;
                    // System.out.println("ciao");
                }
            } else {
                // See whether there are any outlier chains, and remove them to
                // current best value of X
                double[][] seq_x_MOC = new double[sequences[0].length][sequences[0][0].length];
                for (int row = 0; row < seq_x_MOC.length; row++) {
                    for (int col = 0; col < seq_x_MOC[0].length; col++) {
                        seq_x_MOC[row][col] = sequences[iloc][row][col];
                    }
                }
                double[][] hist_logp_x_ROC = new double[counter][int_MCMCPar_seq + 1];
                for (int row = 0; row < hist_logp_x_ROC.length; row++) {
                    for (int col = 0; col < hist_logp_x_ROC[0].length; col++) {
                        hist_logp_x_ROC[row][col] = hist_logp[row][col];
                    }
                }
                RemOutlierChains(X, seq_x_MOC, hist_logp_x_ROC, Iter, output_outlier);
                for (int row = 0; row < seq_x_MOC.length; row++) {
                    for (int col = 0; col < seq_x_MOC[0].length; col++) {
                        sequences[iloc][row][col] = seq_x_MOC[row][col];
                    }
                }
                for (int row = 0; row < hist_logp_x_ROC.length; row++) {
                    for (int col = 0; col < hist_logp_x_ROC[0].length; col++) {
                        hist_logp[row][col] = hist_logp_x_ROC[row][col];
                    }
                }
            }
            for (int iii = 0; iii < pCR.length; iii++) {
                System.out.print(pCR[iii] + " ");
            }
            System.out.println();
            if (Extra_pCR.equals("Update")) {
                CR = GenCR(pCR);
                for (int iii = 0; iii < pCR.length; iii++) {
                    System.out.print(pCR[iii] + " ");
                }
                System.out.println();
            }
            // Calculate Gelman and Rubin convergence diagnostic
            if (Extra_save_in_memory.equals("Yes")) {
                int start_loc = Math.max(1, ((int) (0.5 * (iloc) + 0.5))) - 1;
                int end_loc = iloc;
                // Compute the R-statistic using 50% burn-in from Sequences
                double[][][] seq_x_GE = new double[end_loc - start_loc][int_MCMCPar_n][int_MCMCPar_seq];

                int cnts = 0;
                for (int d1 = start_loc; d1 < end_loc; d1++) {
                    for (int d2 = 0; d2 < int_MCMCPar_n; d2++) {
                        for (int d3 = 0; d3 < int_MCMCPar_seq; d3++) {
                            seq_x_GE[cnts][d2][d3] = sequences[d1][d2][d3];
                        }
                    }
                    cnts += 1;
                }
                double[] rstat2 = Gelman(seq_x_GE);
                output_R_stat[teller][0] = Iter;
                for (int pp = 1; pp < output_R_stat[0].length; pp++) {
                    output_R_stat[teller][pp] = rstat2[pp - 1];
                }
            }
            System.out.println("teller" + teller);
            teller += 1;

        }// while
        // Postprocess output from DREAM before returning arguments
        int[] ii = new int[sequences.length];
        int cnt = 0;
        for (int d1 = 0; d1 < sequences.length; d1++) {
            double somma = 0;
            for (int d2 = 0; d2 < sequences[0].length; d2++) {
                // oksomma+=sequences[d1][d2][1];
                somma += sequences[d1][d2][0];
                // System.out.println(sequences[d1][d2][1]);
            }
            // for(int d2=0;d2<sequences[0].length;d2++){
            // double somma=0;
            // for(int d1=0;d1<sequences.length;d1++){
            // somma+=sequences[d1][d2][1];
            // System.out.println(sequences[d1][d2][1]);
            // }
            if (somma == 0) {
                // ii[cnt]=d1;
                ii[cnt] = d1;
                cnt += 1;
            }
        }
        if (cnt > 0) {
            int[] i = new int[cnt];
            for (int o = 0; o < cnt; o++) {
                i[o] = ii[o];
            }
            int iii = i[0] - 1;
            // int iii=i[0];
            int d22 = sequences[0].length;
            int d33 = sequences[0][0].length;
            double[][][] sequences1 = sequences;
            sequences = new double[iii][d22][d33];
            for (int d1 = 0; d1 < iii; d1++) {
                for (int d2 = 0; d2 < d22; d2++) {
                    for (int d3 = 0; d3 < d33; d3++) {
                        sequences[d1][d2][d3] = sequences1[d1][d2][d3];
                    }
                }
            }
        }
        System.out.println("t");

        // Postprocess output from DREAM before returning arguments
        int[] iiR = new int[output_R_stat.length];
        int cntR = 0;
        for (int d1 = 0; d1 < output_R_stat.length; d1++) {
            double somma = 0;
            for (int d2 = 0; d2 < output_R_stat[0].length; d2++) {
                somma += output_R_stat[d1][d2];
            }
            if (somma == 0) {
                iiR[cntR] = d1;
                cntR += 1;
            }
        }
        if (cntR > 0) {
            int[] iR = new int[cntR];
            for (int o = 0; o < cnt; o++) {
                iR[o] = iiR[o];
            }
            int iii = iR[0] - 1;
            int d22 = output_R_stat[0].length;
            double[][] output_R_stat1 = output_R_stat;
            output_R_stat = new double[iii][d22];
            for (int d1 = 0; d1 < iii; d1++) {
                for (int d2 = 0; d2 < d22; d2++) {
                    output_R_stat[d1][d2] = output_R_stat1[d1][d2];
                }
            }

        }

        // Postprocess output from DREAM before returning arguments
        int[] iiAR = new int[output_AR.length];
        int cntAR = 0;
        for (int d1 = 0; d1 < output_AR.length; d1++) {
            double somma = 0;
            for (int d2 = 0; d2 < output_AR[0].length; d2++) {
                somma += output_AR[d1][d2];
            }
            if (somma == 0) {
                iiAR[cntAR] = d1;
                cntAR += 1;
            }
        }
        if (cntAR > 0) {
            int[] iAR = new int[cntAR];
            for (int o = 0; o < cntAR; o++) {
                iAR[o] = iiAR[o];
            }
            int iii = iAR[0] - 1;
            int d22 = output_AR[0].length;
            double[][] output_AR1 = output_AR;
            output_AR = new double[iii][d22];
            for (int d1 = 0; d1 < iii; d1++) {
                for (int d2 = 0; d2 < d22; d2++) {
                    output_AR[d1][d2] = output_AR1[d1][d2];
                }
            }
        }

        // Postprocess output from DREAM before returning arguments
        int[] iiCR = new int[output_CR.length];
        int cntCR = 0;
        for (int d1 = 0; d1 < output_CR.length; d1++) {
            double somma = 0;
            for (int d2 = 0; d2 < output_CR[0].length; d2++) {
                somma += output_CR[d1][d2];
            }
            if (somma == 0) {
                iiCR[cntCR] = d1;
                cntCR += 1;
            }
        }
        if (cntCR > 0) {
            int[] iCR = new int[cntCR];
            for (int o = 0; o < cntCR; o++) {
                iCR[o] = iiCR[o];
            }
            int iii = iCR[0] - 1;
            int d22 = output_CR[0].length;
            double[][] output_CR1 = output_CR;
            output_CR = new double[iii][d22];
            for (int d1 = 0; d1 < iii; d1++) {
                for (int d2 = 0; d2 < d22; d2++) {
                    output_CR[d1][d2] = output_CR1[d1][d2];
                }
            }

        }
    }

    public void InitVariables(double MCMCPar_DEpairs, double MCMCPar_eps,
            double MCMCPar_Gamma, double MCMCPar_n, double MCMCPar_nCR,
            double MCMCPar_ndraw,
            double MCMCPar_seq, double MCMCPar_steps,
            String Extra_BoundHandling, double Extra_DRscale, 
            String Extra_reduced_sample_collection) {

        // Initialize the array that contains the history of the log_density of
        // each chain        
        int nrow_hist_logp = (int) (MCMCPar_seq);
//        hist_logp = new double[ncol_hist_logp][nrow_hist_logp + 1];
        hist_logp = new double[getMaximumIterationCount()][nrow_hist_logp + 1];
        // End Initialize the array that contains the history of the log_density
        // of each chain

        // Derive the number of elements in the output file
        int Nelem = (int) ((MCMCPar_ndraw / MCMCPar_seq) + 0.5) + 1;
        // End Derive the number of elements in the output file

        // Initialize output information -- AR
        // Matrix output_AR=new Matrix(Nelem,2);
        output_AR = new double[getMaximumIterationCount()][2];
        // System.out.println(output_AR.getRowDimension());
        // System.out.println(output_AR.getColumnDimension());
        output_AR[0][0] = (MCMCPar_seq - 1);
        output_AR[0][1] = (MCMCPar_seq - 1);

        // End Initialize output information -- AR

        // Initialize output information -- Outlier chains
        output_outlier = new double[getMaximumIterationCount()][int_MCMCPar_seq + 1];
        // End Initialize output information -- Outlier chains

        // Initialize output information -- R statistic
        output_R_stat = new double[getMaximumIterationCount()][int_MCMCPar_n + 1];
        
        int ncol_pCR;
        double pCR_value;
        if (Extra_pCR.equals("Update")) {
            // Calculate multinomial probabilities of each of the nCR CR values
            // multi=1/MCMCPar_nCR;

            pCR_value = (1 / MCMCPar_nCR);
            ncol_pCR = (int) MCMCPar_nCR;
            pCR = new double[ncol_pCR];
            for (int nc = 0; nc < ncol_pCR; nc++) {
                pCR[nc] = pCR_value;
            }
            // pCR= new Matrix((ncol_pCR),1,pCR_value);
            // End Calculate multinomial probabilities of each of the nCR CR
            // values

            // Calculate the actual CR values based on p
            CR = GenCR(pCR);
            lCR = new double[ncol_pCR];
            // End Calculate the actual CR values based on p
        }

        // Initialize output information -- N_CR
        // Matrix output_CR =new Matrix(nn, (pCR.getColumnDimension()+1), 0);
        output_CR = new double[getMaximumIterationCount()][(pCR.length + 1)];
        if (Extra_save_in_memory.equals("Yes")) {
            // Initialize Sequences with zeros
            int d1_Seq = (int) ((1.25 * Nelem) + 0.5);
            int d2_Seq = int_MCMCPar_n + 2;
            int d3_Seq = int_MCMCPar_seq;
            sequences = new double[d1_Seq][d2_Seq][d3_Seq];
        }

        // Generate the Table with JumpRates (dependent on number of dimensions
        // and number of pairs)
        Table_JumpRate = new double[int_MCMCPar_n][int_MCMCPar_DEpairs];
        // double []value=new double[int_MCMCPar_n];
        double val = 0;
        // for(int i=0;i<value.length;i++){
        // value[i]=conta;
        // conta=conta+1;
        // }
        int conta = 1;

        for (int zz = 0; zz < int_MCMCPar_DEpairs; zz++) {
            conta = 1;
            for (int kk = 0; kk < int_MCMCPar_n; kk++) {
                val = 2.38 / (Math.sqrt(2 * (zz + 1) * conta));
                Table_JumpRate[kk][zz] = val;
                conta += 1;
            }
        }

        // Initialize Iter and counter
        Iter = int_MCMCPar_seq;
        counter = 1;
        iloc = 1;
        teller = 1;
        new_teller = 1;
        // Change MCMCPar.steps to make sure to get nice iteration numbers in
        // first loop
        MCMCPar_steps = MCMCPar_steps - 1;
        int_MCMCPar_steps = int_MCMCPar_steps - 1;
    }

    public double[][] GenCR(double[] pCR) {

        // System.out.println(pcR_copy.length);
        // double[][]pcr_copy=new double[pCR.getRowDimension()][1];
        // for (int i=0;i<pcr_copy.length;i++){
        // pcr_copy[i][0]=pCR.get(i, 0);
        // }
        double[][] CRRR = new double[int_MCMCPar_seq][int_MCMCPar_steps];
        Multrnd mmm = new Multrnd();
        mmm.n = (int_MCMCPar_seq * int_MCMCPar_steps);
        double spcr = 0;
        for (int row = 0; row < pCR.length; row++) {
            spcr += pCR[row];
        }
        for (int row = 0; row < pCR.length; row++) {
            pCR[row] = pCR[row] / spcr;
        }
        mmm.p = pCR;
        // System.out.println(mmm.p.getRowDimension());
        // System.out.println(mmm.p.getColumnDimension());
        // System.out.println(mmm.p.getRowDimension()+";"+mmm.p.getColumnDimension());

        mmm.m = 1;
        mmm.process();
        double[][] XX = mmm.X;
        // System.out.println(XX.length);
        // System.out.println(XX[0].length);
        // double [][]YY=mmm.Y;

        double[] LL = new double[XX[0].length];
        int aaa = XX[0].length;
        double[] LL2 = new double[aaa + 1];

        LL2[0] = 0;
        double cum;
        for (int i = 0; i < LL.length; i++) {
            // System.out.println(XX[0][i]);
            LL[i] = XX[0][i];
            // System.out.println(LL[i]);
            cum = LL2[i];
            LL2[i + 1] = LL[i] + cum;

            // System.out.println(LL2[i]);
        }
        double[] r = new double[(int_MCMCPar_seq * int_MCMCPar_steps)];
        RandomPermutation.init(r, (int_MCMCPar_seq * int_MCMCPar_steps));
        // RandomStream rs=new MRG32k3a();
        RandomStream rs = randomstream;
        RandomPermutation.shuffle(r, rs);
        Matrix LL3 = new Matrix(r, 1);
        Matrix CCR = new Matrix(LL3.getRowDimension(), LL3.getColumnDimension());
        int int_MCMCPar_nCR = (int) MCMCPar_nCR;
        // double []LLL2= { 0 ,16 , 35 , 49};
        for (int zz = 0; zz < int_MCMCPar_nCR; zz++) {
            double i_start = LL2[zz] + 1;
            double i_end = LL2[zz + 1];
            int int_i_start = (int) i_start;
            int int_i_end = (int) i_end;
            int[] idx = new int[(int_i_end - int_i_start) + 1];
            // System.out.println("idx"+idx.length);
            // System.out.println(r.length);
            int cont = 0;
            for (int row = int_i_start - 1; row < int_i_end; row++) {
                // idx= new int [(int_i_end-int_i_start)+1];
                // System.out.println(row);
                // System.out.println(r[row]);
                idx[cont] = (int) r[row];
                cont += 1;
                // System.out.println(idx[row]);

            }
            // System.out.println(idx.length);
            for (int indice = 0; indice < idx.length; indice++) {
                // System.out.println(CCR.getRowDimension());
                // System.out.println(CCR.getColumnDimension());
                // System.out.println( idx[indice]);
                if ((zz + 1) / (MCMCPar_nCR) == 0) {
                    System.out.println(zz + "fermati");
                }
                CCR.set(0, idx[indice] - 1, (zz + 1) / (MCMCPar_nCR));
            }
            // System.out.println("ciao");

        }
        //
        int indice = 0;
        for (int indrow = 0; indrow < int_MCMCPar_seq; indrow++) {
            for (int indcol = 0; indcol < int_MCMCPar_steps; indcol++) {

                CRRR[indrow][indcol] = CCR.get(0, indice + indcol);
                // System.out.println("CCR="+CCR.get(0, indice+indcol));

            }
            indice = indice + int_MCMCPar_steps;
        }
        // System.out.println(CR.length+";"+CR[0].length);
        return CRRR;
    }

    public double[][] GenCRok(double[] pCR) {

        // System.out.println(pcR_copy.length);
        // double[][]pcr_copy=new double[pCR.getRowDimension()][1];
        // for (int i=0;i<pcr_copy.length;i++){
        // pcr_copy[i][0]=pCR.get(i, 0);
        // }
        CR = new double[int_MCMCPar_seq][int_MCMCPar_steps];
        Multrnd mmm = new Multrnd();
        mmm.n = (int_MCMCPar_seq * int_MCMCPar_steps);

        mmm.p = pCR;
        // System.out.println(mmm.p.getRowDimension());
        // System.out.println(mmm.p.getColumnDimension());
        // System.out.println(mmm.p.getRowDimension()+";"+mmm.p.getColumnDimension());

        mmm.m = 1;
        mmm.process();
        double[][] XX = mmm.X;
        // System.out.println(XX.length);
        // System.out.println(XX[0].length);
        double[][] YY = mmm.Y;

        double[] LL = new double[XX[0].length];
        int aaa = XX[0].length;
        double[] LL2 = new double[aaa + 1];

        LL2[0] = 0;
        double cum;
        for (int i = 0; i < LL.length; i++) {
            // System.out.println(XX[0][i]);
            LL[i] = XX[0][i];
            // System.out.println(LL[i]);
            cum = LL2[i];
            LL2[i + 1] = LL[i] + cum;

            // System.out.println(LL2[i]);
        }
        double[] r = new double[(int_MCMCPar_seq * int_MCMCPar_steps)];
        RandomPermutation.init(r, (int_MCMCPar_seq * int_MCMCPar_steps));
        // RandomStream rs=new MRG32k3a();
        RandomStream rs = randomstream;
        RandomPermutation.shuffle(r, rs);
        Matrix LL3 = new Matrix(r, 1);
        Matrix CCR = new Matrix(LL3.getRowDimension(), LL3.getColumnDimension());
        int int_MCMCPar_nCR = (int) MCMCPar_nCR;
        // double []LLL2= { 0 ,16 , 35 , 49};
        for (int zz = 0; zz < int_MCMCPar_nCR; zz++) {
            double i_start = LL2[zz] + 1;
            double i_end = LL2[zz + 1];
            int int_i_start = (int) i_start;
            int int_i_end = (int) i_end;
            int[] idx = new int[(int_i_end - int_i_start) + 1];
            // System.out.println("idx"+idx.length);
            // System.out.println(r.length);
            int cont = 0;
            for (int row = int_i_start - 1; row < int_i_end; row++) {
                // idx= new int [(int_i_end-int_i_start)+1];
                // System.out.println(row);
                // System.out.println(r[row]);
                idx[cont] = (int) r[row];
                cont += 1;
                // System.out.println(idx[row]);

            }
            // System.out.println(idx.length);
            for (int indice = 0; indice < idx.length; indice++) {
                // System.out.println(CCR.getRowDimension());
                // System.out.println(CCR.getColumnDimension());
                // System.out.println( idx[indice]);
                if ((zz + 1) / (MCMCPar_nCR) == 0) {
                    System.out.println(zz + "fermati");
                }
                CCR.set(0, idx[indice] - 1, (zz + 1) / (MCMCPar_nCR));
            }
            // System.out.println("ciao");

        }
        //
        int indice = 0;
        for (int indrow = 0; indrow < int_MCMCPar_seq; indrow++) {
            for (int indcol = 0; indcol < int_MCMCPar_steps; indcol++) {

                CR[indrow][indcol] = CCR.get(0, indice + indcol);
                // System.out.println("CCR="+CCR.get(0, indice+indcol));
            }
            indice = indice + int_MCMCPar_steps;
        }
        // System.out.println(CR.length+";"+CR[0].length);
        return CR;
    }

    public double[][] LHS(double[] xmin, double[] xmax, int nsample) {
        // Latin Hypercube sampling       
        double[] rr = new double[(nsample)];
        double[][] s = new double[nsample][n];
        for (int j = 0; j < n; j++) {
            RandomPermutation.init(rr, nsample);
            RandomPermutation.shuffle(rr, randomstream);
            for (int kk = 0; kk < nsample; kk++) {
                double P = (rr[kk] - Math.random()) / nsample;
                s[kk][j] = xmin[j] + P * (xmax[j] - xmin[j]);
            }
        }
        return s;
    }

    public double calcLogLikelihood(double y){
        // Calculate the parameters in the exponential power density function of
        // Box and Tiao (1973)
        double alpha1 = 3.0 * (1.0 + MCMCPar_Gamma) / 2.0;
        double alpha2 = 3*alpha1;

        double A1 = Math.exp(Num.lnGamma(alpha1));
        double A2 = Math.exp(Num.lnGamma(alpha2));
        
        double exp1 = 1. / (1. + MCMCPar_Gamma);
        
        double MCMCPar_Wb = Math.sqrt(A1) / ((1. + MCMCPar_Gamma) * (Math.pow(A2, 1.5)));
        double MCMCPar_Cb = Math.pow((A1 / A2), exp1);
        
        double sum1 = Math.log((MCMCPar_Wb / sigma));
        
        double val = Math.abs((-4.-y) / sigma);
        double sum2 = Math.pow(val, (2.0 / (1.0 + MCMCPar_Gamma)));

        return sum1 - MCMCPar_Cb * sum2;
    }
    
    private double[][] computeDensity(double[][] x) throws SampleLimitException, ObjectiveAchievedException{
        // This function computes the density of each x value
        // Sequential evaluation
        log_p_CD = new double[x.length];
        p_CD = new double[log_p_CD.length][2];

        for (int ii = 0; ii < x.length; ii++) {
            SampleFactory.Sample s = this.getSample(x[ii]);
            
            log_p_CD[ii] = s.F()[0];
            if (isTransformObjectiveIntoLogLikelihood()){
                log_p_CD[ii] = calcLogLikelihood(log_p_CD[ii]);
            }                                  
            System.out.println("Testing:" + Arrays.toString(x[ii]) + "\t\tgetting " + log_p_CD[ii] + "\t" + s.F()[0]);
            
            p_CD[ii][0] = log_p_CD[ii];
            p_CD[ii][1] = ii;
        }

        return X;
    }

    public double[][][] InitSequences(double[][] X) {
        // Initialize sequences
        for (int qq = 0; qq < int_MCMCPar_seq; qq++) {
            // Initialize Sequences
            for (int dim2seq = 0; dim2seq < int_MCMCPar_n + 2; dim2seq++) {
                sequences[0][dim2seq][qq] = X[qq][dim2seq];
            }
        }
        return sequences;
    }

    public double[] Gelman2(double[][][] seq) {
        System.out.println();
        // Compute the dimensions of Sequences
        double[] Rstat = new double[int_MCMCPar_n];
        int n = seq.length;
        int nrY = seq[0].length;
        int m = seq[0][0].length;

        double[][] meanSeq;
        if (n < 10) {
            // Set the R-statistic to a large value
            for (int i = 0; i < Rstat.length; i++) {
                Rstat[i] = -2;
            }
        } else {
            double[][] meanSeq_tra = new double[nrY][m];
            meanSeq = new double[nrY][m];

            for (int dim2seq = 0; dim2seq < nrY; dim2seq++) {
                for (int dim3seq = 0; dim3seq < m; dim3seq++) {
                    double somma = 0;
                    for (int dim1seq = 0; dim1seq < n; dim1seq++) {
                        // System.out.println(seq[dim1seq][dim2seq][dim3seq]+" ");
                        somma = somma + seq[dim1seq][dim2seq][dim3seq];
                    }
                    meanSeq_tra[dim2seq][dim3seq] = somma / n;
                }
            }

            for (int row = 0; row < meanSeq.length; row++) {
                for (int col = 0; col < meanSeq[0].length; col++) {
                    meanSeq[row][col] = meanSeq_tra[col][row];
                }
            }

            // Step 1: Determine the variance between the sequence means
            double[] var = new double[nrY];
            double[] B = new double[nrY];
            double[] vetmedia = new double[nrY];

            for (int col = 0; col < meanSeq[0].length; col++) {
                double somma = 0;
                for (int row = 0; row < meanSeq.length; row++) {
                    somma = somma + (meanSeq[row][col]);
                }
                vetmedia[col] = somma / meanSeq.length;
            }
            for (int col = 0; col < meanSeq[0].length; col++) {
                double somma = 0;
                for (int row = 0; row < meanSeq.length; row++) {
                    somma = somma + (meanSeq[row][col] - vetmedia[col])
                            * (meanSeq[row][col] - vetmedia[col]);
                }
                var[col] = somma / (meanSeq.length - 1.0);
                B[col] = var[col] * (n);
            }

            // Step 2: Compute the variance of the various sequences
            double[][] varSeq = new double[m][nrY];
            double[][] sommatemp = new double[m][nrY];
            for (int dim3 = 0; dim3 < m; dim3++) {
                for (int dim2 = 0; dim2 < nrY; dim2++) {
                    double s = 0;
                    // System.out.println("inizio");
                    for (int dim1 = 0; dim1 < n; dim1++) {
                        // System.out.println(seq[dim1][dim2][dim3]);
                        s += seq[dim1][dim2][dim3];
                    }
                    System.out.println();
                    sommatemp[dim2][dim3] = s / n;
                }
            }
            for (int dim3 = 0; dim3 < m; dim3++) {
                for (int dim2 = 0; dim2 < nrY; dim2++) {
                    double s = 0;
                    for (int dim1 = 0; dim1 < n; dim1++) {
                        s += (seq[dim1][dim2][dim3] - sommatemp[dim3][dim2])
                                * (seq[dim1][dim2][dim3] - sommatemp[dim3][dim2]);
                        // System.out.println(seq[dim1][dim2][dim3]);
                        // System.out.println(sommatemp[dim3][dim2]);
                    }
                    varSeq[dim2][dim3] = s / (n - 1);
                }
            }

            // Step 2: Calculate the average of the within sequence variances
            double[] W = new double[nrY];

            for (int i = 0; i < nrY; i++) {
                double somma = 0;
                for (int j = 0; j < m; j++) {
                    somma = somma + varSeq[j][i];
                }
                W[i] = somma / m;
            }

            // Step 4: Estimate the target variance
            double[] sigma2 = new double[W.length];
            for (int t = 0; t < W.length; t++) {
                sigma2[t] = ((n - 1.0) / n) * W[t] + B[t] / n;
            }

            // double []R_stat=new double [W.length];
            for (int t = 0; t < W.length; t++) {
                double ter1 = (m + 1.0) / m * sigma2[t] / W[t] - (n - 1.0) / m / n;
                Rstat[t] = Math.sqrt(ter1);
            }

            // Step 5: Compute the R-statistic
            System.out.println("ok");
        }
        // Step 4: Estimate the target variance
        // for (int i=0;i<)
        // double sigma2 = ((n - 1)/n) * W + (1/n) * B;
        return Rstat;
    }

    public void GetLocation(double[][] XX) {
        // System.out.println(XX.length);
        // System.out.println(XX[0].length);
        // Extracts the current location and density of the chain
        // First get the current location
        x_old = new double[int_MCMCPar_seq][int_MCMCPar_n];
        for (int i = 0; i < int_MCMCPar_seq; i++) {
            for (int j = 0; j < int_MCMCPar_n; j++) {
                x_old[i][j] = XX[i][j];
            }
        }
        // Then get the current density
        p_old = new double[int_MCMCPar_seq];
        for (int i = 0; i < int_MCMCPar_seq; i++) {
            p_old[i] = XX[i][int_MCMCPar_n + 1];

        }
        log_p_old = new double[int_MCMCPar_seq];
        for (int i = 0; i < int_MCMCPar_seq; i++) {
            log_p_old[i] = XX[i][int_MCMCPar_n + 1];

        }
    }

    public void offde2(double[][] XXXX) {
        double[][] eps = new double[int_MCMCPar_seq][int_MCMCPar_n];
        // RandomStream rrs=new MRG32k3a();
        RandomStream rrs = randomstream;
        NormalGen nn = new NormalGen(rrs);
        for (int i = 0; i < int_MCMCPar_seq; i++) {
            for (int j = 0; j < int_MCMCPar_n; j++) {
                eps[i][j] = Math.pow(10, (-6.0)) * nn.nextDouble();
            }
        }
        // If not a delayed rejection step --> generate proposal with DE
        // if strcmp(DR,'No');
        // Determine which sequences to evolve with what DE strategy
        DEStrategy();
        double[][] dummy = new double[int_MCMCPar_seq - 1][int_MCMCPar_seq];
        int[][] tt = new int[int_MCMCPar_seq - 1][int_MCMCPar_seq];

        double[] a = new double[int_MCMCPar_seq - 1];
        for (int col = 0; col < dummy[0].length; col++) {
            for (int i = 0; i < a.length; i++) {
                a[i] = Math.random();
            }
            // double[] a={78.1,0,2.4,5};
            double temp = 0;
            int[] b = new int[int_MCMCPar_seq - 1];
            int kkk = 0;
            for (int x = 0; x < b.length; x++) {
                b[x] = kkk;
                kkk += 1;
            }
            kkk = 0;
            for (int j = 0; j < a.length; j++) {
                for (int i = j; i < a.length; i++) {
                    if (a[j] > a[i]) {
                        temp = a[j];
                        a[j] = a[i];
                        a[i] = temp;
                        int temp2 = b[j];
                        b[j] = b[i];
                        b[i] = temp2;
                    }
                }
            }
            for (int row = 0; row < dummy.length; row++) {
                // System.out.println("row="+row);
                // System.out.println("col="+col);
                // System.out.println(b[row]);
                dummy[row][col] = a[row];
                tt[row][col] = b[row];
            }
        }
        // Generate uniform random numbers for each chain to determine which
        // dimension to update

        double[][] D = new double[int_MCMCPar_seq][int_MCMCPar_n];
        for (int row = 0; row < int_MCMCPar_seq; row++) {
            for (int col = 0; col < int_MCMCPar_n; col++) {
                D[row][col] = Math.random();
            }
        }
        // Ergodicity for each individual chain
        double[][] noise_x = new double[int_MCMCPar_seq][int_MCMCPar_n];
        for (int row = 0; row < int_MCMCPar_seq; row++) {
            for (int col = 0; col < int_MCMCPar_n; col++) {
                noise_x[row][col] = MCMCPar_eps * (2.0 * Math.random() - 1.0);
            }
        }

        // Initialize the delta update to zero
        double[][] delta_x = new double[int_MCMCPar_seq][int_MCMCPar_n];

        // Each chain evolves using information from other chains to create
        // offspring
        for (int qq = 0; qq < int_MCMCPar_seq; qq++) {
            // Define ii and remove current member as an option
            double[] ii = new double[int_MCMCPar_seq];
            for (int i = 0; i < int_MCMCPar_seq; i++) {
                ii[i] = 1;
            }
            ii[qq] = 0;
            int[] idxx = new int[int_MCMCPar_seq];
            int cnt = 0;
            for (int i = 0; i < int_MCMCPar_seq; i++) {
                // if(i!=qq){
                if (ii[i] > 0) {
                    idxx[cnt] = i;
                    cnt += 1;
                }
            }
            int[] idx = new int[cnt];
            for (int j = 0; j < cnt; j++) {
                idx[j] = idxx[j];
            }
            // randomly select two members of ii that have value == 1
            int[] vet_tt = new int[2 * DEversion[qq]];
            for (int i = 0; i < vet_tt.length; i++) {
                vet_tt[i] = tt[i][qq];
            }
            int[] rr = new int[vet_tt.length];
            for (int i = 0; i < vet_tt.length; i++) {
                rr[i] = idx[vet_tt[i]];
            }
            // --- WHICH DIMENSIONS TO UPDATE? DO SOMETHING WITH CROSSOVER ----
            int[] i_temp = new int[int_MCMCPar_n];
            int cont = 0;
            for (int l = 0; l < int_MCMCPar_n; l++) {
                if (D[qq][l] > (1.0 - CR[qq][gen_number])) {
                    i_temp[cont] = l;
                    cont += 1;
                }
            }
            int[] i = null;
            // Update at least one dimension
            if (cont == 0) {
                int[] it = new int[(int_MCMCPar_n)];
                RandomPermutation.init(it, (int_MCMCPar_n));
                RandomStream rs = new MRG32k3a();
                // RandomStream rs=randomstream;
                RandomPermutation.shuffle(it, rs);
                i = new int[1];
                i[cont] = it[0] - 1;
                // cont+=1;
            }
            if (cont > 0) {
                i = new int[cont];
                for (int u = 0; u < cont; u++) {
                    i[u] = i_temp[u];
                }
            }

            int NrDim = i.length;
            // int[] i=new int[NrDim];
            // for (int rrr=0;rrr<cont;rrr++){
            // i[rrr]=i_temp[rrr];
            // }
            double[] delta;
            double n = Math.random();
            double m = 4.0 / 5.0;
            if (n < m) {

                // Lookup Table
                double JumpRate = Table_JumpRate[NrDim - 1][0];

                // Produce the difference of the pairs used for population
                // evolution
                int index_row_1_end = DEversion[qq];
                int index_row_2_start = DEversion[qq];
                int index_row_2_end = 2 * DEversion[qq];
                int[] rr1 = new int[index_row_1_end];
                int[] rr2 = new int[index_row_2_end - index_row_2_start];
                for (int k = 0; k < index_row_1_end; k++) {
                    rr1[k] = rr[k];
                }
                int row_cnt = 0;
                for (int k = index_row_2_start; k < index_row_2_end; k++) {
                    rr2[row_cnt] = rr[k];
                    row_cnt = 1;
                }
                double[][] X1 = new double[rr1.length][int_MCMCPar_n];
                double[][] X2 = new double[rr2.length][int_MCMCPar_n];
                for (int row = 0; row < X1.length; row++) {
                    for (int col = 0; col < X1[0].length; col++) {
                        X1[row][col] = XXXX[rr1[row]][col];
                    }
                }
                for (int row = 0; row < X2.length; row++) {
                    for (int col = 0; col < X2[0].length; col++) {
                        X2[row][col] = XXXX[rr2[row]][col];
                    }
                }

                // double []X1=new double [index_row_1_end];
                delta = new double[X1[0].length];

                for (int col = 0; col < X1[0].length; col++) {
                    double somma = 0;
                    for (int row = 0; row < X1.length; row++) {
                        somma = somma + (X1[row][col] - X2[row][col]);
                    }
                    delta[col] = somma;
                }

                // Then fill update the dimension
                for (int contcol = 0; contcol < i.length; contcol++) {
                    delta_x[qq][i[contcol]] = (1.0 + noise_x[qq][i[contcol]])
                            * JumpRate * delta[i[contcol]];
                }

                // System.out.println("ciao");
            } else {
                // Set the JumpRate to 1 and overwrite CR and DEversion
                double JumpRate = 1.0;
                CR[qq][gen_number] = -1.0;
                delta = new double[int_MCMCPar_n];
                // Compute delta from one pair
                for (int col = 0; col < int_MCMCPar_n; col++) {
                    delta[col] = XXXX[rr[0]][col] - XXXX[rr[1]][col];
                }
                for (int contcol = 0; contcol < delta_x[0].length; contcol++) {
                    delta_x[qq][contcol] = JumpRate * delta[contcol];
                }
            }
            double ssquare = 0;
            for (int v = 0; v < delta_x[0].length; v++) {
                ssquare = (delta_x[qq][v] * delta_x[qq][v]) + ssquare;
            }
            //CF: NOT NICE: mat must be square, that means n = nChains .. cannot be archieved wenn n = 1, thus checking here square condition 
            if (ssquare == 0 && x_old.length == x_old[0].length) {
                double[][] mat = new double[x_old.length][x_old[0].length];
                for (int row = 0; row < mat.length; row++) {
                    for (int col = 0; col < mat[0].length; col++) {
                        mat[row][col] = x_old[row][col];
                    }
                }
                double[] arr1 = new double[mat.length];
                double[] arr2 = new double[mat.length];
                double[][] mat_cov = new double[mat.length][mat[0].length];

                for (int col = 0; col < mat[0].length; col++) {
                    for (int row1 = 0; row1 < mat.length; row1++) {
                        arr1[row1] = mat[row1][col];
                    }

                    DoubleArrayList arrayuno = new DoubleArrayList(arr1);
                    for (int col2 = 0; col2 < mat[0].length; col2++) {
                        for (int row = 0; row < mat.length; row++) {
                            arr2[row] = mat[row][col2];
                        }
                        DoubleArrayList arraydue = new DoubleArrayList(arr2);
                        if (col == col2) {
                            mat_cov[col][col2] = Descriptive.covariance(
                                    arrayuno, arraydue) + Math.pow(10, -5.0);
                        } else {
                            mat_cov[col][col2] = Descriptive.covariance(
                                    arrayuno, arraydue);
                        }
                        // System.out.println("ciao");
                        // System.out.println("ciao");
                    }
                }
                CholeskyDecomposition dec;
                Matrix chol = new Matrix(mat_cov);
                dec = chol.chol();
                // Matrix uff=dec.getL();
                // RandomStream rrrs=new MRG32k3a();
                RandomStream rrrs = randomstream;
                NormalGen nnn = new NormalGen(rrrs);
                double[][] R = new double[x_old.length][x_old[0].length];
                for (int row = 0; row < R.length; row++) {
                    for (int col = 0; col < R[0].length; col++) {
                        R[row][col] = (2.38 / Math.sqrt(MCMCPar_n))
                                * dec.getL().get(row, col);
                    }
                }
                double[] f = new double[int_MCMCPar_n];
                for (int row = 0; row < int_MCMCPar_n; row++) {
                    f[row] = nnn.nextDouble();
                }
                // for(int contcol=0; contcol<int_MCMCPar_n;contcol++){
                // double somma=0;
                // for (int oo=0;oo<R[0].length;oo++){
                // somma+=R[oo][contcol]*f[oo];
                // }
                // delta_x[qq][contcol]=somma;
                // }
                for (int contcol = 0; contcol < int_MCMCPar_n; contcol++) {
                    double somma = 0;
                    for (int oo = 0; oo < R[0].length; oo++) {
                        somma += R[contcol][oo] * f[oo];
                    }
                    delta_x[qq][contcol] = somma;
                }
                System.out.println("Cholewsky decomposition");
            }
            // Attento a che entra nel off de: CR per lui e solo un vettore,
            // table jump vedi cos'e.
        }

        // for(int row=0;row<delta_x.length;row++){
        // for (int col=0;col<delta_x[0].length;col++){
        // System.out.print(delta_x[row][col]+" ");
        // }
        // System.out.println();
        // }
        // Update x_old with delta_x and eps;

        double[][] x_new1 = new double[x_old.length][x_old[0].length];
        for (int row = 0; row < x_new1.length; row++) {
            for (int col = 0; col < x_new1[0].length; col++) {
                x_new1[row][col] = x_old[row][col] + delta_x[row][col]
                        + eps[row][col];
            }
        }
        // x_new = x_old + delta_x + eps;
        x_new = ReflectBounds(x_new1, ParRange_maxn, ParRange_minn);
        // x_new=xnew_1;
        // for (int i=0;i<x_new.length;i++){
        // for (int j=0;j<x_new[0].length;j++){
        // System.out.print(x_old[i][j]-x_new[i][j]+" ");
        // }
        // System.out.println();
        // }
        // System.out.println("offde");
    }

    public void DEStrategy() {
        // Determine which sequences to evolve with what DE strategy
        // Determine probability of selecting a given number of pairs;
        double[] p_pair = new double[int_MCMCPar_DEpairs];
        for (int i = 0; i < int_MCMCPar_DEpairs; i++) {
            p_pair[i] = 1 / MCMCPar_DEpairs;
        }
        double cumsum = 0;
        double[] cump_pair = new double[int_MCMCPar_DEpairs + 1];
        cump_pair[0] = 0;
        for (int i = 0; i < int_MCMCPar_DEpairs; i++) {
            cump_pair[i + 1] = p_pair[i] + cumsum;
            cumsum += cump_pair[i];
        }

        // Generate a random number between 0 and 1
        double[] Z = new double[int_MCMCPar_seq];
        for (int i = 1; i < int_MCMCPar_seq; i++) {
            Z[i] = Math.random();
        }

        DEversion = new int[Z.length];
        for (int i = 0; i < Z.length; i++) {
            int cnt = 0;
            int[] z = new int[cump_pair.length];
            for (int j = 0; j < cump_pair.length; j++) {
                cnt = 0;
                if (Z[i] > cump_pair[j]) {
                    z[j] = j;
                    cnt = j;
                } else {
                    z[j] = 0;
                }
            }
            DEversion[i] = z[cnt] + 1;
            // System.out.print(DEversion[i]+" ");
        }

        // Select number of pairs
        // for qq = 1:MCMCPar.seq,
        // z = find(Z(qq,1)>p_pair); DEversion(qq,1) = z(end);
        // end;
    }

    public double[][] ReflectBounds(double[][] neww, double[] ParRange_maxn,
            double[] ParRange_minn) {
        // Checks the bounds of the parameters
        // First determine the size of new
        // int nmbOfIndivs=neww.length;
        // int Dim=neww[0].length;
        double[][] y = neww;
        for (int row = 0; row < neww.length; row++) {
            for (int col = 0; col < neww[0].length; col++) {
                if (y[row][col] < ParRange_minn[col]) {
                    y[row][col] = 2 * ParRange_minn[col] - y[row][col];
                }
                if (y[row][col] > ParRange_maxn[col]) {
                    y[row][col] = 2 * ParRange_maxn[col] - y[row][col];
                }
            }
        }
        for (int row = 0; row < neww.length; row++) {
            for (int col = 0; col < neww[0].length; col++) {
                if (y[row][col] < ParRange_minn[col]) {
                    y[row][col] = ParRange_minn[col] + Math.random()
                            * (ParRange_maxn[col] - ParRange_minn[col]);
                }
                if (y[row][col] > ParRange_maxn[col]) {
                    y[row][col] = ParRange_minn[col] + Math.random()
                            * (ParRange_maxn[col] - ParRange_minn[col]);
                }
            }
        }
        return y;
    }

    public void metrop(double[][] xxxx, double[] px, double[] logpx,
            double[][] xold, double[] pxold, double[] logpxold) {
        // Metropolis rule for acceptance or rejection

        // Calculate the number of Chains
        int NrChains = xxxx.length;
        // First set newgen to the old positions in X
        // newgen = [x_old p_old log_p_old];
        newgen = new double[xold.length][xold[0].length + 2];
        for (int row = 0; row < newgen.length; row++) {
            for (int col = 0; col < xold[0].length; col++) {
                newgen[row][col] = xold[row][col];
            }
            newgen[row][xold[0].length] = pxold[row];
            newgen[row][xold[0].length + 1] = logpxold[row];
        }
        // And initialize accept with zeros
        accept = new double[NrChains];

        double[] alpha = new double[px.length];

        for (int row = 0; row < px.length; row++) {
            double g = Math.exp(px[row] - pxold[row]);
            // if(g<1.0){
            // alpha[row]=g;
            // }
            // else{
            // alpha[row]=1.0;
            // }
            alpha[row] = Math.min(g, 1.0);
        }
        // Generate random numbers
        double[] ZZZ = new double[NrChains];
        for (int row = 0; row < ZZZ.length; row++) {
            ZZZ[row] = Math.random();
        }
        // Find which alpha's are greater than Z
        int[] idxx = new int[px.length];
        int ccc = 0;
        for (int row = 0; row < ZZZ.length; row++) {
            if (alpha[row] > ZZZ[row]) {
                idxx[ccc] = row;
                ccc += 1;
            }
        }
        int[] idx = new int[ccc];
        for (int c = 0; c < ccc; c++) {
            idx[c] = idxx[c];
        }
        // And update these chains
        for (int row = 0; row < idx.length; row++) {
            for (int col = 0; col < xold[0].length; col++) {
                newgen[idx[row]][col] = xxxx[idx[row]][col];
            }
            newgen[idx[row]][xold[0].length] = px[idx[row]];
            newgen[idx[row]][xold[0].length + 1] = logpx[idx[row]];
            accept[idx[row]] = 1;
        }
        // System.out.println("ciao");
    }

    public double[] CalcDelta(double[] delta_tot_result_CD,
            double[] delta_normX_CD, double[] CR_vet_CD) {
        // Calculate total normalized Euclidean distance for each crossover
        // value
        // Derive sum_p2 for each different CR value
        for (int zz = 0; zz < int_MCMCPar_nCR; zz++) {
            // Find which chains are updated with zz/MCMCPar.nCR
            int[] idx1 = new int[CR_vet_CD.length];
            int cnt = 0;
            for (int c = 0; c < CR_vet_CD.length; c++) {
                double h = CR_vet_CD[c];
                double hh = (zz + 1.0) / int_MCMCPar_nCR;
                if (h == hh) {
                    idx1[cnt] = c;
                    cnt += 1;
                }
            }
            int[] idx = new int[cnt];
            for (int c = 0; c < cnt; c++) {
                idx[c] = idx1[c];
            }
            // Add the normalized squared distance tot the current delta_tot;
            double sum_deltanormx = 0;
            for (int o = 0; o < idx.length; o++) {
                sum_deltanormx += delta_normX_CD[idx[o]];
            }
            delta_tot_result_CD[zz] = delta_tot_result_CD[zz] + sum_deltanormx;
        }
        return delta_tot_result_CD;
    }

    public void AdaptpCR(double[][] CR_in, double[] delta_tot_in,
            double[] lCRold_in) {
        // Updates the probabilities of the various crossover values
        lCR_out_AdaptpCR = new double[lCRold_in.length];
        pCR_out_AdaptpCR = new double[pCR.length];
        double[] CR_vect = new double[CR_in.length * CR_in[0].length];
        for (int qq = 0; qq < CR_in[0].length; qq++) {
            for (int ww = 0; ww < CR_in.length; ww++) {
                // System.out.println("crvect"+(ww+qq*(CR_in.length)));
                // System.out.println("cr ind"+ww+";"+qq);
                CR_vect[ww + qq * (CR_in.length)] = CR_in[ww][qq];
            }
            // System.out.println("ciao");
        }
        // Determine lCR
        for (int zz = 0; zz < int_MCMCPar_nCR; zz++) {
            // Determine how many times a particular CR value is used

//            double[] idx1 = new double[CR_vect.length];
            int lll = 0;
            for (int qq = 0; qq < CR_vect.length; qq++) {
                double aa = CR_vect[qq];
                double aaa = (double) (zz + 1.0) / int_MCMCPar_nCR;
                if (aa == aaa) {
//                    idx1[lll] = zz;
                    lll += 1;
                }

            }
            // This is used to weight delta_tot
            lCR_out_AdaptpCR[zz] = lCRold_in[zz] + lll;
        }
        // Adapt pCR using information from averaged normalized jumping distance
        double sdeltatot = 0;
        for (int s = 0; s < delta_tot_in.length; s++) {
            sdeltatot += delta_tot_in[s];
        }
        double sumpCR = 0;
        for (int mmm = 0; mmm < pCR_out_AdaptpCR.length; mmm++) {
            pCR_out_AdaptpCR[mmm] = int_MCMCPar_seq
                    * (delta_tot_in[mmm] / lCR_out_AdaptpCR[mmm]) / sdeltatot;
            sumpCR += pCR_out_AdaptpCR[mmm];
        }
        for (int mmm = 0; mmm < pCR_out_AdaptpCR.length; mmm++) {
            pCR_out_AdaptpCR[mmm] = pCR_out_AdaptpCR[mmm] / sumpCR;
        }
    }

    public void RemOutlierChains(double[][] Mat_X, double[][] Mat_Sequences,
            double[][] hist_logp_inROC, int Iter_in_ROC,
            double[][] output_outlier_in_ROC) {
        // Finds outlier chains and removes them when needed

        // Determine the number of elements of L_density
        int idx_end = hist_logp_inROC.length;
        int idx_start = (int) ((0.5 * idx_end) + 0.5);

        // Then determine the mean log density of the active chains
        double[] mean_hist_logp = new double[int_MCMCPar_seq];
        int cnt = 0;

        for (int col = 1; col < int_MCMCPar_seq + 1; col++) {
            double somma = 0;
            for (int row = idx_start; row < idx_end; row++) {
                somma = somma + hist_logp_inROC[row][col];
            }
            mean_hist_logp[cnt] = somma / (idx_end - idx_start);
            cnt += 1;
        }
        double UpperRange;
        int Nid = 0;
        int[] chain_id = new int[1];
        // Check whether any of these active chains are outlier chains
        if (MCMCPar_outlierTest.equals("IQR_test")) {
            // double []mean_hist_logp2=bubbleSort1(mean_hist_logp);
            // for (int i=0;i<mean_hist_logp2.length;i++){
            // mean_hist_logp3[i]=mean_hist_logp2[mean_hist_logp2.length-i-1];
            // }

            // double []mean_hist_logp2={1,2,3,4,5,6,7,8};
            // Derive the upper and lower quantile of the data
            // DoubleArrayList al=new DoubleArrayList(mean_hist_logp2);
            // double []perc={0.25-0.035, 0.75-0.035};
            double[] pppp = {25, 75};
            double[] rrrrr = percetili(mean_hist_logp, pppp);
            // DoubleArrayList per=new DoubleArrayList(perc);
            // DoubleArrayList Q=new DoubleArrayList();
            // Q=Descriptive.quantiles(al, per);
            // double Q1=Q.get(1);
            // double Q3=Q.get(0);
            // Derive the Inter quartile range
            double Q1 = rrrrr[1];
            double Q3 = rrrrr[0];
            double IQR = Q1 - Q3;
            // Compute the upper range -- to detect outliers
            UpperRange = Q3 - 2 * IQR;

            // See whether there are any outlier chain
            int[] chain_id1 = new int[mean_hist_logp.length];
            int cnn = 0;
            for (int u = 0; u < mean_hist_logp.length; u++) {
                if (mean_hist_logp[u] < UpperRange) {
                    chain_id1[cnn] = u;
                    cnn += 1;
                }
            }
            chain_id = new int[cnn];
            for (int u = 0; u < cnn; u++) {
                chain_id[u] = chain_id1[u];
            }
            Nid = chain_id.length;

        }
        if (Nid > 0) {
            for (int qq = 0; qq < Nid; qq++) {
                // Draw random other chain -- cannot be the same as current
                // chain
                double max_mean_hist_logp = mean_hist_logp[0];
                for (int u = 1; u < mean_hist_logp.length; u++) {
                    if (mean_hist_logp[u] > max_mean_hist_logp) {
                        max_mean_hist_logp = mean_hist_logp[u];
                    }
                }
                int[] r_idx1 = new int[hist_logp_inROC.length];
                int cnn = 0;
                for (int u = 0; u < mean_hist_logp.length; u++) {
                    if (mean_hist_logp[u] == max_mean_hist_logp) {
                        r_idx1[cnn] = u;
                        cnn += 1;
                    }
                }
                int r_idx = r_idx1[0];
                // Added -- update hist_logp -- chain will not be considered as
                // an outlier chain then
                for (int row = 0; row < hist_logp_inROC.length; row++) {
                    hist_logp_inROC[row][chain_id[qq]] = hist_logp_inROC[row][r_idx];
                }
                // Jump outlier chain to r_idx -- Sequences
                for (int r = 0; r < int_MCMCPar_n + 2; r++) {
                    Mat_Sequences[r][chain_id[qq]] = Mat_X[r_idx][r];
                    Mat_X[chain_id[qq]][r] = Mat_X[r_idx][r];
                }

                output_outlier_in_ROC[aggiorna_outlier][0] = Iter;
                output_outlier_in_ROC[aggiorna_outlier][qq + 1] = chain_id[qq];

                if (output_outlier_in_ROC[aggiorna_outlier][qq + 1] > 0) {
                    System.out.print("outlier= "
                            + output_outlier_in_ROC[aggiorna_outlier][0] + " "
                            + output_outlier_in_ROC[aggiorna_outlier][qq + 1]);
                }

                System.out.println();
                aggiorna_outlier = 1 + aggiorna_outlier;
                // Jump outlier chain to r_idx -- X
            }
        }

    }

    public double[] Gelman(double[][][] seq) {
        System.out.println();
        // Compute the dimensions of Sequences
        double[] Rstat = new double[int_MCMCPar_n];
        int n = (seq.length);
        int nrY = (seq[0].length);
        int m = (seq[0][0].length);

        double[][] meanSeq;
        if (n < 10) {
            // Set the R-statistic to a large value
            for (int i = 0; i < Rstat.length; i++) {
                Rstat[i] = -2.0;
            }
        } else {
            // Step 1: Determine the sequence means
            //double[][] meanSeq_tra = new double[nrY][m];
            //meanSeq = new double[nrY][m];
            meanSeq = new double[nrY][m];
            for (int dim2seq = 0; dim2seq < nrY; dim2seq++) {
                for (int dim3seq = 0; dim3seq < m; dim3seq++) {
                    double somma = 0;
                    for (int dim1seq = 0; dim1seq < n; dim1seq++) {
                        // System.out.println(seq[dim1seq][dim2seq][dim3seq]+" ");
                        somma = somma + seq[dim1seq][dim2seq][dim3seq];
                    }
                    //meanSeq_tra[dim2seq][dim3seq] = somma / n;
                    meanSeq[dim2seq][dim3seq] = somma / n;
                }
            }

            //CF: this works not fine .. 
            /*for (int row = 0; row < meanSeq.length; row++) {
                for (int col = 0; col < meanSeq[0].length; col++) {
                    meanSeq[row][col] = meanSeq_tra[col][row];
                }
            }*/

            // Step 1: Determine the variance between the sequence means
            //double[] var = new double[nrY];
            //double[] B = new double[nrY];
            //CF changed .. 
            //double[] vetmedia = new double[nrY];
            double[] var = new double[m];
            double[] vetmedia = new double[m];
            double[] B = new double[m];
            for (int col = 0; col < meanSeq[0].length; col++) {
                double somma = 0;
                for (int row = 0; row < meanSeq.length; row++) {
                    somma = somma + (meanSeq[row][col]);
                }
                vetmedia[col] = somma / meanSeq.length;
            }
            for (int col = 0; col < meanSeq[0].length; col++) {
                double somma = 0;
                for (int row = 0; row < meanSeq.length; row++) {
                    somma = somma + (meanSeq[row][col] - vetmedia[col])
                            * (meanSeq[row][col] - vetmedia[col]);
                    // somma=somma+(meanSeq[row][col]-vetmedia[row])*(meanSeq[row][col]-vetmedia[row]);
                }
                var[col] = somma / (meanSeq.length - 1);
                B[col] = var[col] * (n + 1);
            }

            // Step 2: Compute the variance of the various sequences
            double[][] varSeq = new double[m][nrY];
            double[][] sommatemp = new double[m][nrY];
            for (int dim3 = 0; dim3 < m; dim3++) {
                for (int dim2 = 0; dim2 < nrY; dim2++) {
                    double s = 0;
                    // System.out.println("inizio");
                    for (int dim1 = 0; dim1 < n; dim1++) {
                        // System.out.println(seq[dim1][dim2][dim3]);
                        s += seq[dim1][dim2][dim3];
                    }
                    sommatemp[dim3][dim2] = s / n;
                }
            }
            for (int dim3 = 0; dim3 < m; dim3++) {
                for (int dim2 = 0; dim2 < nrY; dim2++) {
                    double s = 0;
                    for (int dim1 = 0; dim1 < n; dim1++) {
                        s += (seq[dim1][dim2][dim3] - sommatemp[dim3][dim2])
                                * (seq[dim1][dim2][dim3] - sommatemp[dim3][dim2]);
                        // System.out.println(seq[dim1][dim2][dim3]);
                        // System.out.println(sommatemp[dim3][dim2]);

                    }
                    varSeq[dim3][dim2] = s / (n - 1.0);
                }
            }

            // Step 2: Calculate the average of the within sequence variances
            double[] W = new double[nrY];

            for (int i = 0; i < nrY; i++) {
                double somma = 0;
                for (int j = 0; j < m; j++) {
                    somma = somma + varSeq[j][i];
                }
                W[i] = somma / m;
            }

            // Step 4: Estimate the target variance
            double[] sigma2 = new double[W.length];
            for (int t = 0; t < W.length; t++) {
                sigma2[t] = ((n - 1.0) / n) * W[t] + B[t] / n;
            }

            // double []R_stat=new double [W.length];
            for (int t = 0; t < W.length; t++) {
                double ter1 = (m + 1.0) / m * sigma2[t] / W[t] - (n - 1.0) / m
                        / n;
                Rstat[t] = Math.sqrt(ter1);
            }

            // Step 5: Compute the R-statistic
            System.out.println("ok");
        }

        // Step 4: Estimate the target variance
        // for (int i=0;i<)
        // double sigma2 = ((n - 1)/n) * W + (1/n) * B;
        return Rstat;
    }

    public static double[] bubbleSort1(double[] x) {
        int n = x.length;
        for (int pass = 1; pass < n; pass++) { // count how many times
            // This next loop becomes shorter and shorter
            for (int i = 0; i < n - pass; i++) {
                if (x[i] > x[i + 1]) {
                    // exchange elements
                    double temp = x[i];
                    x[i] = x[i + 1];
                    x[i + 1] = temp;
                }
            }
        }
        return x;
    }

    public void offde(double[][] XXXX) {
        double[][] eps = new double[int_MCMCPar_seq][int_MCMCPar_n];
        // RandomStream rrs=new MRG32k3a();
        RandomStream rrs = randomstream;
        NormalGen nn = new NormalGen(rrs);
        for (int i = 0; i < int_MCMCPar_seq; i++) {
            for (int j = 0; j < int_MCMCPar_n; j++) {
//                eps[i][j] = Math.pow(10, -6.0) * nn.nextDouble();
                eps[i][j] = 10E-6 * nn.nextDouble();
            }
        }

        double[][] delta_x = new double[int_MCMCPar_seq][int_MCMCPar_n];
        // If not a delayed rejection step --> generate proposal with DE
        if (Extra_DR.equals("No")) {
            // if strcmp(DR,'No');
            // Determine which sequences to evolve with what DE strategy
            DEStrategy();
            double[][] dummy = new double[int_MCMCPar_seq - 1][int_MCMCPar_seq];
            int[][] tt = new int[int_MCMCPar_seq - 1][int_MCMCPar_seq];

            double[][] a = new double[int_MCMCPar_seq - 1][int_MCMCPar_seq];
            for (int row = 0; row < a.length; row++) {
                for (int col = 0; col < a[0].length; col++) {
                    a[row][col] = Math.random();
                }
            }
            double[] vet_temporaneo = new double[a[0].length];
            for (int col = 0; col < dummy[0].length; col++) {
                for (int i = 0; i < a.length; i++) {
                    vet_temporaneo[i] = a[i][col];
                }
                // double[] a={78.1,0,2.4,5};
                double temp = 0;
                int[] b = new int[int_MCMCPar_seq - 1];
                int kkk = 0;
                for (int x = 0; x < b.length; x++) {
                    b[x] = kkk;
                    kkk += 1;
                }
                kkk = 0;
                for (int j = 0; j < a.length; j++) {
                    for (int i = j; i < a.length; i++) {
                        if (vet_temporaneo[j] > vet_temporaneo[i]) {
                            temp = vet_temporaneo[j];
                            vet_temporaneo[j] = vet_temporaneo[i];
                            vet_temporaneo[i] = temp;
                            int temp2 = b[j];
                            b[j] = b[i];
                            b[i] = temp2;
                        }
                    }
                }

                // System.out.println();
                for (int row = 0; row < dummy.length; row++) {
                    // System.out.println("row="+row);
                    // System.out.println("col="+col);
                    // System.out.println(b[row]);
                    dummy[row][col] = vet_temporaneo[row];
                    tt[row][col] = b[row];
                }
            }

            // Generate uniform random numbers for each chain to determine which
            // dimension to update

            double[][] D = new double[int_MCMCPar_seq][int_MCMCPar_n];
            for (int row = 0; row < int_MCMCPar_seq; row++) {
                for (int col = 0; col < int_MCMCPar_n; col++) {
                    D[row][col] = Math.random();
                }
            }
            // Ergodicity for each individual chain
            double[][] noise_x = new double[int_MCMCPar_seq][int_MCMCPar_n];
            for (int row = 0; row < int_MCMCPar_seq; row++) {
                for (int col = 0; col < int_MCMCPar_n; col++) {
                    noise_x[row][col] = MCMCPar_eps * (2.0 * Math.random() - 1.0);
                }
            }

            // Initialize the delta update to zero
            delta_x = new double[int_MCMCPar_seq][int_MCMCPar_n];

            // Each chain evolves using information from other chains to create
            // offspring
            for (int qq = 0; qq < int_MCMCPar_seq; qq++) {
                // Define ii and remove current member as an option
                double[] ii = new double[int_MCMCPar_seq];
                for (int i = 0; i < int_MCMCPar_seq; i++) {
                    ii[i] = 1;
                }
                ii[qq] = 0;
                int[] idxx = new int[int_MCMCPar_seq];
                int cnt = 0;
                for (int i = 0; i < int_MCMCPar_seq; i++) {
                    // if(i!=qq){
                    if (ii[i] > 0) {
                        idxx[cnt] = i;
                        cnt += 1;
                    }
                }

                int[] idx = new int[cnt];
                for (int j = 0; j < cnt; j++) {
                    idx[j] = idxx[j];
                }
                // randomly select two members of ii that have value == 1
                int[] vet_tt = new int[2 * DEversion[qq]];  //öhm ok tt has size [nChains-1 x nChains]
                                                            //this means DEVersion must be < than nChains/2
                for (int i = 0; i < vet_tt.length; i++) {
                    vet_tt[i] = tt[i][qq];
                }
                int[] rr = new int[vet_tt.length];
                for (int i = 0; i < vet_tt.length; i++) {
                    rr[i] = idx[vet_tt[i]];
                }
                // --- WHICH DIMENSIONS TO UPDATE? DO SOMETHING WITH CROSSOVER
                // ----
                int[] i_temp = new int[int_MCMCPar_n];
                int cont = 0;
                for (int l = 0; l < int_MCMCPar_n; l++) {
                    // System.out.println("Nel for di offde");
                    // System.out.println(CR[qq][gen_number]);
                    if (CR[qq][gen_number] < 0) {
                        System.out.println(CR[qq][gen_number]);
                    }
                    if (D[qq][l] > (1.0 - CR[qq][gen_number])) {
                        i_temp[cont] = l;
                        cont += 1;
                    }
                }
                int[] i = null;
                // Update at least one dimension
                if (cont == 0) {
                    int[] it = new int[(int_MCMCPar_n)];
                    RandomPermutation.init(it, (int_MCMCPar_n));
                    // RandomStream rs=new MRG32k3a();
                    RandomStream rs = randomstream;
                    RandomPermutation.shuffle(it, rs);
                    i = new int[1];
                    i[cont] = it[0] - 1;
                    // cont+=1;
                } // if(cont>0){
                else {
                    i = new int[cont];
                    for (int u = 0; u < cont; u++) {
                        i[u] = i_temp[u];
                    }
                }
                // System.out.println("cont=" + cont);
                int NrDim = i.length;
                // int[] i=new int[NrDim];
                // for (int rrr=0;rrr<cont;rrr++){
                // i[rrr]=i_temp[rrr];
                // }
                double[] delta;
                double n = Math.random();
                double m = 4.0 / 5.0;
                if (n < m) {
                    // Lookup Table
                    double JumpRate = Table_JumpRate[NrDim - 1][0];

                    // Produce the difference of the pairs used for population
                    // evolution
                    int index_row_1_end = DEversion[qq];
                    int index_row_2_start = DEversion[qq];
                    int index_row_2_end = 2 * DEversion[qq];
                    int[] rr1 = new int[index_row_1_end];
                    int[] rr2 = new int[index_row_2_end - index_row_2_start];
                    for (int k = 0; k < index_row_1_end; k++) {
                        rr1[k] = rr[k];
                    }
                    int row_cnt = 0;
                    for (int k = index_row_2_start; k < index_row_2_end; k++) {
                        rr2[row_cnt] = rr[k];
                        row_cnt = 1;
                    }
                    double[][] X1 = new double[rr1.length][int_MCMCPar_n];
                    double[][] X2 = new double[rr2.length][int_MCMCPar_n];
                    for (int row = 0; row < X1.length; row++) {
                        for (int col = 0; col < X1[0].length; col++) {
                            X1[row][col] = XXXX[rr1[row]][col];
                        }
                    }
                    for (int row = 0; row < X2.length; row++) {
                        for (int col = 0; col < X2[0].length; col++) {
                            X2[row][col] = XXXX[rr2[row]][col];
                        }
                    }

                    // double []X1=new double [index_row_1_end];
                    delta = new double[X1[0].length];

                    for (int col = 0; col < X1[0].length; col++) {
                        double somma = 0;
                        for (int row = 0; row < X1.length; row++) {
                            somma = somma + (X1[row][col] - X2[row][col]);
                        }
                        delta[col] = somma;
                    }

                    // Then fill update the dimension
                    for (int contcol = 0; contcol < i.length; contcol++) {
                        delta_x[qq][i[contcol]] = (1.0 + noise_x[qq][i[contcol]])
                                * JumpRate * delta[i[contcol]];
                    }
                    // System.out.println("ciao");
                } else {
                    // Set the JumpRate to 1 and overwrite CR and DEversion
                    double JumpRate = 1.0;
                    CR[qq][gen_number] = -1.0;
                    delta = new double[int_MCMCPar_n];
                    // Compute delta from one pair
                    for (int col = 0; col < int_MCMCPar_n; col++) {
                        delta[col] = XXXX[rr[0]][col] - XXXX[rr[1]][col];
                    }
                    for (int contcol = 0; contcol < delta_x[0].length; contcol++) {
                        delta_x[qq][contcol] = JumpRate * delta[contcol];

                    }
                }

                double ssquare = 0;
                for (int v = 0; v < delta_x[0].length; v++) {
                    ssquare = (delta_x[qq][v] * delta_x[qq][v]) + ssquare;
                }
                //CF: NOT NICE mat must be square!!
                if (ssquare == 0 && x_old.length == x_old[0].length) {
                    double[][] mat = new double[x_old.length][x_old[0].length];
                    for (int row = 0; row < mat.length; row++) {
                        for (int col = 0; col < mat[0].length; col++) {
                            mat[row][col] = x_old[row][col];
                        }
                    }
                    double[] arr1 = new double[mat.length];
                    double[] arr2 = new double[mat.length];
                    double[][] mat_cov = new double[mat.length][mat[0].length];

                    for (int col = 0; col < mat[0].length; col++) {
                        for (int row1 = 0; row1 < mat.length; row1++) {
                            arr1[row1] = mat[row1][col];
                        }

                        DoubleArrayList arrayuno = new DoubleArrayList(arr1);
                        for (int col2 = 0; col2 < mat[0].length; col2++) {
                            for (int row = 0; row < mat.length; row++) {
                                arr2[row] = mat[row][col2];
                            }
                            DoubleArrayList arraydue = new DoubleArrayList(arr2);
                            if (col == col2) {
                                mat_cov[col][col2] = Descriptive.covariance(
                                        arrayuno, arraydue)
                                        + Math.pow(10, -5.0);
                            } else {
                                mat_cov[col][col2] = Descriptive.covariance(
                                        arrayuno, arraydue);
                            }
                        }
                    }                    
                    Matrix chol = new Matrix(mat_cov);
                    CholeskyDecomposition dec = chol.chol();
                    // Matrix uff=dec.getL();
                    // RandomStream rrrs=new MRG32k3a();
                    RandomStream rrrs = randomstream;
                    NormalGen nnn = new NormalGen(rrrs);
                    double[][] R = new double[x_old.length][x_old[0].length];
                    for (int row = 0; row < R.length; row++) {
                        for (int col = 0; col < R[0].length; col++) {
                            R[row][col] = (2.38 / Math.sqrt(MCMCPar_n))
                                    * dec.getL().get(row, col);
                        }
                    }
                    double[] f = new double[int_MCMCPar_n];
                    for (int row = 0; row < int_MCMCPar_n; row++) {
                        f[row] = nnn.nextDouble();
                    }
                    // for(int contcol=0; contcol<int_MCMCPar_n;contcol++){
                    // double somma=0;
                    // for (int oo=0;oo<R[0].length;oo++){
                    // somma+=R[oo][contcol]*f[oo];
                    // }
                    // delta_x[qq][contcol]=somma;
                    // }
                    for (int contcol = 0; contcol < int_MCMCPar_n; contcol++) {
                        double somma = 0;
                        for (int oo = 0; oo < R[0].length; oo++) {
                            somma += R[contcol][oo] * f[oo];
                        }
                        delta_x[qq][contcol] = somma;
                    }
                    System.out.println("Cholewsky decomposition");
                }
                // Attento a che entra nel off de: CR per lui e solo un vettore,
                // table jump vedi cos'e.
            }
        }

        double[][] x_new1 = new double[x_old.length][x_old[0].length];
        for (int row = 0; row < x_new1.length; row++) {
            for (int col = 0; col < x_new1[0].length; col++) {
                x_new1[row][col] = x_old[row][col] + delta_x[row][col]
                        + eps[row][col];
            }
        }
        // x_new = x_old + delta_x + eps;
        x_new = ReflectBounds(x_new1, ParRange_maxn, ParRange_minn);
    }

    public double[] percetili(double[] vettore, double valori[]) {
        double[] result = new double[valori.length];
        double[] ordinati = bubbleSort1(vettore);
        double[] completo = new double[vettore.length];
        // double []posizioni=new double[];
        for (int i = 0; i < ordinati.length; i++) {
            completo[i] = 100 * ((i) + 0.5) / vettore.length;
        }

        for (int j = 0; j < valori.length; j++) {
            double percentile = valori[j];
            for (int i = 0; i < completo.length; i++) {
                if (completo[i] > percentile) {
                    result[j] = ordinati[i - 1]
                            + ((ordinati[i] - ordinati[i - 1]) / (completo[i] - completo[i - 1]))
                            * (percentile - completo[i - 1]);

                }
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        DREAM d = new DREAM();
        d.setFunction((AbstractFunction)LeafRiverExample.getModel());
        d.setCrossoverValue(3);
        d.setDEPairs(1);
        d.setNumberOfMarkovChains(5);
        d.setMCMCsteps(10);
        d.setMCMCeps(2e-1);        
        d.setTransformObjectiveIntoLogLikelihood(true);
        d.setGamma(0.0);
        d.setSigma(0.01);
        d.setMaxn(5000);
        
        //may be set to number of parameters
        d.setNumberOfMarkovChains(d.getFunction().getInputDimension());
        
        
        try{
            d.init();
            d.procedure();
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }        
    }
}//2285
