#Set your own working directory
setwd("C:/Users/diego/Documents/R/Projects/GitHub_Projects/Optimization/Two-Echelon Multicommodity Location Model")

# Import lpSolve package
library(lpSolve)

#Import required packages
library(dplyr)
library(ROI)
library(ROI.plugin.symphony)
library(ompr)
library(ompr.roi)

#Set number of plants
I <- 2

#Set number of DCs
J <- 2

#Set number of Demanders
R <- 2

#Set number of Commodities
K <- 1

#Set maximum of plant to open
P_max <- 1

#Set up cost
set.seed(12345)
c <- array(dim = c(K, I, J, R))

for(k in 1:K)
  for (i in 1:I) {
    for(j in 1:J){
      for(r in 1:R){
        
        c[k,i,j,r] <- sample(1:15,size = 1) + sample (seq(0, 1, 0.01), 1)
      
      }}}

#Set demands
d <- matrix(c(800,600),
            nrow = 1, byrow = TRUE)

#Set plant capacities
p_max <- matrix(c(1200, 1500),
            nrow = 1, byrow = TRUE)

#Set minimum plant capacities
q_min <- c(0, 0)

#Set maximum plant capacities
q_max <- c(1500, 1200)

#Set fixed cost
f <- c(960000,880000)

#Set marginal cost
g <- c(1.00, 2.00)

#Feasibility condition
condition <- FALSE
  
for (k in 1:K) {
  total_capacity = 0
  
  total_demand = 0
  
  for (i in 1:I) {
    total_capacity <- total_capacity + p_max[k, i]
  }
  for (r in 1:R) {
    total_demand <- total_demand + d[k, r]
  }
  if (total_capacity < total_demand) {
    condition <- FALSE
    print(paste("Feasibility Condition does not hold for commodity:", k))
  }
  else{
    condition <- TRUE
    print(paste("Feasibility Condition holds for commodity:", k))
  }
}

#Build Model
Model <- MIPModel() %>%
  add_variable(s[k,i,j,r],k = 1:K, i = 1:I, j = 1:J, r = 1:R,  type = "continuous", lb=0) %>% #define variables
  add_variable(y[j,r], j = 1:J, r = 1:R , type = "binary") %>%
  add_variable(z[j], j = 1:J , type = "binary") %>%
  set_objective(expr = sum_expr(c[k,i,j,r]*s[k,i,j,r], k = 1:K, i = 1:I, j = 1:J, r = 1:R) + 
                       sum_expr(f[j]*z[j] + g[j]*sum_expr(d[k,r]*y[j,r], r = 1:R, k = 1:K), j = 1:J),
                sense = "min") %>% #define objective
  add_constraint(sum_expr(s[k,i,j,r],j = 1:J, r = 1:R) <= p_max[k,i],i = 1:I,k = 1:K) %>% #define constraints
  add_constraint(sum_expr(s[k,i,j,r],i = 1:I) == d[k,r]*y[j,r], j = 1:J, r = 1:R,k = 1:K) %>%
  add_constraint(sum_expr(y[j,r], j = 1:J) == 1, r = 1:R) %>%
  add_constraint(sum_expr(d[k,r]*y[j,r],r = 1:R,k = 1:K) <= q_max[j]*z[j] , j = 1:J)%>%
  add_constraint(sum_expr(d[k,r]*y[j,r],r = 1:R,k = 1:K) >= q_min[j]*z[j] , j = 1:J)%>%
  add_constraint(sum_expr(z[j], j = 1:J) == P_max ) %>%
  solve_model(with_ROI(solver = "symphony", verbosity = 1))

#Model summary
##Status
print(paste("Model status is:", Model$status))

##Objective Function
print(paste("Objective value:", objective_value(Model)))

##Variables

for (a in 1:J) {
  for (b in 1:R) {
    tmp_y <- get_solution(Model, y[j, r]) %>%
      filter(variable == "y", j == a, r == b) %>%
      select(value)
    
    
    if (tmp_y != 0) {
      print(paste("--->y[", a, ",", b , "] =", tmp_y))
    }
  }
}

for (a in 1:J) {
  tmp_z <- get_solution(Model, z[j]) %>%
    filter(variable == "z", j == a) %>%
    select(value)
  
  
  if (tmp_z != 0) {
    print(paste("--->z[", a, "] =", tmp_z))
  }
}

for (c in 1:K)
  for (d in 1:I) {
    for (a in 1:J) {
      for (b in 1:R) {
        tmp_s <- get_solution(Model, s[k, i, j, r]) %>%
          filter(variable == "s", k == c, i == d, j == a, r == b) %>%
          select(value)
        
        
        if (tmp_s != 0) {
          print(paste("--->s[", c, ",", d, ",", a, ",", b, "] =", tmp_s))
        }
        
      }
    }
  }

