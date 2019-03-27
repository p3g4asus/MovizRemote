package mfz.movizremote.utils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    public enum MessageClass {
        INFO, ERROR, WARNING, SUCCESS
    }

    private String msg;
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDateString(String fmt) {
        return new SimpleDateFormat(fmt).format(date);
    }

    private MessageClass cl;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MessageClass getCl() {
        return cl;
    }

    public void setCl(MessageClass cl) {
        this.cl = cl;
    }

    public Message(MessageClass c, String m, Class<?> tr) {
        cl = c;
        date = new Date();
        if (tr == null)
            msg = m;
        else {
            try {
                Method getString = tr.getMethod("getString", String.class);
                msg = (String) getString.invoke(null, new String(m));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                msg = m;
            }
        }
    }

    public Message(MessageClass c, String m) {
        this(c, m, null);
    }

    public String toString() {
        return cl + ": " + msg;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Message))
            return false;
        else {
            Message m = (Message) o;
            return m.cl == cl && m.msg.equalsIgnoreCase(msg);
        }
    }
}
