package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        // Task 1
        Random rand = new Random();
        CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
        Thread t1 = new Thread(() -> {
            synchronized (list) {
                for (int i = 0; i < 10; i++) {
                    list.add(rand.nextInt(20) - 10);
                }
            }
        });
        t1.start();
        AtomicReference<Double> avg = new AtomicReference<>((double) 0);
        AtomicInteger sum = new AtomicInteger(0);

        Thread t2 = new Thread(() -> {
            synchronized (list) {
                sum.set(list.stream().mapToInt(item -> item).sum());
            }
        });

        Thread t3 = new Thread(() -> {
            synchronized (list) {
                avg.set(list.stream().mapToDouble(item -> (double) item).average().orElse(0.0));
            }
        });

        t1.join();
        t2.start();
        t3.start();
        t2.join();
        t3.join();

        System.out.println(list);
        System.out.println("average: " + avg.get());
        System.out.println("summary: " + sum.get());

        // Task 2
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the file path:");
        String filePath = scanner.nextLine();
        String noExtensionPath;
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex != -1) {
            noExtensionPath = filePath.substring(0, dotIndex);
        } else {
            noExtensionPath = filePath;
        }
        long startTime = System.currentTimeMillis();
        Thread thread1 = new Thread(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (int i = 0; i < 10; i++) {
                    writer.write(rand.nextInt(10) + "\n");
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });

        thread1.start();

        List<Integer> numbers = new ArrayList<>();
        Thread thread2 = new Thread(() -> {
            try {
                numbers.addAll(readNumbersFromFile(filePath));
                List<Integer> primes = findPrimes(numbers);
                writeNumbersToFile(noExtensionPath + "Primes.txt", primes);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });

        Thread thread3 = new Thread(() -> {
            try {
                numbers.addAll(readNumbersFromFile(filePath));
                List<Long> factorials = calculateFactorials(numbers);
                writeNumbersToFile(noExtensionPath + "Factorials.txt", factorials);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
        thread1.join();
        thread2.start();
        thread3.start();
        thread2.join();
        thread3.join();

        long endTime = System.currentTimeMillis();

        System.out.println("Processing complete.");
        System.out.println("Time taken: " + (endTime - startTime) + " ms");
    }

    private static List<Integer> readNumbersFromFile(String filePath) throws IOException {
        List<Integer> numbers = new ArrayList<>();
        Files.lines(Path.of(filePath)).forEach(line -> numbers.add(Integer.parseInt(line)));
        return numbers;
    }

    private static void writeNumbersToFile(String filePath, List<?> numbers) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Object number : numbers) {
                writer.write(number.toString() + "\n");
            }
        }
    }

    private static List<Integer> findPrimes(List<Integer> numbers) {
        List<Integer> primes = new ArrayList<>();
        for (int number : numbers) {
            if (isPrime(number)) {
                primes.add(number);
            }
        }
        return primes;
    }

    private static boolean isPrime(int number) {
        if (number <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }

    private static List<Long> calculateFactorials(List<Integer> numbers) {
        List<Long> factorials = new ArrayList<>();
        for (int number : numbers) {
            factorials.add(factorial(number));
        }
        return factorials;
    }

    private static long factorial(int number) {
        if (number < 0) {
            return 0; // Factorial of negative numbers is not defined
        }
        long result = 1;
        for (int i = 1; i <= number; i++) {
            result *= i;
        }
        return result;
    }
}
