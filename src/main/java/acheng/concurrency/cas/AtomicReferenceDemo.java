package acheng.concurrency.cas;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomicReferenceDemo {

    public static void main(String[] args) {
        // 使用new BigDecimal(String)构造函数，创建一个参数以字符串表示数值的对象, 直接传入数值可能会有精度问题
        DecimalAccountUnsafe account = new DecimalAccountUnsafe(new BigDecimal(10000 + ""));
        DecimalAccount.start(account);

        DecimalAccountSafe account2 = new DecimalAccountSafe(new BigDecimal(10000 + ""));
        DecimalAccount.start(account2);

    }
}

/**
 * BigDecimal作为对象引用，多个线程进行读写时会发生问题
 */
class DecimalAccountUnsafe implements DecimalAccount {

    private BigDecimal balance;

    public DecimalAccountUnsafe(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public BigDecimal getBalance() {
        return this.balance;
    }

    @Override
    public void withdraw(BigDecimal amount) {
        this.balance = this.getBalance().subtract(amount);
    }
}

/**
 * 使用 AtomicReference 对BigDecimal进行共享保护， CAS
 * AtomicReference:
 *      会出现ABA问题  -》 AtomicStampedReference: 对版本号进行维护
 *          当不关心版本号，只关心是否被其他线程修改 =》 AtomicMarkableReference(obj, flag)  => obj: 要保护的共享对象, flag-boolean: 初始标志位
 */
class DecimalAccountSafe implements DecimalAccount {

    private AtomicReference<BigDecimal> balance;

    public DecimalAccountSafe(BigDecimal balance) {
        this.balance = new AtomicReference<>(balance);
    }

    @Override
    public BigDecimal getBalance() {
        return this.balance.get();
    }

    @Override
    public void withdraw(BigDecimal amount) {
        while (true) {
            BigDecimal prev = getBalance();
            BigDecimal next = prev.subtract(amount);
            if (balance.compareAndSet(prev, next)) {
                return;
            }
        }
    }
}

interface DecimalAccount {

    BigDecimal getBalance();

    void withdraw(BigDecimal amount);

    static void start(DecimalAccount account) {
        List<Thread> threads = new LinkedList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            threads.add(new Thread(()-> {
                account.withdraw(BigDecimal.TEN);
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
        long end = System.currentTimeMillis() - start;
        System.out.println(String.format("======> balance: %s, spend: [%d]", account.getBalance(), end));
    }
}