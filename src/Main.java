import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final byte SIZE_QUEUE = 100;
    private static final int SIZE_TEXT = 10_000;
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

    public static void main(String[] args) throws InterruptedException {
        Thread threadQueue = new Thread(() -> {
            for (int i = 0; i < SIZE_TEXT; i++) {
                try {
                    queueA.put(generateText(LETTERS, LENGTH_TEXT));
                    queueB.put(generateText(LETTERS, LENGTH_TEXT));
                    queueC.put(generateText(LETTERS, LENGTH_TEXT));
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }
        });
        System.out.println("Создание Queues.");
        threadQueue.start();
        System.out.println("Старт!");
//        threadQueue.join();
        System.out.println("Queues созданы.");
        Thread.sleep(1000);
        System.out.println("Считаю...");

        new Thread(() -> CalcSymbol(queueA, 'a')).start(); // 'a'
//        threadCountA.start();

        Thread threadCountB = new Thread(() -> CalcSymbol(queueB, 'b')); // 'b'
        threadCountB.start();

        Thread threadCountC = new Thread(() -> CalcSymbol(queueC, 'c')); // 'c'
        threadCountC.start();

//        threadCountA.join();
        threadCountB.join();
        threadCountC.join();

        System.out.printf("\nВ строке с максимальным количеством символа 'a': %d шт.", maxA);
        System.out.printf("\nВ строке с максимальным количеством символа 'b': %d шт.", maxB);
        System.out.printf("\nВ строке с максимальным количеством символа 'c': %d шт.\n", maxC);
    }

    public static void CalcSymbol(BlockingQueue<String> queue, char symbol) {
        String s;

        for (int i = 0; i < SIZE_TEXT; i++) {
            try {
                s = queue.take();
                for (char c : s.toCharArray()) {
                    if (c == symbol) {
                        incrementCount(symbol);
                    }
                }
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
            ifSymbol(symbol);
        }
    }

    public static void incrementCount(char c) {
        switch (c) {
            case 'a' -> ATOMIC_A.incrementAndGet();
            case 'b' -> ATOMIC_B.incrementAndGet();
            case 'c' -> ATOMIC_C.incrementAndGet();
        }
    }

    public static void ifSymbol(char c) {
        switch (c) {
            case 'a': {
                if (maxA < ATOMIC_A.get()) {
                    maxA = ATOMIC_A.get();
                }
                ATOMIC_A.set(0);
            }
            case 'b': {
                if (maxB < ATOMIC_B.get()) {
                    maxB = ATOMIC_B.get();
                }
                ATOMIC_B.set(0);
            }
            case 'c':{
                if (maxC < ATOMIC_C.get()) {
                    maxC = ATOMIC_C.get();
                }
                ATOMIC_C.set(0);
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