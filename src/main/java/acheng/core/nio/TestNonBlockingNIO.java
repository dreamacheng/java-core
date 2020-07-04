package acheng.core.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.Scanner;

// 非阻塞式 IO
public class TestNonBlockingNIO {

    @Test
    public void client() throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
        // 设置为非阻塞式 io
        socketChannel.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String next = scanner.next();
            buffer.put((LocalTime.now() + "=> str: " + next).getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
        socketChannel.close();
    }

    @Test
    public void server() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置为 非阻塞式 io
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(9898));

        Selector selector = Selector.open();
        // 绑定selector 并指定监听事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 轮询式的获取选择器上已经 “准备就绪” 的事件
        while(selector.select()>0){

            //获取当前选择器中所有注册的“选择键（已就绪的监听事件）”
            Iterator<SelectionKey> it=selector.selectedKeys().iterator();

            while(it.hasNext()){
                // 获取准备“就绪”的事件
                SelectionKey sk=it.next();

                // 判断具体是什么时间准备就绪
                if(sk.isAcceptable()){
                    // 若“接收就绪”，获取客户端连接
                    SocketChannel socketChannel =serverSocketChannel.accept();

                    // 切换非阻塞模式
                    socketChannel.configureBlocking(false);

                    // 将该通道注册到选择器上
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }else if(sk.isReadable()){
                    // 获取当前选择器上“读就绪”状态的通道
                    SocketChannel sChannel=(SocketChannel)sk.channel();
                    // 读取数据
                    ByteBuffer buf=ByteBuffer.allocate(1024);
                    int len=0;
                    while((len=sChannel.read(buf))>0){
                        buf.flip();
                        System.out.println(new String(buf.array(),0,len));
                        buf.clear();
                    }
                }
                // 取消选择键SelectionKey
                it.remove();
            }
        }
    }
}
