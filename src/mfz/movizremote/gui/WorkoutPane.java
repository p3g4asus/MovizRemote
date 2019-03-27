package mfz.movizremote.gui;

import com.moviz.lib.comunication.holder.WahooBlueSCHolder;
import com.moviz.lib.velocity.PVelocityContext;

public class WorkoutPane extends VelocityPane {

    public WorkoutPane() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getTemplateName() {
        // TODO Auto-generated method stub
        return "workout";
    }


    @Override
    protected void addToContext(PVelocityContext context) {
        super.addToContext(context);
        context.put("WSensorType", WahooBlueSCHolder.SensorType.class);
    }


}
