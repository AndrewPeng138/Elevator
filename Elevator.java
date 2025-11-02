import java.util.*;

public class Elevator {
    private int currentFloor;
    private Direction direction;
    private State state;
    private final int minFloor;
    private final int maxFloor;
    private final int capacity;
    private int currentLoad;
    private double currentWeight;
    private final double maxWeight;
    private Map<Integer, Integer> passengerDestinations;
    private Map<Integer, Double> passengerWeights;
    private TreeSet<Integer> upStops;
    private TreeSet<Integer> downStops;
    
    public enum Direction {
        UP, DOWN, IDLE
    }
    
    public enum State {
        MOVING, STOPPED, DOORS_OPEN, DOORS_CLOSED
    }
    
    public Elevator(int minFloor, int maxFloor, int capacity) {
        this(minFloor, maxFloor, capacity, 2000.0);
    }
    
    public Elevator(int minFloor, int maxFloor, int capacity, double maxWeight) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.capacity = capacity;
        this.maxWeight = maxWeight;
        this.currentFloor = 1;
        this.direction = Direction.IDLE;
        this.state = State.STOPPED;
        this.currentLoad = 0;
        this.currentWeight = 0.0;
        this.passengerDestinations = new HashMap<>();
        this.passengerWeights = new HashMap<>();
        this.upStops = new TreeSet<>();
        this.downStops = new TreeSet<>(Collections.reverseOrder());
    }
    
    public Elevator() {
        this(1, 10, 8, 2000.0);
    }
    
    public void addPassengerWithDestination(int passengerNum, int destinationFloor, double weight) {
        if (destinationFloor < minFloor || destinationFloor > maxFloor) {
            System.out.println("Invalid floor: " + destinationFloor);
            return;
        }
        
        if (destinationFloor == currentFloor) {
            System.out.println("Passenger " + passengerNum + " is already at floor " + destinationFloor);
            return;
        }
        
        passengerDestinations.put(passengerNum, destinationFloor);
        passengerWeights.put(passengerNum, weight);
        System.out.println("Passenger " + passengerNum + " (" + String.format("%.1f", weight) + " lbs) wants floor " + destinationFloor);
    }
    
    public void startJourney() {
        if (passengerDestinations.isEmpty()) {
            System.out.println("No destinations requested.");
            return;
        }
        
        organizeStops();
        determineInitialDirection();
        processStops();
    }
    
    private void organizeStops() {
        upStops.clear();
        downStops.clear();
        
        Integer firstPassengerFloor = passengerDestinations.get(1);
        Direction initialDir = Direction.IDLE;
        
        if (firstPassengerFloor != null) {
            if (firstPassengerFloor > currentFloor) {
                initialDir = Direction.UP;
            } else if (firstPassengerFloor < currentFloor) {
                initialDir = Direction.DOWN;
            }
        }
        
        if (initialDir == Direction.UP) {
            for (int floor : passengerDestinations.values()) {
                upStops.add(floor);
            }
        } else if (initialDir == Direction.DOWN) {
            for (int floor : passengerDestinations.values()) {
                downStops.add(floor);
            }
        }
        
        System.out.println("\nAll destination floors: " + passengerDestinations.values());
        System.out.println("Initial direction (set by Person 1): " + initialDir);
    }
    
    private void determineInitialDirection() {
        if (upStops.isEmpty() && downStops.isEmpty()) {
            direction = Direction.IDLE;
            return;
        }
        
        if (!upStops.isEmpty()) {
            direction = Direction.UP;
        } else if (!downStops.isEmpty()) {
            direction = Direction.DOWN;
        }
    }
    
    private void processStops() {
        state = State.MOVING;
        
        if (direction == Direction.UP) {
            processUpStops();
        } else if (direction == Direction.DOWN) {
            processDownStops();
        }
        
        direction = Direction.IDLE;
        state = State.STOPPED;
        System.out.println("\nAll passengers delivered!");
    }
    
    private void processDownStops() {
        System.out.println("\n=== Going DOWN ===");
        System.out.println("Stops: " + downStops);
        
        while (!downStops.isEmpty()) {
            int nextFloor = downStops.first();
            downStops.remove(nextFloor);
            moveToFloor(nextFloor, Direction.DOWN);
            stopAndUnload(nextFloor);
        }
    }
    
    private void processUpStops() {
        System.out.println("\n=== Going UP ===");
        System.out.println("Stops: " + upStops);
        
        while (!upStops.isEmpty()) {
            int nextFloor = upStops.first();
            upStops.remove(nextFloor);
            moveToFloor(nextFloor, Direction.UP);
            stopAndUnload(nextFloor);
        }
    }
    
    private void moveToFloor(int targetFloor, Direction dir) {
        while (currentFloor != targetFloor) {
            if (dir == Direction.UP) {
                currentFloor++;
            } else {
                currentFloor--;
            }
            System.out.println("  Passing floor " + currentFloor + "...");
            simulateDelay(1000);
        }
    }
    
    private void stopAndUnload(int floor) {
        state = State.STOPPED;
        System.out.println("\n*** ARRIVED at floor " + floor + " ***");
        openDoors();
        
        List<Integer> exitingPassengers = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : passengerDestinations.entrySet()) {
            if (entry.getValue() == floor) {
                exitingPassengers.add(entry.getKey());
            }
        }
        
        if (!exitingPassengers.isEmpty()) {
            Collections.sort(exitingPassengers);
            for (int passengerNum : exitingPassengers) {
                double weight = passengerWeights.get(passengerNum);
                System.out.println("  -> Person " + passengerNum + " exits (" + String.format("%.1f", weight) + " lbs)");
                
                // Remove passenger and their weight
                passengerDestinations.remove(passengerNum);
                passengerWeights.remove(passengerNum);
                currentLoad--;
                currentWeight -= weight;
            }
            
            System.out.println("  Current load: " + currentLoad + "/" + capacity + " people");
            System.out.println("  Current weight: " + String.format("%.1f", currentWeight) + "/" + String.format("%.1f", maxWeight) + " lbs");
        }
        
        simulateDelay(3000);
        closeDoors();
        state = State.MOVING;
    }
    
    public void openDoors() {
        state = State.DOORS_OPEN;
        System.out.println("  [Doors opening...]");
    }
    
    public void closeDoors() {
        state = State.DOORS_CLOSED;
        System.out.println("  [Doors closing...]");
    }
    
    public void addPassengers(int count, double totalWeight) {
        if (currentLoad + count > capacity) {
            System.out.println("Cannot add " + count + " passengers. Would exceed capacity of " + capacity);
            return;
        }
        
        if (currentWeight + totalWeight > maxWeight) {
            System.out.println("Cannot add passengers. Would exceed weight limit.");
            System.out.println("Current weight: " + String.format("%.1f", currentWeight) + " lbs");
            System.out.println("Adding: " + String.format("%.1f", totalWeight) + " lbs");
            System.out.println("Max weight: " + String.format("%.1f", maxWeight) + " lbs");
            return;
        }
        
        currentLoad += count;
        currentWeight += totalWeight;
        System.out.println("\n" + count + " passengers entered.");
        System.out.println("Current load: " + currentLoad + "/" + capacity + " people");
        System.out.println("Current weight: " + String.format("%.1f", currentWeight) + "/" + String.format("%.1f", maxWeight) + " lbs");
    }
    
    public void emergencyStop() {
        upStops.clear();
        downStops.clear();
        passengerDestinations.clear();
        passengerWeights.clear();
        state = State.STOPPED;
        direction = Direction.IDLE;
        System.out.println("EMERGENCY STOP activated at floor " + currentFloor);
    }
    
    private void simulateDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public int getCurrentFloor() {
        return currentFloor;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public State getState() {
        return state;
    }
    
    public int getCurrentLoad() {
        return currentLoad;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public double getCurrentWeight() {
        return currentWeight;
    }
    
    public double getMaxWeight() {
        return maxWeight;
    }
    
    @Override
    public String toString() {
        return String.format("Elevator[Floor: %d, Direction: %s, State: %s, Load: %d/%d]",
                currentFloor, direction, state, currentLoad, capacity);
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Elevator Simulation ===\n");
        
        System.out.println("--- Building Configuration ---");
        System.out.print("Enter number of floors in the building: ");
        int numFloors = scanner.nextInt();
        
        if (numFloors <= 1) {
            System.out.println("Invalid number of floors. Must be greater than 1.");
            scanner.close();
            return;
        }
        
        System.out.println("\n--- Elevator Configuration ---");
        System.out.print("Enter maximum occupancy (number of people): ");
        int maxOccupancy = scanner.nextInt();
        
        if (maxOccupancy <= 0) {
            System.out.println("Invalid occupancy. Must be greater than 0.");
            scanner.close();
            return;
        }
        
        System.out.print("Enter maximum weight capacity (in lbs): ");
        double maxWeight = scanner.nextDouble();
        
        if (maxWeight <= 0) {
            System.out.println("Invalid weight capacity. Must be greater than 0.");
            scanner.close();
            return;
        }
        
        System.out.println("\nBuilding & Elevator configured:");
        System.out.println("  Floors: 1 to " + numFloors);
        System.out.println("  Max occupancy: " + maxOccupancy + " people");
        System.out.println("  Max weight: " + maxWeight + " lbs");
        System.out.println();
        
        Elevator elevator = new Elevator(1, numFloors, maxOccupancy, maxWeight);
        System.out.println("Elevator starting at floor " + elevator.getCurrentFloor() + "\n");
        
        System.out.print("How many people want to enter the elevator? ");
        int numPeople = scanner.nextInt();
        
        if (numPeople <= 0) {
            System.out.println("No passengers to add. Exiting simulation.");
            scanner.close();
            return;
        }
        
        if (numPeople > elevator.getCapacity()) {
            System.out.println("Too many people! Elevator capacity is " + elevator.getCapacity());
            scanner.close();
            return;
        }
        
        double totalWeight = 0.0;
        Map<Integer, Integer> passengerFloors = new HashMap<>();
        Map<Integer, Double> passengerWeightMap = new HashMap<>();
        
        for (int i = 1; i <= numPeople; i++) {
            System.out.println("\n--- Person " + i + " ---");
            
            System.out.print("Enter weight (in lbs): ");
            double weight = scanner.nextDouble();
            
            if (weight <= 0) {
                System.out.println("Invalid weight. Please enter a positive number.");
                i--;
                continue;
            }
            
            totalWeight += weight;
            passengerWeightMap.put(i, weight);
            
            System.out.print("Enter destination floor (1-" + numFloors + "): ");
            int floor = scanner.nextInt();
            
            if (floor < 1 || floor > numFloors) {
                System.out.println("Invalid floor. Please enter a floor between 1 and " + numFloors + ".");
                i--;
                totalWeight -= weight;
                passengerWeightMap.remove(i);
                continue;
            }
            
            passengerFloors.put(i, floor);
        }
        
        System.out.println("\n=== Summary ===");
        System.out.println("Total passengers: " + numPeople);
        System.out.println("Total weight: " + String.format("%.1f", totalWeight) + " lbs");
        System.out.println("Destinations:");
        for (Map.Entry<Integer, Integer> entry : passengerFloors.entrySet()) {
            int personNum = entry.getKey();
            double weight = passengerWeightMap.get(personNum);
            System.out.println("  Person " + personNum + " (" + String.format("%.1f", weight) + " lbs) -> Floor " + entry.getValue());
        }
        
        elevator.addPassengers(numPeople, totalWeight);
        
        if (elevator.getCurrentLoad() > 0) {
            System.out.println("\n" + "=".repeat(40));
            
            for (Map.Entry<Integer, Integer> entry : passengerFloors.entrySet()) {
                int personNum = entry.getKey();
                double weight = passengerWeightMap.get(personNum);
                elevator.addPassengerWithDestination(personNum, entry.getValue(), weight);
            }
            
            System.out.println("\n" + "=".repeat(40));
            elevator.startJourney();
            
            System.out.println("\n=== Simulation Complete ===");
            System.out.println("Final state: " + elevator);
        }
        
        scanner.close();
    }
}