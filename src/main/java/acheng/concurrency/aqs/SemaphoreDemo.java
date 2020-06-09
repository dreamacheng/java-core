package acheng.concurrency.aqs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

@Slf4j
public class SemaphoreDemo {

    public static void main(String[] args) {
        SemaphoreDemo connectionPool = new SemaphoreDemo(3);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                ConnectionMock connection = connectionPool.borrow();
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectionPool.release(connection);
            }, "t" + i).start();
        }
    }


    // 连接状态
    private AtomicIntegerArray status;

    // 连接实例
    private ConnectionMock[] connections;

    // 连接池大小
    private int poolSize;

    private Semaphore semaphore;

    public SemaphoreDemo(int poolSize) {
        this.poolSize = poolSize;
        this.semaphore = new Semaphore(poolSize);
        this.status = new AtomicIntegerArray(poolSize);
        this.connections = new ConnectionMock[poolSize];
        for (int i = 0; i < poolSize; i++) {
            connections[i] = new ConnectionMock("连接实例：" + i);
        }
    }


    // 获取空闲连接实例
    public ConnectionMock borrow() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < poolSize; i++) {
            if (status.get(i) == 0) {
                if (status.compareAndSet(i, 0 , 1)) {
                    log.debug("get connection: [{}]", this.connections[i].getName());
                    return connections[i];
                }
            }
        }
        return null;
    }

    // 释放连接
    public void release(ConnectionMock connectionMock) {
        for (int i = 0; i < poolSize; i++) {
            if (connections[i].equals(connectionMock)) {
                status.set(i, 0);
                semaphore.release();
                log.debug("release connection: [{}]", this.connections[i].getName());
                break;
            }
        }
    }
}

@AllArgsConstructor
@Getter
@ToString
class ConnectionMock {
    private String name;
}
