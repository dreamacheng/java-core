package acheng.core.model;

import java.util.concurrent.locks.LockSupport;

/**
 *  desc: 顺序执行
 *  使用三个线程交替打印 a,b,c各5次
 *  方法三： 使用park，unpark 实现
 */
public class SequenceExecutorExecute3 {

    public static void main(String[] args) {
        SequencePrint3 printer = new SequencePrint3(5);
        Thread t1 = new Thread(() -> {
            printer.print("a");
        });
        Thread t2 = new Thread(() -> {
            printer.print("b");
        });
        Thread t3 = new Thread(() -> {
            printer.print("c");
        });
        printer.setThreads(t1, t2, t3);
        printer.start();
    }
}


class SequencePrint3 {
    private int loopNumber;

    private Thread[] threads;

    public SequencePrint3(int loopNumber) {
        this.loopNumber = loopNumber;
    }

    public void setThreads(Thread... threads) {
        this.threads = threads;
    }

    public void print(String str) {
        for (int i = 0; i < loopNumber; ++i) {
            LockSupport.park();
            System.out.print(str);
            LockSupport.unpark(getNext());
        }
    }

    private Thread getNext() {
        Thread currentThread = Thread.currentThread();
        int index = 0;
        for (int i = 0; i < threads.length; i++) {
            if (currentThread == threads[i]) {
                index = i + 1;
            }
        }
        return index <= threads.length - 1 ? threads[index] : threads[0];
    }

    public void start() {
        for (Thread t : threads) {
            t.start();
        }
        LockSupport.unpark(threads[0]);
    }
}
