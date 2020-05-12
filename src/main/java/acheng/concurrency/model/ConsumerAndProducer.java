package acheng.concurrency.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * desc: 异步模式： 生产者/消费者模式
 * 要点： 1.消息队列是有容量限制的，满时不会再加入数据，空时不会再消耗数据
 *       2.生产者仅负责产生结果数据，不关心数据该如何处理，而消费者专心处理结果数据
 */
@Slf4j
public class ConsumerAndProducer {

    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue(3);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                List<String> strings = GuardedSuspension.connectCDUT();
                messageQueue.put(strings);
            }, "生产者:" + i).start();
        }
        new Thread(() -> {
            while (true) {
                List<String> strings = (List<String>) messageQueue.get();
            }
        }, "消费者").start();
    }
}

@AllArgsConstructor
class Message {
    @Getter
    private int id;
    @Getter
    private Object message;
}

@Slf4j
class MessageQueue {
    private int capacity;
    private LinkedList<Object> queue;

    public MessageQueue(int capacity) {
        this.capacity = capacity;
        this.queue = new LinkedList<>();
    }

    public void put(Object message) {
        synchronized (queue) {
            while (queue.size() == capacity) {
                log.debug("队列已满，保持等待");
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("生产消息：[{}]", message);
            queue.addLast(message);
            queue.notifyAll();
        }
    }

    public Object get() {
        synchronized (queue) {
            while (queue.isEmpty()) {
                log.debug("队列已空，保持等待");
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Object message = queue.removeFirst();
            log.debug("消费消息：[{}]",message);
            queue.notifyAll();
            return message;
        }
    }

}
