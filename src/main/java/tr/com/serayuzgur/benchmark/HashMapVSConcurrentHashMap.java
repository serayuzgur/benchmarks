package tr.com.serayuzgur.benchmark;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class inserts lots of records  to {@link java.util.HashMap} and {@link java.util.concurrent.ConcurrentHashMap}.
 * Tries to get values with 100 threads at the same time.
 * <p/>
 * Main task is to benchmark performance under multi-threaded situations for <b>only get</b> operations.
 */
public class HashMapVSConcurrentHashMap {

    private static Map<String, Object> hashMap = new HashMap<String, Object>();
    private static Map<String, Object> cHashMap = new HashMap<String, Object>();
    private static String[] keys;
    private static AtomicInteger activeThreads = new AtomicInteger(0);

    public static void main(String[] args) {
        int threadSize = 100;
        prepare(1000000);

        //Warm up
        System.out.println("---Warming up---");
        for (int i = 0; i < 3; i++) {
            test(threadSize);
        }

        System.out.println("---Starting  up---");
        for (int i = 1; i < 11; i++) {
            System.out.println("----Pass" + i + "---");
            test(threadSize);
        }

    }

    private static void test(int threadSize){
        Thread[] threads = createThreads(threadSize, cHashMap);
        long start = System.nanoTime();
        runAll(threads);
        System.out.println("ConcurrentHashMap " + (System.nanoTime() - start) / 1000);
        waitThreads();

        threads = createThreads(threadSize, hashMap);
        start = System.nanoTime();
        runAll(threads);
        System.out.println("HashMap           " + (System.nanoTime() - start) / 1000);
        waitThreads();

    }
    private static void waitThreads() {
        while (activeThreads.get() != 0){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }


    private static void prepare(int size) {
        fillMapWith(hashMap, size);
        cHashMap.putAll(hashMap);
        keys = new String[size];
        hashMap.keySet().toArray(keys);
        //Now two maps are filled with same size entries
        //Keys are cached for get operation

    }

    private static Thread[] createThreads(int size, Map<String, Object> map) {
        Thread[] threads = new Thread[size];
        for (int i = 0; i < size; i++) {
            threads[i] = new Thread(new GetRunnable(map));
            threads[i].setPriority(Thread.MAX_PRIORITY);
        }
        return threads;
    }

    private static void runAll(Thread[] threads) {
        for (Thread thread : threads)
            thread.start();
    }


    private static void fillMapWith(Map<String, Object> map, int size) {
        for (int i = 0; i < size; i++) {
            String key = generateKey();
            map.put(key, key);
        }
    }


    /*
    Possible chars for a rest service path.
     */
    private static char[] chars = "abcdefghijklmnopqrstuvwxyz/".toCharArray();

    private static String generateKey() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }


    private static class GetRunnable implements Runnable {
        Map<String, Object> map;
        int size;

        public GetRunnable(Map<String, Object> map) {
            this.map = map;
            this.size = map.size();
        }

        public void run() {
            long start = System.nanoTime();

            activeThreads.incrementAndGet();
            for (int i = 0; i < size; i++) {
                map.get(keys[i]);
            }
            activeThreads.decrementAndGet();

        }
    }

}
