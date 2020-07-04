package acheng.core.model;

import lombok.AllArgsConstructor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  desc: 顺序执行
 *  使用三个线程交替打印 a,b,c各5次
 *  方法二： 使用await, signal 实现
 */
public class SequenceExecutorExecute2 {

    public static void main(String[] args) {
        SequencePrint2 lock = new SequencePrint2(5);
        Condition a = lock.newCondition();
        Condition b = lock.newCondition();
        Condition c = lock.newCondition();
        new Thread(() -> {
            lock.print("a", a, b);
        }).start();
        new Thread(() -> {
            lock.print("b", b, c);
        }).start();
        new Thread(() -> {
            lock.print("c", c, a);
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.lock();
        try {
            a.signal();
        } finally {
            lock.unlock();
        }
    }

}

@AllArgsConstructor
class SequencePrint2 extends ReentrantLock{
    // 循环打印次数
    private int loopNumber;

    public void print(String str, Condition current, Condition next) {
        for (int i = 0; i < loopNumber; ++i) {
            lock();
            try {
                current.await();
                System.out.print(str);
                next.signal();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                unlock();
            }
        }
    }
}
