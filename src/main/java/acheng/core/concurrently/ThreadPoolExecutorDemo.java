package acheng.core.concurrently;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ThreadPoolExecutorDemo {

    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool(1, 1000, TimeUnit.MILLISECONDS, 1, (taskQueue, task) -> {
            // 阻塞等待
            // taskQueue.put(task);
            // 超时等待
            // taskQueue.offer(task, 1500, TimeUnit.MILLISECONDS);
            // 直接丢弃任务
            // log.debug("丢弃任务：{}", task);
            // 抛出异常
            // throw new RuntimeException("队列已满," + task);
            // 调用者执行
            task.run();
        });
        for (int i = 0; i < 4; i++) {
            int j = i;
            threadPool.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("{}", j);
            });
        }
    }
}

@Slf4j
class ThreadPool {

    // 任务队列
    private BlockQueue<Runnable> taskQueue;

    // 线程集合
    private HashSet<Worker> workers = new HashSet<>();

    // 核心线程数
    private int coreSize;

    // 获取任务的超时时间
    private long timeout;

    // 超时时间单位
    private TimeUnit unit;

    // 拒绝策略
    private RejectPolicy<Runnable> rejectPolicy;

    public ThreadPool(int coreSize, long timeout, TimeUnit unit, int capacity, RejectPolicy<Runnable> rejectPolicy) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.unit = unit;
        this.taskQueue = new BlockQueue<>(capacity);
        this.rejectPolicy = rejectPolicy;
    }

    // 执行任务
    public void execute(Runnable task) {
        // 当任务数不超过 coreSize，直接交给worker 对象执行
        synchronized (workers) {
            if (workers.size() < coreSize) {
                Worker worker = new Worker(task);
                log.debug("新增 worker：{}", worker);
                workers.add(worker);
                worker.start();
            } else {
//                taskQueue.put(task);
                taskQueue.tryPut(rejectPolicy, task);
            }
        }
    }

    class Worker extends Thread {

        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public synchronized void run() {
            // 执行任务
            // 1） 当task 不为空时执行任务
            // 2） 当task执行完毕， 从任务队列中获取任务执行
//            while (task != null || (task = taskQueue.take()) != null) {
            while (task != null || (task = taskQueue.poll(timeout, unit)) != null) {
                try {
                    log.debug("执行任务...{}", task);
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
            synchronized (workers) {
                log.debug("移除worker...{}", this);
                workers.remove(this);
            }
        }
    }

}

/**
 * 拒绝策略
 * @param <T>
 */
@FunctionalInterface
interface RejectPolicy<T> {
    void reject(BlockQueue<T> taskQueue, T task);
}

@Slf4j
class BlockQueue<T> {

    // 任务队列
    private Deque<T> queue = new ArrayDeque<>();

    private ReentrantLock lock = new ReentrantLock();

    // 消费者条件变量
    private Condition emptyWaitSet = lock.newCondition();

    // 生产者条件变量
    private Condition fullWaitSet = lock.newCondition();

    // 队列大小
    private int capacity;

    // 初始化队列大小
    public <T> BlockQueue(int capacity) {
        this.capacity = capacity;
    }

    // 获取队列中任务数
    public int size() {
        lock.lock();
        try {
            return this.queue.size();
        } finally {
            lock.unlock();
        }
    }

    // 带超时的阻塞获取任务
    public T poll(long timeout, TimeUnit unit) {
        lock.lock();
        try {
            // 将超时时间统一转换为纳秒
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                try {
                    if (nanos <= 0) {
                        return null;
                    }
                    // 返回值为剩余等待时间
                    nanos = emptyWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T task = queue.removeFirst();
            fullWaitSet.signal();
            return task;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞获取任务
    public T take() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                try {
                    emptyWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T task = queue.removeFirst();
            fullWaitSet.signal();
            return task;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞添加任务
    public void put(T task) {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                try {
                    log.debug("等待加入队列 task: {}", task);
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入队列 task: {}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
        } finally {
            lock.unlock();
        }
    }

    // 带超时时间的阻塞添加任务
    public boolean offer(T task, long timeout, TimeUnit unit) {
        lock.lock();
        try {
            // 统一转换为纳秒
            long nanos = unit.toNanos(timeout);
            while (queue.size() == capacity) {
                try {
                    if (nanos <= 0) {
                        return false;
                    }
                    log.debug("等待加入队列 task: {}", task);
                    nanos = fullWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入队列 task: {}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {
        lock.lock();
        try {
            if (this.queue.size() == capacity) {
                // 调用者实现拒绝策略
                rejectPolicy.reject(this, task);
            } else {
                log.debug("加入队列 task: {}", task);
                queue.addLast(task);
                emptyWaitSet.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}
