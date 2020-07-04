package acheng.core.nio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.ByteBuffer;


@Slf4j
public class TestBuffer {

    /**
     * * capacity：容量，表示缓冲区中最大存储数据的容量。一旦声明不能改变。
     *  * limit：界限，表示缓冲区中可以操作数据的大小。(limit后数据不能进行读写)
     *  * position：位置，表示缓冲区中正在操作数据的位置。
     *  * mark:标记，表示记录当前position位置。可以通过reset()恢复到mark的位置。
     */
    @Test
    public void testApi() {
        ByteBuffer bf = ByteBuffer.allocate(1024);
        log.debug("================== new =========================");
        log.debug("position: [{}]", bf.position());
        log.debug("limit: [{}]", bf.limit());
        log.debug("capacity: [{}]", bf.capacity());

        log.debug("================== put =========================");
        String exam = "example";
        bf.put(exam.getBytes());
        log.debug("position: [{}]", bf.position());
        log.debug("limit: [{}]", bf.limit());
        log.debug("capacity: [{}]", bf.capacity());

        log.debug("================== flip =========================");
        bf.flip();
        log.debug("position: [{}]", bf.position());
        log.debug("limit: [{}]", bf.limit());
        log.debug("capacity: [{}]", bf.capacity());

        log.debug("================== get =========================");
        byte[] dst = new byte[bf.limit()];
        bf.get(dst);
        log.debug("content: [{}]", new String(dst));
        log.debug("position: [{}]", bf.position());
        log.debug("limit: [{}]", bf.limit());
        log.debug("capacity: [{}]", bf.capacity());

    }
}
