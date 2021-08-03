package com.mycompany.two_echelon_multicommodity_location_problem;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloObjective;
import ilog.concert.IloObjectiveSense;
import ilog.cplex.IloCplex;
import static java.lang.Math.round;

public class TEMC_Model {

    protected int I;
    protected int J;
    protected int R;
    protected int K;

    protected int p;
    protected double[][][][] c;
    protected double[][] d;
    protected double[][] p_max;
    protected double[] q_min;
    protected double[] q_max;
    protected double[] f;
    protected double[] g;
    protected boolean Feasibility_Condition;
    protected IloCplex model;
    protected IloIntVar[] z;
    protected IloIntVar[][] y;
    protected IloNumVar[][][][] s;

    TEMC_Model(int I, int J, int R, int K, int p, double[][][][] c, double[][] d, double[][] p_max, double[] q_min, double[] q_max, double[] f, double[] g) throws IloException {
        this.I = I;
        this.J = J;
        this.R = R;
        this.K = K;

        this.p = p;
        this.c = c;
        this.d = d;
        this.p_max = p_max;
        this.q_min = q_min;
        this.q_max = q_max;
        this.f = f;
        this.g = g;
        this.model = new IloCplex();
        this.z = new IloIntVar[J];
        this.y = new IloIntVar[J][R];
        this.s = new IloNumVar[K][I][J][R];
        this.Feasibility_Condition = Feasibility_Check();
    }

    protected void addVariables() throws IloException {
        for (int j = 0; j < J; j++) {
            int pos_j = j + 1;
            z[j] = (IloIntVar) model.numVar(0, 1, IloNumVarType.Int, "z[" + pos_j + "]");

        }

        for (int j = 0; j < J; j++) {
            int pos_j = j + 1;
            for (int r = 0; r < R; r++) {
                int pos_r = r + 1;
                y[j][r] = (IloIntVar) model.numVar(0, 1, IloNumVarType.Int, "y[" + pos_j + "][" + pos_r + "]");

            }
        }

        for (int k = 0; k < K; k++) {
            int pos_k = k + 1;
            for (int i = 0; i < I; i++) {
                int pos_i = i + 1;
                for (int j = 0; j < J; j++) {
                    int pos_j = j + 1;
                    for (int r = 0; r < R; r++) {
                        int pos_r = r + 1;

                        s[k][i][j][r] = (IloNumVar) model.numVar(0, Float.MAX_VALUE, IloNumVarType.Float, "s[k=" + pos_k + "][i=" + pos_i + "][j=" + pos_j + "][r=" + pos_r + "]");

                    }
                }
            }
        }

    }

    protected void addObjective() throws IloException {
        IloLinearNumExpr objective = model.linearNumExpr();

        for (int k = 0; k < K; k++) {

            for (int i = 0; i < I; i++) {

                for (int j = 0; j < J; j++) {

                    for (int r = 0; r < R; r++) {

                        objective.addTerm(c[k][i][j][r], s[k][i][j][r]);

                    }
                }
            }
        }

        for (int j = 0; j < J; j++) {
            objective.addTerm(f[j], z[j]);
            for (int r = 0; r < R; r++) {
                for (int k = 0; k < K; k++) {

                    objective.addTerm(d[k][r] * g[j], y[j][r]);

                }
            }

        }

        IloObjective Obj = model.addObjective(IloObjectiveSense.Minimize, objective);
    }

    protected void addConstraints() throws IloException {
        // Constraint (1)
        for (int i = 0; i < I; i++) {
            for (int k = 0; k < K; k++) {
                IloLinearNumExpr expr_1 = model.linearNumExpr();

                for (int j = 0; j < J; j++) {

                    for (int r = 0; r < R; r++) {

                        expr_1.addTerm(s[k][i][j][r], 1);

                    }
                }
                model.addLe(expr_1, p_max[k][i]);
            }

        }

        // Constraint (2)
        for (int j = 0; j < J; j++) {
            for (int r = 0; r < R; r++) {
                for (int k = 0; k < K; k++) {
                    IloLinearNumExpr expr_2 = model.linearNumExpr();
                    for (int i = 0; i < I; i++) {
                        expr_2.addTerm(s[k][i][j][r], 1);

                    }
                    expr_2.addTerm(y[j][r], -d[k][r]);
                    model.addEq(expr_2, 0);
                }

            }
        }

        //Constraint (3)
        for (int r = 0; r < R; r++) {
            IloLinearNumExpr expr_3 = model.linearNumExpr();
            for (int j = 0; j < J; j++) {
                expr_3.addTerm(y[j][r], 1);

            }

            model.addEq(expr_3, 1);

        }

        // Constraint (4 max)
        for (int j = 0; j < J; j++) {
            IloLinearNumExpr expr_4max = model.linearNumExpr();
            for (int r = 0; r < R; r++) {
                for (int k = 0; k < K; k++) {

                    expr_4max.addTerm(d[k][r], y[j][r]);

                }
            }
            expr_4max.addTerm(z[j], -q_max[j]);
            model.addLe(expr_4max, 0);
        }

        // Constraint (4 min)
        for (int j = 0; j < J; j++) {
            IloLinearNumExpr expr_4min = model.linearNumExpr();
            for (int r = 0; r < R; r++) {
                for (int k = 0; k < K; k++) {

                    expr_4min.addTerm(d[k][r], y[j][r]);

                }
            }
            expr_4min.addTerm(z[j], -q_min[j]);
            model.addGe(expr_4min, 0);
        }

        //  Constraint (5)  
        IloLinearNumExpr expr_5 = model.linearNumExpr();
        for (int j = 0; j < J; j++) {
            expr_5.addTerm(z[j], 1);
        }
        model.addEq(expr_5, p);

    }

    protected boolean Feasibility_Check() {

        for (int k = 0; k < K; k++) {
            double total_capacity = 0;
            double total_demand = 0;
            for (int i = 0; i < I; i++) {
                total_capacity += p_max[k][i];
            }
            for (int r = 0; r < R; r++) {
                total_demand += d[k][r];
            }
            if (total_capacity < total_demand) {
                return false;
            }

        }

        return true;
    }

    public void solve() throws IloException {
        addVariables();
        addObjective();
        addConstraints();
        model.exportModel("Two_Echelon_Multicommodity_Location_Problem.lp");

        model.solve();

        if (Feasibility_Condition == true) {

            for (int k = 0; k < K; k++) {
                int commodity = k + 1;
                System.out.println();
                System.out.println();
                System.out.println("Feasibility condition is satisfied for commodity " + commodity);

                double total_capacity = 0;
                double total_demand = 0;
                for (int i = 0; i < I; i++) {
                    total_capacity += p_max[k][i];
                }
                for (int r = 0; r < R; r++) {
                    total_demand += d[k][r];
                }

                if (total_demand == total_capacity) {
                    System.out.println();
                    System.out.println();
                    System.out.println("Total demand is equal to total capacity for commodity " + commodity);
                    System.out.println("Total demand for commodity " + commodity + " is : " + total_demand);
                    System.out.println("Total capacity for commodity " + commodity + " is : " + total_capacity);

                } else {
                    System.out.println();
                    System.out.println();
                    System.out.println("Total demand is less than total capacity for commodity " + commodity);
                    System.out.println("Total demand for commodity " + commodity + " is : " + total_demand);
                    System.out.println("Total capacity for commodity " + commodity + " is : " + total_capacity);
                }

            }
            if (model.getStatus() == IloCplex.Status.Feasible
                    | model.getStatus() == IloCplex.Status.Optimal) {
                System.out.println();
                System.out.println();
                System.out.println("Solution status = " + model.getStatus());

                System.out.println();
                System.out.println();
                System.out.println("Total Cost is = " + model.getObjValue());
                double transportation_total_cost = 0;
                for (int k = 0; k < K; k++) {

                    for (int i = 0; i < I; i++) {

                        for (int j = 0; j < J; j++) {

                            for (int r = 0; r < R; r++) {
                                double H = model.getValue(s[k][i][j][r]);
                                double G = c[k][i][j][r];

                                transportation_total_cost += H * G;

                            }
                        }
                    }
                }
                      
                System.out.println("---> Total Transportation Cost is = " + transportation_total_cost);
                
                double total_fixed_cost = 0;
                for (int j = 0; j < J; j++) {
                    double H = model.getValue(z[j]);
                    double G = f[j];
                    total_fixed_cost+= H*G;
                }
                
                System.out.println("---> Total Fixed Cost is = " + total_fixed_cost );

                double total_marginal_cost = 0;

                for (int j = 0; j < J; j++) {

                    for (int r = 0; r < R; r++) {
                        for (int k = 0; k < K; k++) {

                            total_marginal_cost += d[k][r] * g[j] * model.getValue(y[j][r]);

                        }
                    }

                }

                
                System.out.println("---> Total Marginal Cost is = " + total_marginal_cost);
                System.out.println();
                System.out.println();
                System.out.println("Value of variables z");

                for (int j = 0; j < J; j++) {
                    if (model.getValue(z[j]) != 0) {
                        System.out.println("---> " + z[j].getName() + " = " + model.getValue(z[j]));
                    }
                }
                System.out.println();
                System.out.println();

                System.out.println("Value of variables y");

                for (int j = 0; j < J; j++) {
                    for (int r = 0; r < R; r++) {
                        if (model.getValue(y[j][r]) != 0) {
                            System.out.println("---> " + y[j][r].getName() + " = " + model.getValue(y[j][r]));
                        }
                    }
                }

                System.out.println();
                System.out.println();

                System.out.println("Value of variables s");
                for (int k = 0; k < K; k++) {

                    for (int i = 0; i < I; i++) {

                        for (int j = 0; j < J; j++) {

                            for (int r = 0; r < R; r++) {
                                if (model.getValue(s[k][i][j][r]) != 0) {
                                    System.out.println("---> " + s[k][i][j][r].getName() + " = " + model.getValue(s[k][i][j][r]));

                                }
                            }
                        }
                    }
                }

            } else {
                System.out.println("The problem status is:" + model.getStatus());
            }

        } else {
            System.out.println("Feasibility condition is not satisfied");
            System.out.println();
            for (int k = 0; k < K; k++) {

                double total_capacity = 0;
                double total_demand = 0;
                for (int i = 0; i < I; i++) {
                    total_capacity += p_max[k][i];
                }
                for (int r = 0; r < R; r++) {
                    total_demand += d[k][r];
                }

                if (total_demand > total_capacity) {
                    System.out.println("Total capacity is less than total demand for commodity " + (k + 1));
                    System.out.println("Total demand for commodity " + (k + 1) + "is: " + total_demand);
                    System.out.println("Total capacity for commodity " + (k + 1) + "is: " + total_capacity);

                }
            }
        }
    }

}
    
    
