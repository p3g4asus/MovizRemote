package mfz.movizremote.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import com.moviz.lib.velocity.PVelocityContext;

import mfz.movizremote.utils.JavaVelocitySheet;

public abstract class VelocityPane extends JavaVelocitySheet {

    private LineBorder lineBorder = null;
    private String bgColor;
    private JEditorPane pane;

    public JEditorPane getPane() {
        return pane;
    }


    @Override
    protected void addToContext(PVelocityContext ctx) {
        super.addToContext(ctx);
        ctx.put("bgColor", bgColor);
        ctx.put("putlinks", true);
    }

    @Override
    protected void putStringToSheet(final String s) {
        if (SwingUtilities.isEventDispatchThread())
            pane.setText(s);
        else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        pane.setText(s);
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }

    public VelocityPane() {
        super();
        pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setDoubleBuffered(true);
        // addHyperlinkListener(this);

        pane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                pane.setDoubleBuffered(true);
            }
        });

    }
    
    private void initBGColor() {
        Component parent = pane.getParent();
        if (parent != null) {
            Color c = parent.getBackground();
            if (lineBorder == null)
                pane.setBorder(lineBorder = new LineBorder(c, 1));
            bgColor = Integer.toHexString(c.getRGB() & 0x00ffffff);
        } else
            bgColor = "FFFFFF";
    }

    @Override
    public void init() throws Exception {
        initBGColor();
        super.init();
    }

}
