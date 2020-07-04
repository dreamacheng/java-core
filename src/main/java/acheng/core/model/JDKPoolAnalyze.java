package acheng.core.model;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JDKPoolAnalyze {

    public static void main(String[] args) {

        /**
         * 构造方法： 初始指定最大线程数与核心线程数，无空闲线程
         *  LinkedBlockingQueue作为任务队列，无界
         *  当核心线程数达到最大线程数，再次提交的任务放入queue等待
         *  当任务提交过多，堆积大量任务，OOM
         */
//        ExecutorService pool = Executors.newFixedThreadPool(5);

        /**
         * 构造方法： 核心线程数为零，最大线程数为 Integer.MAX_VALUE， 最大空闲时间 60s
         * SynchronousQueue作为任务队列，该队列不为队列元素维护存储空间
         * 当有空闲线程时，复用已创建的线程
         * 当无空闲线程时，创建新的线程执行任务
         * 当提交的任务过多时，不停的创建线程，OOM， 电脑直接跑黑屏。。。。。。
         */
//        ExecutorService pool1 = Executors.newCachedThreadPool();

        /**
         * 构造方法： 核心线程数与最大线程数均为 1，无空闲线程
         * LinkedBlockingQueue作为任务队列，无界
         * 只创建一个线程，串行的执行任务队列中的任务
         * 当任务提交过多，堆积大量任务，OOM
         */
//        ExecutorService pool2 = Executors.newSingleThreadExecutor();

        /**
         * 构造方法： 初始指定核心线程数，最大线程数为 Integer.MAX_VALUE， *空闲线程超时时间为0,表示池内不存在空闲线程.
         * DelayedWorkQueue作为任务队列，无界
         *
         * //初始延时执行
         * schedule(Runnable command, ong delay, TimeUnit unit)
         *
         * 连续周期性执行，从上一个任务开始执行时计算延迟多少开始执行下一个任务
         * scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
         *
         * 连续周期性执行，从上一个任务全部执行完成时计算延迟多少开始执行下一个任务
         * scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
         */
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

        /*for (int i = 0; i < Integer.MAX_VALUE; i++) {
            final int j = i;
            pool1.submit(() -> {
                log.debug("thread[{}]: task execute begin...", j);
                try {
                    TimeUnit.MILLISECONDS.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("thread[{}]: task execute end...", j);
            });
        }*/

        for (int i = 0; i < 100; i++) {
            final int j = i;
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                log.debug("task[{}] execute begin...", j);
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //log.debug("thread[{}]: task execute end...", j);
            }, 1000, 1000, TimeUnit.MILLISECONDS);
        }

    }
}
