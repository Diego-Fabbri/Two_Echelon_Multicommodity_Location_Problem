package com.mycompany.two_echelon_multicommodity_location_problem;

import Utility.Data;
import ilog.concert.IloException;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Main {

    public static void main(String[] args) throws IloException, FileNotFoundException {
        System.setOut(new PrintStream("Two_Echelon_Multicommodity_Location_Problem.log"));

        int I = Data.Number_of_Plants();
        int J = Data.Number_of_DCs();
        int R = Data.Number_of_Demanders();
        int K = Data.Number_of_Commodities();
        
        int p = Data.p();
        
        double[][][][] c = Data.Cost(I, J, R, K);
        double[][] d = Data.Demand();
        double[][] p_max = Data.Plant_Capacities();
        double[] q_min = Data.Minimum_DCs_Capacities();
        double[] q_max = Data.Maximum_DCs_Capacities();
        double[] f = Data.DC_Fixed_Cost();
        double[] g = Data.DC_Marginal_Cost();

        TEMC_Model model = new TEMC_Model(I, J, R, K, p, c, d, p_max, q_min, q_max, f, g);
         model.solve();
    }
}
