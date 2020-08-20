package acheng.core.concurrently;

import lombok.extern.log4j.Log4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DynamicThreadPool {

    public static void main(String[] args) throws InterruptedException {
        dynamicModifyExecutor();
    }

    /**
     * 自定义线程池
     * @return
     */
    private static ThreadPoolExecutor buildThreadPoolExecutor() {
        return new ThreadPoolExecutor(2,
                5,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                new NamedThreadFactory());
    }

    /**
     * 先提交任务给线程池， 并修改线程池参数
     * @throws InterruptedException
     */
    private static void dynamicModifyExecutor() throws InterruptedException {
        ThreadPoolExecutor executor = buildThreadPoolExecutor();
        for (int i = 0; i < 15; i++) {
            executor.submit(() -> {
                threadPoolStatus(executor, "create task");
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        threadPoolStatus(executor, "change before");
        executor.setCorePoolSize(10);
        executor.setMaximumPoolSize(10);
        threadPoolStatus(executor, "change after");
        Thread.currentThread().join();
    }

    /**
     * 打印线程池状态
     * @param executor
     * @param name
     */
    private static void threadPoolStatus(ThreadPoolExecutor executor, String name) {
        LinkedBlockingQueue queue = (LinkedBlockingQueue) executor.getQueue();
        System.out.println(Thread.currentThread().getName() + "-" + name + "-:" +
                "核心线程数: " + executor.getCorePoolSize() +
                " 活动线程数: " + executor.getActiveCount() +
                "最大线程数: " + executor.getMaximumPoolSize() +
                " 线程池活跃度: " +
                            divide(executor.getActiveCount(), executor.getMaximumPoolSize()) +
                " 任务完成数: " + executor.getCompletedTaskCount() +
                " 队列大小: " + (queue.size() + queue.remainingCapacity()) +
                " 当前排队线程数: " + queue.size() +
                " 队列剩余大小: " + queue.remainingCapacity() +
                " 队列使用度: " + divide(queue.size(), queue.size() + queue.remainingCapacity()));
    }

    /**
     * 保留两位小数
     * @param num1
     * @param num2
     * @return
     */
    private static String divide(int num1, int num2) {
        return String.format("%1.2f%%",
                Double.parseDouble(num1 + "") / Double.parseDouble(num2 + "") * 100);
    }
}

/**
 * 通过工厂构建线程
 */
class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    NamedThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
    }
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
