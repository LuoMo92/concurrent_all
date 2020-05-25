package cn.itcast.test2;

import cn.itcast.n2.util.Sleeper;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

/**
 * @author Liumei
 * @date 2020/5/25  0025
 */
@Slf4j(topic = "c.Test3")
public class Test3 {

    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue(2);
        for (int i = 0; i < 3; i++) {
            int id = i;
            new Thread(()->{
                messageQueue.put(new Message(id,"信息"+id));
            },"生产者"+i).start();
        }
        new Thread(()->{
            while (true){
                Sleeper.sleep(1);
                messageQueue.take();
            }
        },"消费者").start();
    }

}

@Slf4j(topic = "c.MessageQueue")
class MessageQueue {

    private LinkedList<Message> messageList = new LinkedList();
    private int capcity;

    public MessageQueue(int capcity) {
        this.capcity = capcity;
    }

    public Message take() {
        synchronized (messageList) {
            while (messageList.isEmpty()) {
                try {
                    log.debug("队列为空, 消费者线程等待");
                    messageList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Message message = messageList.removeFirst();
            log.debug("已消费消息 {}", message);
            messageList.notifyAll();
            return message;
        }
    }

    public void put(Message message) {
        synchronized (messageList) {
            while (messageList.size() == capcity) {
                try {
                    log.debug("队列已满, 生产者线程等待");
                    messageList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            messageList.addLast(message);
            log.debug("已生产消息 {}", message);
            messageList.notifyAll();
        }
    }
}

@Getter
@ToString
final class Message {

    private int id;
    private Object value;

    public Message(int id, Object value) {
        this.id = id;
        this.value = value;
    }
}