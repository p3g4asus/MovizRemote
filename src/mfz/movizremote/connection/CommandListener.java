package mfz.movizremote.connection;

import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.StatusHolder;

public interface CommandListener {
    void onWorkout(DeviceUpdate workoutHolder);

    void onStatus(StatusHolder d);

    void onListUser(String list);

    void onListProgram(String list);
}
