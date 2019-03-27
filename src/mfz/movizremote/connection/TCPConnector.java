package mfz.movizremote.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import mfz.movizremote.utils.Configuration;

public class TCPConnector {
    private ConnectionListener connectionListener = null;
    protected String host = null;
    protected Integer port = -1;

    private Timer connectTMR = null;
    private ConnectTask connectTT = null;

    private ReadThread readTH = null;
    private WriteThread writeTH = null;

    private enum Status {
        DISCONNECTED, CONNECTED, STOPPED
    }

    private Status innerStatus = Status.STOPPED;
    private SocketChannel sock = null;
    protected long retryms;
    protected long readtimeoutms;
    protected long sendperiodms;

    private class ConnectTask extends TimerTask {

        String mhost;
        int mport;

        public ConnectTask(String h, int p) {
            mhost = h;
            mport = p;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                if (connectionListener != null)
                    connectionListener.onTryConnect(mhost, mport);
                SocketChannel s = SocketChannel.open();
                s.connect(new InetSocketAddress(mhost, mport));
                connectionAction(s);
                return;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if (connectionListener != null)
                    connectionListener.onError(e);
                connect("", null, false);
            }
            sock = null;

        }

    }

    private class CheckReadTimeoutTask extends TimerTask {
        private long lastread = 0;
        private long timeout = 0;

        public CheckReadTimeoutTask(long readt) {
            timeout = readt;
        }

        public void notifyRead() {
            lastread = System.currentTimeMillis();
        }

        @Override
        public void run() {
            if (innerStatus == Status.CONNECTED
                    && System.currentTimeMillis() - lastread > timeout)
                stopByError(new IOException("ReadTimeout"));
        }

    }

    private class WriteThread extends Thread {
        private SocketChannel s;
        private ArrayList<ByteBuffer> buffer = new ArrayList<ByteBuffer>();
        private long timeout;

        public WriteThread(SocketChannel sock, long wtimeout) {
            s = sock;
            timeout = wtimeout;
            setName("WT " + TCPConnector.this.getClass().getSimpleName());
        }

        public void schedule(ByteBuffer b) {
            if (b.limit() > 0) {
                ByteBuffer clone = ByteBuffer.allocate(b.limit());
                clone.put(b);
                clone.position(0);
                synchronized (buffer) {
                    if (innerStatus == Status.CONNECTED) {
                        buffer.add(clone);
                        buffer.notifyAll();
                    }
                }
            }
        }

        public void run() {
            try {
                long lastsend = 0, dif;
                int ln;
                while (innerStatus == Status.CONNECTED) {
                    if ((dif = System.currentTimeMillis() - lastsend) >= timeout) {
                        synchronized (buffer) {
                            if ((ln = buffer.size()) > 0) {
                                ByteBuffer bb = buffer.remove(0);
                                // System.out.println(new String(bb.array(),
                                // 0,bb.limit(),StandardCharsets.UTF_8));
                                s.write(bb);
                                lastsend = System.currentTimeMillis();
                                ln--;
                            }
                            if (ln > 0)
                                buffer.wait(timeout);
                            else
                                buffer.wait();
                        }
                    } else {
                        synchronized (buffer) {
                            buffer.wait(timeout - dif);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                stopByError(e);
            }
        }

        public void innerStop() {
            synchronized (buffer) {
                buffer.clear();
                buffer.notifyAll();
            }
        }
    }

    private class ReadThread extends Thread {
        private Timer checkReadTMR = null;
        private CheckReadTimeoutTask checkReadTT = null;
        private SocketChannel s;
        private ConnectionListener listen;
        private long timeout;
        private ByteBuffer bb = ByteBuffer.allocate(1024);

        public ReadThread(SocketChannel h, ConnectionListener cl,
                long readtimeoutms) {
            s = h;
            listen = cl;
            timeout = readtimeoutms;
            setName("RT " + TCPConnector.this.getClass().getSimpleName());
        }

        public void run() {
            checkReadTMR = new Timer();
            checkReadTMR.schedule(checkReadTT = new CheckReadTimeoutTask(
                    timeout), timeout + 2000, timeout);
            try {
                int sz;
                bb.clear();
                while (innerStatus == Status.CONNECTED) {
                    if (!bb.hasRemaining())
                        bb.clear();
                    sz = s.read(bb);
                    if (sz < 0)
                        throw new IOException("EOF Reached");
                    else if (sz >= 0 && innerStatus == Status.CONNECTED) {
                        if (listen != null) {
                            bb.limit(bb.position());
                            bb.position(0);
                            listen.onData(bb, sz);
                        }
                        if (checkReadTT != null)
                            checkReadTT.notifyRead();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                TCPConnector.this.stopByError(e);
            }
        }

        public void innerStop() {
            if (checkReadTT != null) {
                checkReadTT.cancel();
                checkReadTT = null;
            }
            if (checkReadTMR != null) {
                checkReadTMR.cancel();
                checkReadTMR = null;
            }
        }
    }

    public void write(ByteBuffer b) {
        if (b != null && writeTH != null && innerStatus == Status.CONNECTED)
            writeTH.schedule(b);
    }

    private void connectionAction(SocketChannel s) {
        stopConnectTimer();
        sock = s;
        innerStatus = Status.CONNECTED;
        if (connectionListener != null)
            connectionListener.onConnect();
        readTH = new ReadThread(sock, connectionListener, readtimeoutms);
        readTH.start();
        writeTH = new WriteThread(sock, sendperiodms);
        writeTH.start();
    }

    public boolean connect(String id, Configuration c) {
        return connect(id, c, true);
    }

    private boolean connect(String id, Configuration c, boolean shortdelay) {
        stopConnectTimer();
        if (c != null)
            loadParams(id, c);
        if (validConf()) {
            connectTMR = new Timer();
            connectTMR.schedule(connectTT = new ConnectTask(host, port),
                    shortdelay ? 2000 : retryms);
            return true;
        } else
            return false;
    }

    private void stopConnectTimer() {
        if (connectTT != null) {
            connectTT.cancel();
            connectTT = null;
        }
        if (connectTMR != null) {
            connectTMR.cancel();
            connectTMR = null;
        }
    }

    private void stopByError(Exception e) {
        if (innerStatus == Status.CONNECTED) {
            innerStop(Status.DISCONNECTED);
            if (connectionListener != null)
                connectionListener.onError(e);
            connect("", null);
        }

    }

    public void stopByRequest() {
        if (innerStatus != Status.STOPPED)
            innerStop(Status.STOPPED);
    }

    private void innerStop(Status s) {
        innerStatus = s;
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            sock = null;
        }
        if (readTH != null) {
            readTH.innerStop();
            readTH = null;
        }
        if (writeTH != null) {
            writeTH.innerStop();
            writeTH = null;
        }
        stopConnectTimer();

    }

    public TCPConnector(ConnectionListener l) {
        connectionListener = l;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    protected void loadParams(String id, Configuration c) {
        host = c.get(id + ".host");
        port = c.getInt(id + ".port", 1, 65535);
        retryms = c.getInt(id + ".retry", 5, null) * 1000;
        readtimeoutms = c.getInt(id + ".readtimeout", 5, null) * 1000;
        sendperiodms = c.getInt(id + ".sendperiodms", 10, 5000);
    }

    private boolean validConf() {
        return host != null && !host.isEmpty();
    }

}
