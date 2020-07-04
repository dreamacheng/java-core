package acheng.core.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

@Slf4j
public class AQSDemo {

    public static void main(String[] args) {
        SpecLock lock = new SpecLock();
        new Thread(() -> {
           lock.lock();
           try {
               log.debug("locking....");
               TimeUnit.SECONDS.sleep(3000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           } finally {
               log.debug("unlocking...");
               lock.unlock();
           }
        }, "t1").start();

        new Thread(() -> {
            try {
                if (lock.tryLock(2, TimeUnit.SECONDS)) {
                    try {
                        log.debug("locking....");
                    } finally {
                        log.debug("unlocking....");
                        lock.unlock();
                    }
                } else {
                    log.debug("give up....");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t2").start();
    }

}

// 自定义锁（不可重入）
class SpecLock implements Lock {

    private SpecSync sync = new SpecSync();

    // 加锁
    @Override
    public void lock() {
        sync.acquire(1);
    }

    // 加锁， 可打断
    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    // 尝试加锁， once
    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    // 尝试加锁， 带超时
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    // 解锁
    @Override
    public void unlock() {
        sync.release(1);
    }

    // 条件变量
    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }

    class SpecSync extends AbstractQueuedSynchronizer {

        @Override
        protected boolean tryAcquire(int arg) {
            // 加锁成功
            if (compareAndSetState(0, 1)) {
                // 设置owner为当前线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            setExclusiveOwnerThread(null);
            // state被 volatile 修饰， 由写屏障保证指令不重排序
            setState(0);
            return true;
        }

        // 是否持有独占锁
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        public Condition newCondition() {
            return new ConditionObject();
        }
    }
}
