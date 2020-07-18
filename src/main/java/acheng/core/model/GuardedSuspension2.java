package acheng.core.model;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * desc: 同步模式： 保护性暂停
 * 要点： 产生结果和消费结果的线程一一对应
 * 优化： 消息生产者和消息消费者不直接通信， 通过中间类解耦；
 */
@Slf4j
public class GuardedSuspension2 {

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Consumer("消息消费方" + i).start();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i : MessageBoxes.getIds()) {
            new Producer(i, "message: " + i, "消息生产方" + i).start();
        }
    }

}

@Slf4j
class GuardedObject2 {
    @Getter
    private int id;
    @Setter
    private Object response;

    public GuardedObject2(int id) {
        this.id = id;
    }

    public Object getResponse(long mills) {
        synchronized (this) {
            long begin = System.currentTimeMillis();
            // 等待时间
            long timePassed = 0;
            // while循环， 避免虚假唤醒
            while (response == null) {
                long waitTime = mills - timePassed;
                if (waitTime <= 0) {
                    log.debug("等待时间完毕");
                    break;
                }
                try {
                    log.debug("wait notify, 继续等待时间：{}", waitTime);
                    this.wait(waitTime);
                    log.debug("获取响应：{}", response);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timePassed = System.currentTimeMillis() - begin;
            }
            return response;
        }
    }

    public void complete(Object response) {
        synchronized (this) {
            this.setResponse(response);
            this.notifyAll();
        }
    }

}


class MessageBoxes {
    private static Map<Integer, GuardedObject2> boxes = new Hashtable<>();

    private static int id = 1;

    public static synchronized int generateId() {
        return id++;
    }

    public static GuardedObject2 createGuardedObject2() {
        GuardedObject2 guardedObject = new GuardedObject2(generateId());
        boxes.put(guardedObject.getId(), guardedObject);
        return guardedObject;
    }

    public static GuardedObject2 getGuardedObject2(int id) {
        return boxes.remove(id);
    }

    public static Set<Integer> getIds() {
        return boxes.keySet();
    }
}

@Slf4j
class Consumer extends Thread {

    public Consumer(String name) {
        super(name);
    }

    @Override
    public void run() {
        GuardedObject2 guardedObject = MessageBoxes.createGuardedObject2();
        log.debug("等待消息产生， 收取id: [{}]", guardedObject.getId());
        Object message = guardedObject.getResponse(5000);
        log.debug("收到消息， 收取id: [{}], 消息: [{}]", guardedObject.getId(), message);
    }
}

@Slf4j
class Producer extends Thread {
    private int id;
    private String message;

    public Producer(int id, String message, String name) {
        super(name);
        this.id = id;
        this.message = message;
    }

    @Override
    public void run() {
        GuardedObject2 guardedObject2 = MessageBoxes.getGuardedObject2(id);
        log.debug("生成设置消息， 收取id: [{}], 消息: [{}]",guardedObject2.getId(), message);
        guardedObject2.complete(message);
    }
}
