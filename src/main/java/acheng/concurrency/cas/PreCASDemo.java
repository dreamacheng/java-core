package acheng.concurrency.cas;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PreCASDemo {

    public static void main(String[] args) {
        AccountUnsafe account = new AccountUnsafe(10000);
        Account.start(account);

        AccountSafe accountSafe = new AccountSafe(10000);
        Account.start(accountSafe);
    }
}


class AccountUnsafe implements Account {

    // 共享变量，多线程修改会发生问题
     private Integer balance;

    public AccountUnsafe (Integer amount) {
        this.balance = amount;
    }

    @Override
    public int getBalance() {
        return this.balance;
    }

    @Override
    public void withdraw(int amount) {
        this.balance -= amount;  // 多个线程访问临界区代码发生问题
    }
}

class AccountSafe implements Account {

    private AtomicInteger balance;

    public AccountSafe (Integer amount) {
        this.balance = new AtomicInteger(amount);
    }

    @Override
    public int getBalance() {
        return this.balance.get();
    }

    @Override
    public void withdraw(int amount) {
        // CAS
        while (true) {
            int prev = this.balance.get();
            int next = prev - amount;
            if (this.balance.compareAndSet(prev, next)) {
                return;
            }
        }
    }
}


interface Account {

    int getBalance();

    void withdraw(int amount);

    static void start(Account account) {
        List<Thread> threads = new LinkedList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            threads.add(new Thread(()-> {
                account.withdraw(10);
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
        System.out.println(String.format("======> balance: [%d], spend: [%d]", account.getBalance(), end));
    }
}