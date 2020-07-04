package acheng.core.model;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * desc: 同步模式： 保护性暂停
 * JDK 中，join 的实现、Future 的实现，采用的就是此模式
 *
 */
@Slf4j
public class GuardedSuspension {

    public static void main(String[] args) throws InterruptedException {
        GuardedObject guardedObject = new GuardedObject();
        new Thread(() -> {
            log.debug("获取response");
            List<String>  response = (List<String>) guardedObject.getResponse(3000);
            log.debug("response: [{}]", response);
        }).start();
        Thread.sleep(2000);
        new Thread(() -> {
            log.debug("开始链接资源");
            List<String> strings = connectCDUT();
            log.debug("虚晃一手");
            guardedObject.complete(null);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("设置response");
            guardedObject.complete(strings);
        }).start();
    }


    public static List<String> connectCDUT() {
        List<String> strings = new ArrayList<>();
        try {
            Document document = Jsoup.connect("http://www.cdut.edu.cn/").get();
            Elements elements = document.getElementsByClass("clyw_list_y");
            for (Element e : elements) {
                strings.add(e.className());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }
}

// 线程通信Object
@Slf4j
class GuardedObject {

    private Object lock = new Object();
    @Setter
    private Object response;

    public Object getResponse(long mills) {
        synchronized (lock) {
            long begin = System.currentTimeMillis();
            // 等待时间
            long timePassed = 0;
            // while循环， 避免虚假唤醒
            while (response == null) {
                long waitTime = mills - timePassed;
                if (waitTime <= 0) {
                    log.debug("攒够失望就离开");
                    break;
                }
                try {
                    log.debug("wait notify, 继续等待时间：{}", waitTime);
                    lock.wait(waitTime);
                    log.debug("终于等到你，康康结果：{}", response);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timePassed = System.currentTimeMillis() - begin;
            }
            return response;
        }
    }

    public void complete(Object response) {
        synchronized (lock) {
            this.setResponse(response);
            lock.notifyAll();
        }
    }

}
