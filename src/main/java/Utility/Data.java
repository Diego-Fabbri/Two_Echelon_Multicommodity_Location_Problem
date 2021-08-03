package Utility;

public class Data {

    public static int Number_of_Plants() {
        return 2;// cardinality of set I
    }

    public static int Number_of_DCs() {
        return 2;// cardinality of set J
    }

    public static int Number_of_Demanders() {
        return 2;// cardinality of set R
    }

    public static int Number_of_Commodities() {
        return 1;// cardinality of set K
    }

    public static int p() {//limit to the maximum number of opened DCs 
        return 1;
    }



    public static double[][][][] Cost( int I, int J, int R, int K) {

        // K,I,J,R are cardinality of sets K,I,J,R
        double[][][][] Cost = new double[K][I][J][R];
        for (int k = 0; k < K; k++) {
            for (int i = 0; i < I; i++) {
                for (int j = 0; j < J; j++) {
                    for (int r = 0; r < R; r++) {
                     Cost[k][i][j][r]= 1+ Math.random() * 15;

                    }
                }
            }

        }
        return Cost;
    }

    public static double[][] Demand() {// d_r^k
        double[][] demand
                ={ {800.00,600.00}};
        return demand;

    }

    public static double[][] Plant_Capacities() {// p_i^k
        double[][] Plant_Capacities = {{1200.00, 1500.00}};
        return Plant_Capacities;

    }

    public static double[] Minimum_DCs_Capacities() {// q_j^-
        double[] Minimum_DCs_Capacities = {0, 0};
        return Minimum_DCs_Capacities;
    }

    public static double[] Maximum_DCs_Capacities() {// q_j^+
        double[] Maximum_DCs_Capacities = {1500.00, 1200.00};
        return Maximum_DCs_Capacities;
    }

    public static double[] DC_Fixed_Cost() {// f_j
        double[] DC_Fixed_Cost = {960000.00,880000.00};
        return DC_Fixed_Cost;
    }

    public static double[] DC_Marginal_Cost() {// g_j
        double[] DC_Marginal_Cost = {1.00, 2.00};
        return DC_Marginal_Cost;
    }

}
