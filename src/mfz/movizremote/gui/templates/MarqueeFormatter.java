package mfz.movizremote.gui.templates;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;

import mfz.movizremote.utils.Configuration;
import mfz.movizremote.utils.JavaVelocitySheet;

public class MarqueeFormatter extends JavaVelocitySheet {
    private final static String MESSAGE_PLACEHOLDER = "mm";
    protected ByteBuffer outBB = ByteBuffer.allocate(2048);
    protected String myId;
    protected String answerTemplate = "";
    protected VelocityContext oldCtx = new VelocityContext();

    public MarqueeFormatter() {
        super();
    }

    public void load(String id, Configuration c) throws Exception {
        myId = id;
        answerTemplate = c.get(myId + ".answer");
        init();
        manageAnswerTemplate(c);
    }

    private void manageAnswerTemplate(Configuration c) {
        Pattern pattern = Pattern.compile("\\$([^\\$]+)\\$");
        Matcher matcher = pattern.matcher(answerTemplate);
        String rv = "", m, s;
        int st, en, olden = 0;
        while (matcher.find()) {
            st = matcher.start();
            en = matcher.end();
            if (st > olden)
                rv += answerTemplate.substring(olden, st);
            olden = en;
            m = matcher.group(1);
            if (!m.equals(MESSAGE_PLACEHOLDER)) {
                s = c.get(m);
                rv += (s == null ? "" : s);
            } else
                rv += "$" + MESSAGE_PLACEHOLDER + "$";
        }
        if (olden < answerTemplate.length())
            rv += answerTemplate.substring(olden);
        answerTemplate = rv;
    }

    public ByteBuffer getBB() {
        if (outBB.limit() == 0)
            return null;
        else
            return outBB;
    }

    @Override
    protected void putStringToSheet(String string) {
        outBB.clear();
        if (string.indexOf("$D.") < 0) {
            string = string.trim().replaceAll("[\\s]*\n[\\s]*", " ");
            String out = answerTemplate.replace("$mm$",  string)
                    + "\r\n";
            outBB.put(out.getBytes());
            int p = outBB.position();
            outBB.position(0);
            outBB.limit(p);
        }
    }

    @Override
    protected String getTemplateName() {
        return myId;
    }

}
