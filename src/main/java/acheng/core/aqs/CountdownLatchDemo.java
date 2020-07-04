package acheng.core.aqs;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 使用CountdownLatch模拟多线程加载资源，主线程等待加载完成
 */
public class CountdownLatchDemo {

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        Random random = new Random();
        System.out.println("资源加载中：");
        String[] progress = new String[10];
        for (int j = 0 ; j < 10; j++) {
            final int k = j;
            pool.submit(() -> {
                for (int i = 1; i <= 100; i++) {
                    progress[k] = i + "%";
                    try {
                        TimeUnit.MILLISECONDS.sleep(random.nextInt(100));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.print("\r" + Arrays.toString(progress));
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\r\n资源加载完成");
    }
}
