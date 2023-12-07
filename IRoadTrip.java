import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class Node implements Comparable<Node> {
    String country;
    double distance;

    Node(String country, double distance) {
        this.country = country;
        this.distance = distance;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.distance, other.distance);
    }
}

class Graph {
    final Map<String, List<Node>> adjacencyList = new HashMap<>();


    void addCountry(String country) {
        adjacencyList.put(country, new ArrayList<>());
    }

    void addEdge(String countryA, String countryB, double distance) {
        adjacencyList.get(countryA).add(new Node(countryB, distance));
        adjacencyList.get(countryB).add(new Node(countryA, distance));
    }

    void replaceEdgeDistance(String countryA, String countryB, double newDistance) {
        List<Node> neighborsA = adjacencyList.get(countryA);
        if (neighborsA != null) {
            for (Node node : neighborsA) {
                if (node.country.equals(countryB)) {
                    node.distance = newDistance;
                    break;
                }
            }
        }

        List<Node> neighborsB = adjacencyList.get(countryB);
        if (neighborsB != null) {
            for (Node node : neighborsB) {
                if (node.country.equals(countryA)) {
                    node.distance = newDistance;
                    break;
                }
            }
        }
    }


    boolean areAdjacent(String countryA, String countryB) {
        List<Node> neighbors = adjacencyList.get(countryA);
        return neighbors != null && neighbors.stream().anyMatch(node -> node.country.equals(countryB));
    }


    List<Node> dijkstra(String startCountry, String endCountry) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();

        for (String country : adjacencyList.keySet()) {
            distances.put(country, Double.MAX_VALUE);
            previous.put(country, null);
        }

        distances.put(startCountry, 0.0);
        priorityQueue.add(new Node(startCountry, 0));

        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();

            for (Node neighbor : adjacencyList.get(current.country)) {
                double newDistance = distances.get(current.country) + neighbor.distance;
                if (newDistance < distances.get(neighbor.country)) {
                    distances.put(neighbor.country, newDistance);
                    previous.put(neighbor.country, current.country);
                    priorityQueue.add(new Node(neighbor.country, newDistance));
                }
            }
        }

        List<Node> path = new ArrayList<>();
        String currentCountry = endCountry;

        while (currentCountry != null) {
            path.add(new Node(currentCountry, distances.get(currentCountry)));
            currentCountry = previous.get(currentCountry);
        }

        Collections.reverse(path);
        return path;
    }
}

public class IRoadTrip {
    private final Graph countryGraph = new Graph();
    private final Map<String, String> countryCodesMap = new HashMap<>();
    private final Map<String, String> fixedCountriesMap = createFixedCountries();

    public boolean isValidCountry(String countryName) {
        return countryGraph.adjacencyList.containsKey(countryName);
    }

    private static final double MAX_VALUE = Double.MAX_VALUE;

    public IRoadTrip(String bordersFile, String distancesFile, String codesFile) {
        try {
            loadGraph(bordersFile);
            loadDistances(distancesFile);
            loadCodes(codesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadGraph(String bordersFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(bordersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 4) {
                    String countryA = fixCountryName(parts[2]);
                    String countryB = fixCountryName(parts[3]);
                    countryGraph.addCountry(countryA);
                    countryGraph.addCountry(countryB);
                    countryGraph.addEdge(countryA, countryB, MAX_VALUE);
                    System.out.println("Added edge: " + countryA + " - " + countryB); // Debug print
                }
            }
        } catch (IOException e) {
            System.err.println("Reading file: " + bordersFile);
            e.printStackTrace();
            throw e;
        }
    }


    private void loadDistances(String distancesFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(distancesFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String countryA = fixCountryName(parts[1]);
                    String countryB = fixCountryName(parts[3]);
                    double distance = Double.parseDouble(parts[4]);
                    countryGraph.replaceEdgeDistance(countryA, countryB, distance);
                }
            }
        }
    }

    private void loadCodes(String codesFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(codesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 6) {
                    String countryCode = parts[1];
                    String countryName = fixCountryName(parts[2]);
                    countryCodesMap.put(countryCode, countryName);
                }
            }
        }
    }

    private String fixCountryName(String countryName) {
        return fixedCountriesMap.getOrDefault(countryName, countryName);
    }

    private Map<String, String> createFixedCountries() {
        Map<String, String> fixedCountries = new HashMap<>();
        fixedCountries.put("Greenland).", "Greenland");
        fixedCountries.put("U.S.A.", "USA");
        fixedCountries.put("US", "United States of America");
        fixedCountries.put("German Federal Republic", "Germany");
        fixedCountries.put("Bahamas", "Bahamas, The");
        fixedCountries.put("Macedonia (Former Yugoslav Republic of)", "Macedonia");
        fixedCountries.put("Bosnia-Herzegovina", "Bosnia and Herzegovina");
        fixedCountries.put("Congo, Democratic Republic of (Zaire)", "Democratic Republic of the Congo");
        fixedCountries.put("Zambia.", "Zambia");
        // Add more mappings as needed

        return fixedCountries;
    }

    public double getDistance(String countryA, String countryB) {
        countryA = fixCountryName(countryA);
        countryB = fixCountryName(countryB);

        if (!countryGraph.areAdjacent(countryA, countryB)) {
            return -1;
        }

        return countryGraph.dijkstra(countryA, countryB).get(0).distance;
    }

    public List<Node> findPath(String countryA, String countryB) {
        countryA = fixCountryName(countryA);
        countryB = fixCountryName(countryB);

        return countryGraph.dijkstra(countryA, countryB);
    }

    public void acceptUserInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter the first country (or 'exit' to quit):");
            String countryA = scanner.nextLine();

            if (countryA.equalsIgnoreCase("exit")) {
                break;
            }

            if (!isValidCountry(countryA)) {
                System.out.println("Invalid country name. Please enter a correct country name.");
                continue;
            }

            System.out.println("Enter the second country:");
            String countryB = scanner.nextLine();

            if (countryB.equalsIgnoreCase("exit")) {
                break;
            }

            if (!isValidCountry(countryB)) {
                System.out.println("Invalid country name. Please enter a correct country name.");
                continue;
            }

            double distance = getDistance(countryA, countryB);
            if (distance == -1) {
                System.out.println("No direct path or data available between " + countryA + " and " + countryB + ".");
            } else {
                System.out.println("The distance between " + countryA + " and " + countryB + " is " + distance + " km.");
            }
        }

        scanner.close();
    }


    public void printPath(List<Node> path) {
        System.out.println("Shortest path:");
        for (Node node : path) {
            System.out.println(node.country + " - " + node.distance + " km");
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            usage();
            System.exit(1);
        }
        IRoadTrip roadTrip = new IRoadTrip(args[0], args[1], args[2]);
        roadTrip.acceptUserInput();
    }

    public static void usage() {
        System.out.println("Usage: java IRoadTrip <bordersFile> <distancesFile> <codesFile>");
    }
}
// I tried everything and I failed. This code doesn't run, but I hope you can understand.