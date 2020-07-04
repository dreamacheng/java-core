package acheng.core.cas;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class AtomicIntegerArrayDemo {

    public static void main(String[] args) {
        // 多线程对int数组 进行读写 线程不安全
        demo(
                () -> new int[10],
                arr -> arr.length,
                (arr, index) -> arr[index]++,
                arr -> log.info("array: {}",Arrays.toString(arr))
        );

        // 使用  AtomicIntegerArray 对 int[] 进行共享保护， CAS
        demo(
                () -> new AtomicIntegerArray(10),
                arr -> arr.length(),
                (arr, index) -> arr.getAndIncrement(index),
                arr -> log.info("atomic array: {}", arr.toString())
        );
    }

    /**
     *
     * @param supplier  产生一个数组, 提供者， () -> 结果
     * @param function  获取数组长度, 函数， arg -> 结果   =》扩展  BiFunction (arg1, arg2) -> 结果，接受两个参数
     * @param biConsumer 消费者  (arg1, arg2) -> {}, 接受两个参数
     * @param consumer 消费者 arg -> {}, 接受一个参数
     * @param <T>
     */
    public static <T> void demo(
            Supplier<T> supplier,
            Function<T, Integer> function,
            BiConsumer<T, Integer> biConsumer,
            Consumer<T> consumer
    ) {
        List<Thread> threads = new LinkedList<>();
        T array = supplier.get();
        Integer len = function.apply(array);
        for (int i = 0; i < len; ++i) {
            threads.add(new Thread(() -> {
                for (int j = 0; j < 10000; ++j) {
                    biConsumer.accept(array, j % len);
                }
            }));
        }
        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        consumer.accept(array);
    }

}