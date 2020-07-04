package acheng.core.nio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * java.nio.channels.Channel 接口：
 *     |--FileChannel：用于读取、写入、映射和操作文件的通道。
 *     |--SocketChannel：通过 TCP 读写网络中的数据。
 *     |--ServerSocketChannel：可以监听新进来的 TCP 连接，对每一个新进来的连接都会创建一个 SocketChannel。
 *     |--DatagramChannel：通过 UDP 读写网络中的数据通道。
 *
 *
 * java针对支持通道的类提供了getChannel()方法
 *      本地IO：
 *      FileInputStream/FileOutputStream
 *      RandomAccessFile
 *
 *      网络IO：
 *      Socket
 *      ServerSocket
 *      DatagramSocket
 */
@Slf4j
public class TestChannel {


    @Test
    // 非直接缓冲区
    public void testApi1() throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            fis = new FileInputStream("E:\\DeskPhoto\\aa.jpg");
            fos = new FileOutputStream("E:\\DeskPhoto\\a.jpg");

            inChannel = fis.getChannel();
            outChannel = fos.getChannel();

            ByteBuffer bf = ByteBuffer.allocate(1024);

            while (inChannel.read(bf) != -1) {
                bf.flip();
                outChannel.write(bf);
                bf.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    @Test
    // 直接缓冲区
    public void testApi2() throws IOException {

        FileChannel inChannel = null;
        FileChannel outChanel = null;
        try {
            inChannel = FileChannel.open(Paths.get("E:\\DeskPhoto\\aa.jpg"), StandardOpenOption.READ);
            outChanel = FileChannel.open(Paths.get("E:\\DeskPhoto\\bb.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.READ);

            // 内存映射文件
            MappedByteBuffer inMappedBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            MappedByteBuffer outMappedBuffer = outChanel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

            byte[] dst = new byte[inMappedBuffer.limit()];
            inMappedBuffer.get(dst);
            outMappedBuffer.put(dst);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChanel != null) {
                outChanel.close();
            }
        }
    }

    @Test
    // channel之间之间传输 (直接缓冲区)
    public void testApi3() throws IOException {

        FileChannel inChannel = null;
        FileChannel outChanel = null;
        try {
            inChannel = FileChannel.open(Paths.get("E:\\DeskPhoto\\aa.jpg"), StandardOpenOption.READ);
            outChanel = FileChannel.open(Paths.get("E:\\DeskPhoto\\dd.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            // inChannel.transferTo(0, inChannel.size(), outChanel);
            outChanel.transferFrom(inChannel, 0, inChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChanel != null) {
                outChanel.close();
            }
        }
    }
}
