import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class BankSimulation {
    private final int numTellers;
    private final int queueCapacity;
    private final int simulationMinutes;
    private final BlockingQueue<Customer> waitingQueue;
    private final AtomicInteger totalArrived = new AtomicInteger(0);
    private final AtomicInteger totalServed = new AtomicInteger(0);
    private final AtomicInteger totalLeft = new AtomicInteger(0);
    private final AtomicLong totalServiceTime = new AtomicLong(0);
    private volatile boolean simulationRunning = true;

    public BankSimulation(int numTellers, int queueCapacity, int simulationMinutes) {
        this.numTellers = numTellers;
        this.queueCapacity = queueCapacity;
        this.simulationMinutes = simulationMinutes;
        this.waitingQueue = new LinkedBlockingQueue<>(queueCapacity);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java BankSimulation <simulation_minutes>");
            System.out.println("Example: java BankSimulation 5");
            return;
        }

        int simulationMinutes = Integer.parseInt(args[0]);
        int numTellers = 2;
        int queueCapacity = 6;

        BankSimulation bank = new BankSimulation(numTellers, queueCapacity, simulationMinutes);
        bank.startSimulation();
    }

    public void startSimulation() {
        System.out.println("=== BANK SIMULATION ===");
        System.out.println("Simulation Time: " + simulationMinutes + " minutes");
        System.out.println("Tellers: " + numTellers);
        System.out.println("Waiting Queue Capacity: " + queueCapacity + " customers\n");

        List<Thread> tellerThreads = new ArrayList<>();
        for (int i = 0; i < numTellers; i++) {
            final int tellerId = i;
            Thread tellerThread = new Thread(() -> tellerService(tellerId));
            tellerThread.start();
            tellerThreads.add(tellerThread);
        }

        Thread arrivalThread = new Thread(this::customerArrival);
        arrivalThread.start();

        try {
            Thread.sleep(simulationMinutes * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        simulationRunning = false;
        
        try {
            arrivalThread.join(2000);
            for (Thread t : tellerThreads) {
                t.join(2000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        printStatistics();
    }

    private void customerArrival() {
        Random random = new Random();
        
        while (simulationRunning) {
            try {
                int arrivalInterval = random.nextInt(51) + 50;
                Thread.sleep(arrivalInterval * 10);

                Customer customer = new Customer(totalArrived.incrementAndGet());
                boolean served = false;

                if (waitingQueue.offer(customer)) {
                    System.out.printf("[ARRIVAL] Customer %d joined waiting queue (Queue size: %d/%d)%n",
                            customer.id, waitingQueue.size(), queueCapacity);
                    served = true;
                }

                if (!served) {
                    System.out.printf("[WAITING] Customer %d found queue full. Waiting...%n", customer.id);
                    Thread.sleep(600 * 10);
                    
                    if (waitingQueue.offer(customer)) {
                        System.out.printf("[RETRY SUCCESS] Customer %d joined queue after waiting%n", customer.id);
                        served = true;
                    } else {
                        totalLeft.incrementAndGet();
                        System.out.printf("[LEFT] Customer %d left unserved%n", customer.id);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void tellerService(int tellerId) {
        Random random = new Random();
        
        while (simulationRunning || !waitingQueue.isEmpty()) {
            try {
                Customer customer = waitingQueue.poll(100, TimeUnit.MILLISECONDS);
                if (customer != null) {
                    int serviceTime = random.nextInt(301) + 300;
                    
                    System.out.printf("[SERVICE] Teller %d serving customer %d for %d seconds (Queue: %d)%n",
                            tellerId + 1, customer.id, serviceTime, waitingQueue.size());
                    Thread.sleep(serviceTime);
                    
                    totalServed.incrementAndGet();
                    totalServiceTime.addAndGet(serviceTime);
                    System.out.printf("[COMPLETE] Teller %d finished customer %d%n",
                            tellerId + 1, customer.id);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void printStatistics() {
        System.out.println("\n=== BANK SIMULATION RESULTS ===");
        System.out.println("Total customers arrived: " + totalArrived.get());
        System.out.println("Total customers served: " + totalServed.get());
        System.out.println("Customers who left unserved: " + totalLeft.get());
        
        if (totalServed.get() > 0) {
            double avgServiceTime = totalServiceTime.get() / (double) totalServed.get();
            System.out.printf("Average service time: %.2f seconds%n", avgServiceTime);
        } else {
            System.out.println("Average service time: 0.00 seconds");
        }
        
        System.out.println("Final queue size: " + waitingQueue.size());
    }

    private static class Customer {
        final int id;

        Customer(int id) {
            this.id = id;
        }
    }
}