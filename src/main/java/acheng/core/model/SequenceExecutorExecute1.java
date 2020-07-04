package acheng.core.model;

import lombok.AllArgsConstructor;

/**
 *  desc: 顺序执行
 *  使用三个线程交替打印 a,b,c各5次
 *  方法一： 使用 wait, notifyAll 实现
 */
public class SequenceExecutorExecute1 {

    public static void main(String[] args) {
        SequencePrint printer = new SequencePrint(1, 5);
        new Thread(() -> {
            printer.print("a", 1, 2);
        }).start();
        new Thread(() -> {
            printer.print("b", 2, 3);
        }).start();
        new Thread(() -> {
            printer.print("c", 3, 1);
        }).start();
    }
}


@AllArgsConstructor
class SequencePrint {
    /**
     * 线程标志位
     * a -> 1 ,b -> 2, c -> 3
     */
    private int flag;
    private int loopNumber;

    public void print(String str, int currentFlag, int nextFlag) {
        for (int i = 0; i < loopNumber; ++i) {
            synchronized (this) {
                while (flag != currentFlag) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print(str);
                flag = nextFlag;
                this.notifyAll();
            }
        }
    }
}
