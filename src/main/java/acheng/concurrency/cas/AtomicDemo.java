package acheng.concurrency.cas;

import lombok.Getter;
import sun.misc.Unsafe;

public class AtomicDemo {

    public static void main(String[] args) {
        Account.start(new Account() {

            AtomicData selfImpl = new AtomicData(10000);

            @Override
            public int getBalance() {
                return selfImpl.getData();
            }

            @Override
            public void withdraw(int amount) {
                selfImpl.decrease(amount);
            }
        });
    }

}

class AtomicData {

    @Getter
    private volatile int data;

    static final Unsafe unsafe;

    // 字段偏移量
    static final long DATA_OFFSET;

    static {
        unsafe = UnsafeAccessor.getUnsafe();
        try {
            DATA_OFFSET = unsafe.objectFieldOffset(AtomicData.class.getDeclaredField("data"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public AtomicData(int data) {
        this.data = data;
    }

    public void decrease(int amount) {
        int oldValue;
        while (true) {
            oldValue = data;
            int next = oldValue - amount;
            // CAS
            if (unsafe.compareAndSwapInt(this, DATA_OFFSET, oldValue, next)) {
                return;
            }
        }
    }

}
