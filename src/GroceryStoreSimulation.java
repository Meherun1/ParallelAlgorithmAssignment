import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class GroceryStoreSimulation {
    private final int numCounters;
    private final int queueCapacity;
    private final int simulationMinutes;
    private final List<BlockingQueue<Customer>> queues;
    private final AtomicInteger totalArrived = new AtomicInteger(0);
    private final AtomicInteger totalServed = new AtomicInteger(0);
    private final AtomicInteger totalLeft = new AtomicInteger(0);
    private final AtomicLong totalServiceTime = new AtomicLong(0);
    private volatile boolean simulationRunning = true;

    public GroceryStoreSimulation(int numCounters, int queueCapacity, int simulationMinutes) {
        this.numCounters = numCounters;
        this.queueCapacity = queueCapacity;
        this.simulationMinutes = simulationMinutes;
        this.queues = new ArrayList<>();
        
        for (int i = 0; i < numCounters; i++) {
            queues.add(new LinkedBlockingQueue<>(queueCapacity));
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java GroceryStoreSimulation <simulation_minutes>");
            System.out.println("Example: java GroceryStoreSimulation 5");
            return;
        }

        int simulationMinutes = Integer.parseInt(args[0]);
        int numCounters = 3;
        int queueCapacity = 4;

        GroceryStoreSimulation store = new GroceryStoreSimulation(numCounters, queueCapacity, simulationMinutes);
        store.startSimulation();
    }

    public void startSimulation() {
        System.out.println("=== GROCERY STORE SIMULATION ===");
        System.out.println("Simulation Time: " + simulationMinutes + " minutes");
        System.out.println("Checkout Counters: " + numCounters);
        System.out.println("Queue Capacity: " + queueCapacity + " customers\n");

        List<Thread> counterThreads = new ArrayList<>();
        for (int i = 0; i < numCounters; i++) {
            final int counterId = i;
            Thread counterThread = new Thread(() -> checkoutCounter(counterId));
            counterThread.start();
            counterThreads.add(counterThread);
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
            for (Thread t : counterThreads) {
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

                List<Integer> shortestQueues = findShortestQueues();
                if (!shortestQueues.isEmpty()) {
                    int chosenQueue = shortestQueues.get(random.nextInt(shortestQueues.size()));
                    
                    if (queues.get(chosenQueue).offer(customer)) {
                        System.out.printf("[ARRIVAL] Customer %d joined queue %d (Queue sizes: %s)%n",
                                customer.id, chosenQueue + 1, getQueueSizes());
                        served = true;
                    }
                }

                if (!served) {
                    System.out.printf("[WAITING] Customer %d found all queues full. Waiting...%n", customer.id);
                    Thread.sleep(600 * 10);
                    
                    List<Integer> retryQueues = findShortestQueues();
                    if (!retryQueues.isEmpty()) {
                        int chosenQueue = retryQueues.get(random.nextInt(retryQueues.size()));
                        if (queues.get(chosenQueue).offer(customer)) {
                            System.out.printf("[RETRY SUCCESS] Customer %d joined queue %d after waiting%n",
                                    customer.id, chosenQueue + 1);
                            served = true;
                        }
                    }
                    
                    if (!served) {
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

    private List<Integer> findShortestQueues() {
        List<Integer> shortestQueues = new ArrayList<>();
        int minSize = Integer.MAX_VALUE;

        for (int i = 0; i < queues.size(); i++) {
            int currentSize = queues.get(i).size();
            if (currentSize < queueCapacity) {
                if (currentSize < minSize) {
                    minSize = currentSize;
                    shortestQueues.clear();
                    shortestQueues.add(i);
                } else if (currentSize == minSize) {
                    shortestQueues.add(i);
                }
            }
        }
        return shortestQueues;
    }

    private void checkoutCounter(int counterId) {
        Random random = new Random();
        
        while (simulationRunning || !queues.get(counterId).isEmpty()) {
            try {
                Customer customer = queues.get(counterId).poll(100, TimeUnit.MILLISECONDS);
                if (customer != null) {
                    int serviceTime = random.nextInt(301) + 300;
                    
                    System.out.printf("[SERVICE] Counter %d serving customer %d for %d seconds%n",
                            counterId + 1, customer.id, serviceTime);
                    Thread.sleep(serviceTime);
                    
                    totalServed.incrementAndGet();
                    totalServiceTime.addAndGet(serviceTime);
                    System.out.printf("[COMPLETE] Counter %d finished customer %d%n",
                            counterId + 1, customer.id);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private String getQueueSizes() {
        List<String> sizes = new ArrayList<>();
        for (int i = 0; i < queues.size(); i++) {
            sizes.add("Q" + (i + 1) + ":" + queues.get(i).size());
        }
        return String.join(", ", sizes);
    }

    private void printStatistics() {
        System.out.println("\n=== GROCERY STORE SIMULATION RESULTS ===");
        System.out.println("Total customers arrived: " + totalArrived.get());
        System.out.println("Total customers served: " + totalServed.get());
        System.out.println("Customers who left unserved: " + totalLeft.get());
        
        if (totalServed.get() > 0) {
            double avgServiceTime = totalServiceTime.get() / (double) totalServed.get();
            System.out.printf("Average service time: %.2f seconds%n", avgServiceTime);
        } else {
            System.out.println("Average service time: 0.00 seconds");
        }
        
        System.out.println("Final queue status: " + getQueueSizes());
    }

    private static class Customer {
        final int id;

        Customer(int id) {
            this.id = id;
        }
    }
}