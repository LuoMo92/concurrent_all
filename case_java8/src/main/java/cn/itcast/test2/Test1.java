package cn.itcast.test2;

import cn.itcast.n2.util.Sleeper;
import javafx.geometry.Pos;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * @author Liumei
 * @date 2020/5/22  0022
 */
@Slf4j(topic = "c.Test1")
public class Test1 {

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new People().start();
        }
        Sleeper.sleep(1);
        for (int id:MailBoxes.getIds()){
            new Postman(id,"mail"+id).start();
        }
    }


}

@Slf4j(topic = "c.people")
class People extends Thread {

    @Override
    public void run() {
        GuardedObject guardedObject = MailBoxes.generateGuardedObject();
        int id = guardedObject.getId();
        log.debug("{}开始收信", id);
        Object o = guardedObject.get(5000);
        log.debug("{}收到信：{}", id, o);

    }
}

@Slf4j(topic = "c.postman")
class Postman extends Thread {

    private int id;
    private String mail;

    public Postman(int id, String mail) {
        this.id = id;
        this.mail = mail;
    }

    @Override
    public void run() {
        GuardedObject guardedObject = MailBoxes.get(id);
        guardedObject.complete(mail);
        log.debug("给{}送信：{}", id, mail);
    }
}

class MailBoxes {

    private static Map<Integer, GuardedObject> mailBoxes = new Hashtable<>();

    private static int id = 1;

    private static synchronized int generateId() {
        return id++;
    }

    public static GuardedObject generateGuardedObject() {
        int id = generateId();
        GuardedObject guardedObject = new GuardedObject(id);
        mailBoxes.put(id, guardedObject);
        return guardedObject;
    }

    public static GuardedObject get(int id) {
        return mailBoxes.remove(id);
    }

    public static Set<Integer> getIds() {
        return mailBoxes.keySet();
    }
}

@Data
class GuardedObject {
    /**
     * 标识
     */
    private int id;

    private Object response;

    public GuardedObject(int id) {
        this.id = id;
    }

    /**
     * 可设置等待时间
     *
     * @param timeout
     * @return
     */
    public Object get(long timeout) {
        synchronized (this) {
            //开始时间
            long begin = System.currentTimeMillis();
            //经历时间
            long passedTime = 0;
            while (response == null) {
                //这一轮循环应该等待的时间
                long waitTime = timeout - passedTime;
                if (waitTime <= 0) {
                    break;
                }
                try {
                    this.wait(waitTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                passedTime = System.currentTimeMillis() - begin;
            }
        }
        return response;
    }

    public void complete(Object response) {
        synchronized (this) {
            this.response = response;
            this.notifyAll();
        }
    }
}
