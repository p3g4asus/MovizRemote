package mfz.movizremote.connection;

import java.nio.ByteBuffer;

public interface ConnectionListener {
    public void onConnect();

    public void onError(Exception e);

    public void onData(ByteBuffer bb, int sz);

    public void onTryConnect(String mhost, int mport);
}
