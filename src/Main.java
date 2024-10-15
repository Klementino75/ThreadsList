import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final byte SIZE_QUEUE = 100;
    private static final int SIZE_TEXT = 100;
    private static final int LENGTH_TEXT = 100_000;
    private static final String LETTERS = "abc";

    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(SIZE_QUEUE);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(SIZE_QUEUE);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(SIZE_QUEUE);

    private static final AtomicInteger ATOMIC_A = new AtomicInteger(0);
    private static final AtomicInteger ATOMIC_B = new AtomicInteger(0);
    private static final AtomicInteger ATOMIC_C = new AtomicInteger(0);

    private static volatile int maxA = 0;
    private static volatile int maxB = 0;
    private static volatile int maxC = 0;

    private static String maxStrA = "";
    private static String maxStrB = "";
    private static String maxStrC = "";

    public static void main(String[] args) throws InterruptedException {
        Thread threadQueue = new Thread(() -> {
            for (int i = 0; i < SIZE_TEXT; i++) {
                try {
                    queueA.put(generateText(LETTERS, LENGTH_TEXT));
                    queueB.put(generateText(LETTERS, LENGTH_TEXT));
                    queueC.put(generateText(LETTERS, LENGTH_TEXT));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        System.out.println("\nСоздание Queues.");
        threadQueue.start();
        System.out.println("Старт!");
        try {
            threadQueue.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Queues созданы.");
        System.out.println("Считаю...");
        Thread.sleep(1000);

        Thread threadCountA = new Thread(() -> CalcSymbol(queueA, 'a')); // 'a'
        threadCountA.start();
        Thread threadCountB = new Thread(() -> CalcSymbol(queueB, 'b')); // 'b'
        threadCountB.start();
        Thread threadCountC = new Thread(() -> CalcSymbol(queueC, 'c')); // 'c'
        threadCountC.start();
        try {
            threadCountA.join();
            threadCountB.join();
            threadCountC.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Thread.sleep(1000);
        System.out.printf("\nСтрока с максимальным количеством символа 'a' (%d шт.):\n", maxA);
        System.out.println(maxStrA);
        System.out.printf("\nСтрока с максимальным количеством символа 'b' (%d шт.):\n", maxB);
        System.out.println(maxStrB);
        System.out.printf("\nСтрока с максимальным количеством символа 'c' (%d шт.):\n", maxC);
        System.out.println(maxStrC);
    }

    public static void CalcSymbol(BlockingQueue<String> queue, char symbol) {
        String s = "";

        for (int i = 0; i < SIZE_TEXT; i++) {
            try {
                s = queue.take();
                for (char c : s.toCharArray()) {
                    if (c == symbol) {
                        switch (symbol) {
                            case 'a' -> ATOMIC_A.incrementAndGet();
                            case 'b' -> ATOMIC_B.incrementAndGet();
                            case 'c' -> ATOMIC_C.incrementAndGet();
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            switch (symbol) {
                case 'a': {
                    if (maxA < ATOMIC_A.get()) {
                        maxA = ATOMIC_A.get();
                        maxStrA = s;
                    }
                    ATOMIC_A.set(0);
                }
                case 'b': {
                    if (maxB < ATOMIC_B.get()) {
                        maxB = ATOMIC_B.get();
                        maxStrB = s;
                    }
                    ATOMIC_B.set(0);
                }
                case 'c': {
                    if (maxC < ATOMIC_C.get()) {
                        maxC = ATOMIC_C.get();
                        maxStrC = s;
                    }
                    ATOMIC_C.set(0);
                }
            }
        }
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}