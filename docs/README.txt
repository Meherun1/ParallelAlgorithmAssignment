PARALLEL ALGORITHM ASSIGNMENT
=============================
Submitted By
 Student: [Meherun Nesa]
 ID: 24MCSE726
 Course: Parallel Algorithm
 Submission Date: October 25, 2025

Submitted TO
 Teacher's Name: Md.Monjur Ul Hasan
 Dept : Computer Science & Engineering, CUET

PROJECT DESCRIPTION:
-------------------
This assignment implements two multithreading simulations:

1. OPTION A: Grocery Store Queue System
   - Multiple checkout counters with separate queues
   - Customers choose shortest queue
   - Thread-safe queue operations

2. OPTION B: Bank Queue System  
   - Multiple tellers with single shared queue
   - FIFO customer service
   - Thread-safe synchronization

IMPLEMENTATION DETAILS:
----------------------
- Language: Java
- Multithreading: Yes
- Synchronization: BlockingQueue, Atomic variables
- Time Simulation: Real-time scaled (1 real second = 1 simulated minute)

SAMPLE RUNS PERFORMED:
----------------------
Grocery Store Simulation:
- Run 1: 5 minutes
- Run 2: 10 minutes  
- Run 3: 15 minutes

Bank Simulation:
- Run 1: 5 minutes
- Run 2: 10 minutes
- Run 3: 15 minutes

FILES INCLUDED:
---------------
src/
  GroceryStoreSimulation.java
  BankSimulation.java

docs/
  README.txt 

PROGRAM OUTPUT INCLUDES:
-----------------------
- Total customers arrived
- Total customers served  
- Customers who left unserved
- Average service time
- Final queue status

GITHUB REPOSITORY:
------------------

https://github.com/Meherun1/ParallelAlgorithmAssignment