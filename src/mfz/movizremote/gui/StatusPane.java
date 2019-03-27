package mfz.movizremote.gui;

import com.moviz.lib.comunication.DeviceStatus;
import com.moviz.lib.comunication.tcp.TCPStatus;
import com.moviz.lib.velocity.PVelocityContext;

import mfz.movizremote.utils.Configuration;

public class StatusPane extends VelocityPane {
    public StatusPane() {
        super();
    }

    protected String getTemplateName() {
        return "status";
    }

    @Override
    protected void addToContext(PVelocityContext context) {
        super.addToContext(context);
        context.put("datef",
                Configuration.newInstance(null).get("prop.dateformat")
                        + " HH:mm:ss");
        context.put("TCPStatus", TCPStatus.class);
        context.put("DeviceStatus", DeviceStatus.class);

    }

}