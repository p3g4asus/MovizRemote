package mfz.movizremote.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;

import mfz.movizremote.utils.JavaVelocitySheet;
import mfz.movizremote.utils.Message;

public class MessagePane extends JEditorPane {

    /**
	 * 
	 */

    private RuntimeInstance ri = new RuntimeInstance();
    private Template template;
    private LineBorder lineBorder = null;

    public MessagePane() {
        super();
        setContentType("text/html");
        setEditable(false);
        setDoubleBuffered(true);
        try {
            ri.setProperty("introspector.uberspect.class",
                    "com.moviz.lib.velocity.PublicFieldUberspect");
            ri.setProperty("resource.loaders", "class");
            ri.setProperty("resource.loader.class.class",
                    "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            ri.init();
            template = JavaVelocitySheet.loadTemplate(ri, "message",
                    JavaVelocitySheet.readResourceIntoString("message.vm"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // addHyperlinkListener(this);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                setDoubleBuffered(true);
            }
        });
    }

    private void setContext(VelocityContext context) {
        Writer writer = new StringWriter();
        Component parent = getParent();
        if (parent != null) {
            Color c = parent.getBackground();
            if (lineBorder == null)
                setBorder(lineBorder = new LineBorder(c, 1));
            context.put("bgColor", Integer.toHexString(c.getRGB() & 0x00ffffff));
        }
        template.merge(context, writer);
        final String s = writer.toString();
        if (SwingUtilities.isEventDispatchThread())
            setText(s);
        else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        setText(s);
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }

    protected void addToContext(VelocityContext context) {

    }

    public void resetValues() {
        setContext(buildContext(null));
    }

    public void updateValues(Message m) {
        setContext(buildContext(m));
    }

    private ArrayList<Message> messages = new ArrayList<Message>();

    /**
	 * 
	 */
    private static final long serialVersionUID = 333073179841248379L;

    protected String getTemplateName() {
        return "message";
    }

    protected VelocityContext buildContext(Message m) {
        VelocityContext context = new VelocityContext();
        synchronized (messages) {
            if (m == null)
                messages.clear();
            else {
                if (messages.size() >= 7)
                    messages.remove(0);
                messages.add(m);
            }
        }
        context.put("msgs", messages);
        HashMap<String, Message.MessageClass> map = new HashMap<String, Message.MessageClass>();

        map.put("WARNING", Message.MessageClass.WARNING);
        map.put("ERROR", Message.MessageClass.ERROR);
        map.put("INFO", Message.MessageClass.INFO);
        map.put("SUCCESS", Message.MessageClass.SUCCESS);
        context.put("mc", map);
        return context;
    }

}
