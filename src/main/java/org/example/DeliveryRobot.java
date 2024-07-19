package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DeliveryRobot {
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    public static final Object lock = new Object();

    private static int countRotationCommands(String route) {
        int countR = 0;
        for (int i = 0; i < route.length(); i++) {
            if (route.charAt(i) == 'R') {
                countR++;
            }
        }
        return countR;
    }

    public static synchronized void updateFrequencyMap(int countR) {
        sizeToFreq.put(countR, sizeToFreq.getOrDefault(countR, 0) + 1);
    }

    private static synchronized void printFrequencyResults() {
        int maxFrequency = 0;
        for (int freq : sizeToFreq.values()) {
            if (freq > maxFrequency) {
                maxFrequency = freq;

            }

        }
        System.out.println("Другие размеры:");
        sizeToFreq.forEach((size, freq) -> System.out.println("- " + size + " (встретилось " + freq + " раз)"));
    }

    private static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();

        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        Thread firstThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    synchronized (lock) {
                        while (sizeToFreq.isEmpty()) {
                            lock.wait();
                        }

                        int maxFrequency = sizeToFreq.values().stream()
                                .mapToInt(v -> v)
                                .max()
                                .orElse(0);
                        int count = (int) sizeToFreq.values().stream()
                                .filter(v -> v == maxFrequency)
                                .count();
                        System.out.println("Самое частое количество повторений " + maxFrequency + " (встретилось " + count + " раз)");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }
        });

        firstThread.start();

        Thread[] threads = new Thread[1000];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                String route = generateRoute("RLRFR", 100);

                int countR = countRotationCommands(route);

                synchronized (lock) {
                    updateFrequencyMap(countR);
                    System.out.println("количество команд поворота направо: " + countR);
                    lock.notifyAll();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        firstThread.interrupt();

        printFrequencyResults();
    }
}




