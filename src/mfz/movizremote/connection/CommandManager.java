package mfz.movizremote.connection;

import java.nio.ByteBuffer;

import com.moviz.lib.comunication.message.CommandMessage;
import com.moviz.lib.comunication.message.ConnectMessage;
import com.moviz.lib.comunication.message.DisconnectMessage;
import com.moviz.lib.comunication.message.ExitMessage;
import com.moviz.lib.comunication.message.KeepAliveMessage;
import com.moviz.lib.comunication.message.PauseMessage;
import com.moviz.lib.comunication.message.ProgramChangeMessage;
import com.moviz.lib.comunication.message.ProgramListMessage;
import com.moviz.lib.comunication.message.ProgramListRequestMessage;
import com.moviz.lib.comunication.message.StartMessage;
import com.moviz.lib.comunication.message.StatusMessage;
import com.moviz.lib.comunication.message.UpDownMessage;
import com.moviz.lib.comunication.message.UpdateCommandMessage;
import com.moviz.lib.comunication.message.UserChangeMessage;
import com.moviz.lib.comunication.message.UserListMessage;
import com.moviz.lib.comunication.message.UserListRequestMessage;
import com.moviz.lib.comunication.tcp.TCPProtocol;

public class CommandManager {
    private ByteBuffer outBB = ByteBuffer.allocate(1024);
    private CommandListener listener;
    private TCPProtocol protocol = new TCPProtocol();

    // long lastWorkout = 0;
    // long lastStatus = 0;
    public CommandManager(CommandListener l) {
        listener = l;
    }

    private static String byte2Hex(byte[] cmd, int len) {
        String readMessage = "";
        for (int i = 0; i < len; i++) {
            readMessage = readMessage
                    + String.format("%02X",
                            new Object[] { Byte.valueOf(cmd[i]) });
        }
        return readMessage;
    }

    public void parse(ByteBuffer bb, int length) {
        CommandMessage prt;
        do {
            prt = protocol.decodeMsg(bb);
            if (prt != null) {
                if (prt instanceof UpdateCommandMessage) {
                    listener.onWorkout(((UpdateCommandMessage) prt).getUpdate());
                } else if (prt instanceof StatusMessage) {
                    listener.onStatus(((StatusMessage) prt).getStatus());
                } else if (prt instanceof UserListMessage) {
                    listener.onListUser(((UserListMessage) prt).getList());
                } else if (prt instanceof ProgramListMessage) {
                    listener.onListProgram(((ProgramListMessage) prt).getList());
                }
            }
        } while (prt != null);
    }

    public ByteBuffer program(String s) {
        outBB.clear();
        protocol.encodeMsg(new ProgramChangeMessage(s), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer user(String s) {
        outBB.clear();
        protocol.encodeMsg(new UserChangeMessage(s), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer queryListUser() {
        outBB.clear();
        protocol.encodeMsg(new UserListRequestMessage(), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer queryListProgram() {
        protocol.encodeMsg(new ProgramListRequestMessage(), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer up(int v) {
        outBB.clear();
        protocol.encodeMsg(new UpDownMessage(Math.abs(v)), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer down(int v) {
        outBB.clear();
        protocol.encodeMsg(new UpDownMessage(-Math.abs(v)), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer start() {
        outBB.clear();
        protocol.encodeMsg(new StartMessage(), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer stop() {
        outBB.clear();
        protocol.encodeMsg(new DisconnectMessage(), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer pause() {
        outBB.clear();
        protocol.encodeMsg(new PauseMessage(), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer reset() {
        outBB.clear();
        protocol.encodeMsg(new ExitMessage(), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer keepAlive() {
        outBB.clear();
        protocol.encodeMsg(new KeepAliveMessage(), outBB);
        outBB.position(0);
        return outBB;
    }

    public ByteBuffer connect() {
        outBB.clear();
        protocol.encodeMsg(new ConnectMessage(), outBB);
        outBB.position(0);
        return outBB;
    }

}
