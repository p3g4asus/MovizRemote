package mfz.movizremote.gui;

import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Rectangle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.gpl.JSplitButton.JSplitButton;
import org.gpl.JSplitButton.action.SplitButtonActionListener;

import com.moviz.lib.comunication.ComunicationConstants;
import com.moviz.lib.comunication.holder.DeviceHolder;
import com.moviz.lib.comunication.holder.DeviceUpdate;
import com.moviz.lib.comunication.holder.Holderable;
import com.moviz.lib.comunication.holder.StatusHolder;
import com.moviz.lib.velocity.VelocitySheet.SheetUpdate;

import mfz.movizremote.connection.CommandListener;
import mfz.movizremote.connection.CommandManager;
import mfz.movizremote.connection.ConnectionListener;
import mfz.movizremote.connection.TCPConnector;
import mfz.movizremote.gui.templates.MarqueeFormatter;
import mfz.movizremote.utils.Configuration;
import mfz.movizremote.utils.Message;

public class PRMainWindow {

    private static final String LIST_PANE_NAME = "ListRecordPane";
    private static final String LIST_DIALOG_NAME = "ListRecordDialog";
    private static final String PROGRESS_CONNECT = "connect";
    private static final String PROGRESS_PROGRAM = "program";
    private static final String PROGRESS_USER = "user";
    private JFrame frame;
    private MyProgressMonitor connectProgress = null;
    private CommandManager commandManager = null;
    private TCPConnector mainConnector = null;
    private TCPConnector[] connectors = null;
    private Configuration conf = null;
    private Timer keepAliveTimer = null;
    private Map<Long, DeviceHolder> sessionMap = new HashMap<Long, DeviceHolder>();
    private Map<DeviceHolder, DeviceUpdate> updateMap = new HashMap<DeviceHolder, DeviceUpdate>();
    private Map<DeviceHolder, StatusHolder> statusMap = new HashMap<DeviceHolder, StatusHolder>();
    private int keepaliveT = (int) (ComunicationConstants.KEEPALIVE_PERIOD / 1000);

    private void startKeepAliveTimer() {
        stopKeepAliveTimer();
        keepAliveTimer = new Timer();
        keepAliveTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                mainConnector.write(commandManager.keepAlive());
            }

        }, keepaliveT * 1000, keepaliveT * 1000);
    }

    private void stopKeepAliveTimer() {
        if (keepAliveTimer != null) {
            keepAliveTimer.cancel();
            keepAliveTimer = null;
        }

    }

    private HyperlinkListener statusHLL = new HyperlinkListener() {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String desc = e.getDescription();
                if (desc.equals(PROGRESS_USER))
                    mainConnector.write(commandManager.queryListUser());
                else
                    mainConnector.write(commandManager.queryListProgram());
                showProgressBar(desc, true, null);
            }

        }

    };
    private ConnectionListener connectionListener = new ConnectionListener() {

        @Override
        public void onConnect() {
            // TODO Auto-generated method stub
            showProgressBar(PROGRESS_CONNECT, false, null);
            startKeepAliveTimer();
        }

        @Override
        public void onError(Exception e) {
            // TODO Auto-generated method stub
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sessionMap.clear();
            statusMap.clear();
            updateMap.clear();
            showProgressBar(PROGRESS_CONNECT, true, new Message(
                    Message.MessageClass.ERROR, sw.toString()));
            stopKeepAliveTimer();
        }

        @Override
        public void onData(ByteBuffer b, int sz) {
            // TODO Auto-generated method stub
            commandManager.parse(b, sz);
        }

        @Override
        public void onTryConnect(String mhost, int mport) {
            // TODO Auto-generated method stub
            showProgressBar(
                    PROGRESS_CONNECT,
                    true,
                    new Message(Message.MessageClass.INFO, String.format(
                            PRMessages.getString("connection.trymsg"), mhost,
                            mport)));
        }
    };

    private void updateFormatters(
            Map<DeviceHolder, ? extends Holderable> updates, String varName) {
        int i = 0;
        /*Id2Key convId = new Id2Key() {

            @Override
            public String convert(String id) {
                return id.replace('.', '_');
            }
        };
        HashMap<String, Map<String, Holder>> deb = new HashMap<String,Map<String,Holder>>();*/ 
        for (MarqueeFormatter mf : formatters) {
            if (mf != null) {
                mf.updateValues(new SheetUpdate(updateMap, "upd"), new SheetUpdate(statusMap, "status"));
                connectors[i].write(mf.getBB());
            }
            i++;
        }
    }
    
    private static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.err.println(pair.getKey() + " = [" + pair.getValue().getClass().getSimpleName()+"] " + pair.getValue());
            //it.remove(); // avoids a ConcurrentModificationException
        }
    }

    private CommandListener commandListener = new CommandListener() {

        @Override
        public void onWorkout(DeviceUpdate d) {
            // TODO Auto-generated method stub
            long sid = d.getSessionId();
            if (sessionMap.containsKey(sid)) {
                // System.out.println(d.getClass().getSimpleName()+" "+sid);
                final JScrollBar sb = workoutSP.getVerticalScrollBar();
                final int statusSB = sb != null ? sb.getValue() : 0;
                updateMap.put(sessionMap.get(sid), d);
                workoutEP.updateValues(new SheetUpdate(updateMap,"upd"));
                updateFormatters(updateMap, "upd");
                if (sb != null)
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            public void run() {
                                sb.setValue(statusSB);
                            }
                        });
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
            }
            // chartPNL.updateValues(d);
            showProgressBar(PROGRESS_CONNECT, false, null);
        }

        @Override
        public void onStatus(StatusHolder d) {
            // TODO Auto-generated method stub
            long sid = d.session.getId();
            if (sid >= 0 && !sessionMap.containsKey(sid)) {
                sessionMap.put(sid, d.session.getDevice());
                // System.out.println(" Putting "+sid+" "+d.session.getDevice().getAlias());
            }
            // System.out.println(d.session.getDevice().getAlias()+" "+d.session.getDevice().getAddress()+" "+sid);
            final JScrollBar sb = statusSP.getVerticalScrollBar();
            final int statusSB = sb != null ? sb.getValue() : 0;
            statusMap.put(d.session.getDevice(), d);
            statusEP.updateValues(new SheetUpdate(statusMap, "status"));
            updateFormatters(statusMap, "status");
            if (sb != null)
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            sb.setValue(statusSB);
                        }
                    });
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            showProgressBar(PROGRESS_CONNECT, false, null);
        }

        @Override
        public void onListUser(String d) {
            // TODO Auto-generated method stub
            if (connectProgress != null
                    && connectProgress.getName().equals(PROGRESS_USER)) {
                showProgressBar(PROGRESS_USER, false, null);
                String user = showChoiceDialog(d, PROGRESS_USER);
                if (user != JOptionPane.UNINITIALIZED_VALUE) {
                    mainConnector.write(commandManager.user(user));
                }
            }
        }

        @Override
        public void onListProgram(String d) {
            if (connectProgress != null
                    && connectProgress.getName().equals(PROGRESS_PROGRAM)) {
                showProgressBar(PROGRESS_PROGRAM, false, null);
                String program = showChoiceDialog(d, PROGRESS_PROGRAM);
                if (program != JOptionPane.UNINITIALIZED_VALUE) {
                    mainConnector.write(commandManager.program(program));
                }
            }

        }

    };

    private String showChoiceDialog(String lr, String prefx) {
        JOptionPane pane = new JOptionPane(createInputComponent(lr, prefx),
                JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION);
        pane.setName(LIST_PANE_NAME);
        JDialog dialog = pane.createDialog(PRMessages.getString(prefx
                + ".dialog.title"));
        dialog.setName(LIST_DIALOG_NAME);
        dialog.pack();
        dialog.setVisible(true);
        // UNINITIALIZED_VALUE se si spinge OK
        return (String) pane.getInputValue();
    }

    private ActionListener createChoiceAction() {
        ActionListener chooseMe = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton choice = (JButton) e.getSource();

                // find the pane so we can set the choice.
                Container parent = choice.getParent();
                while (!LIST_PANE_NAME.equals(parent.getName())) {
                    parent = parent.getParent();
                }

                JOptionPane pane = (JOptionPane) parent;
                pane.setInputValue(choice.getText());

                // find the dialog so we can close it.
                while ((parent != null)
                        && !LIST_DIALOG_NAME.equals(parent.getName())) {
                    parent = parent.getParent();
                }

                if (parent != null) {
                    parent.setVisible(false);
                }
            }
        };
        return chooseMe;
    }

    private JComponent createInputComponent(String lr, String prefx) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(PRMessages.getString(prefx + ".dialog.label")),
                BorderLayout.NORTH);

        Box rows = Box.createVerticalBox();
        rows.add(Box.createVerticalStrut(5));
        ActionListener chooseMe = createChoiceAction();
        String[] lra = lr.split(ComunicationConstants.LIST_SEPARATOR);

        for (String s : lra) {
            JButton b = new JButton(s);
            b.addActionListener(chooseMe);
            rows.add(Box.createVerticalStrut(1));
            rows.add(b);
        }
        // rows.add(Box.createVerticalStrut(5));
        // JButton b = new JButton(PRMessages.getString(prefx+".dialog.exit"));
        // b.addActionListener(chooseMe);
        // rows.add(b);

        rows.add(Box.createVerticalStrut(10));

        p.add(rows, BorderLayout.CENTER);
        return p;
    }

    private ActionListener cancelACL = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
            JButton src = (JButton) e.getSource();
            String name = src.getName();
            if (name.equals(PROGRESS_CONNECT))
                System.exit(0);
            else
                showProgressBar(name, false, null);

        }

    };
    private StatusPane statusEP;
    private JScrollPane statusSP;
    private JScrollPane workoutSP;
    private WorkoutPane workoutEP;
    // CHT private WorkoutChartPanel chartPNL;
    // CHT private JDialog chartDLG = null;
    private JDialog statusDLG = null;
    private JPanel buttonPNL;
    private JCheckBoxMenuItem viewStatusMI, alwaysOnTopMI;
    // CHT private JCheckBoxMenuItem viewGraphMI;
    private MarqueeFormatter[] formatters = null;

    /*
     * CHT private void openChartDialog(boolean close) { if (chartDLG==null &&
     * !close) { chartDLG = new JDialog(frame,
     * PRMessages.getString("chartDLG.title"),false);
     * chartDLG.addWindowListener(new WindowAdapter() { public void
     * windowClosing(WindowEvent e) { chartDLG.dispose(); chartDLG = null;
     * viewGraphMI.setState(chartDLG!=null); } });
     * chartDLG.getContentPane().setLayout(new BorderLayout(0,0));
     * chartDLG.getContentPane().add(chartPNL,BorderLayout.CENTER); Rectangle r
     * = conf.loadRectangle("chart"); if (r!=null) chartDLG.setBounds(r); else
     * chartDLG.pack(); chartDLG.setVisible(true); } else if (chartDLG!=null &&
     * close) { conf.saveRectangle(chartDLG.getBounds(), "chart");
     * chartDLG.dispose(); chartDLG = null; }
     * viewGraphMI.setState(chartDLG!=null); }
     */

    private void openStatusDialog(boolean close) {
        if (statusDLG == null && !close) {
            statusDLG = new JDialog(frame,
                    PRMessages.getString("statusDLG.title"), false);
            statusDLG.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    statusDLG.dispose();
                    statusDLG = null;
                    viewStatusMI.setState(statusDLG != null);
                }
            });
            statusDLG.getContentPane().setLayout(new BorderLayout(0, 0));
            statusDLG.getContentPane().add(statusSP, BorderLayout.CENTER);
            statusDLG.getContentPane().add(buttonPNL, BorderLayout.SOUTH);
            Rectangle r = conf.loadRectangle("status");
            if (r != null)
                statusDLG.setBounds(r);
            else
                statusDLG.pack();
            statusDLG.setVisible(true);
        } else if (statusDLG != null && close) {
            conf.saveRectangle(statusDLG.getBounds(), "status");
            statusDLG.dispose();
            statusDLG = null;
        }
        viewStatusMI.setState(statusDLG != null);
    }

    /**
     * Launch the application.
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Configuration c = Configuration.newInstance(args);
                    PRMainWindow window = new PRMainWindow(c);
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showProgressBar(final String type, final boolean show,
            final Message mt) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showProgressBar(type, show, mt);
                }
            });
            return;
        }
        if (type.equals(PROGRESS_CONNECT))
            frame.setAlwaysOnTop(show ? false : alwaysOnTopMI.getState());
        if (type.equals(PROGRESS_CONNECT) && connectProgress != null
                && !connectProgress.getName().equals(PROGRESS_CONNECT)) {
            if (show) {
                connectProgress.setCurrent(null, connectProgress.getTotal(),
                        null);
                connectProgress = null;
            } else
                return;
        } else if ((type.equals(PROGRESS_PROGRAM) || type.equals(PROGRESS_USER))
                && connectProgress != null
                && connectProgress.getName().equals(PROGRESS_CONNECT))
            return;
        if (show && connectProgress == null) {

            connectProgress = ProgressUtil.createModalProgressMonitor(frame,
                    100, true, 100, type, cancelACL);
            connectProgress.start(PRMessages.getString(type
                    + ".progress.status"));
        } else if (!show && connectProgress != null) {
            connectProgress.setCurrent(null, connectProgress.getTotal(), null);
            connectProgress = null;
        }
        if (mt != null && connectProgress != null)
            connectProgress.setCurrent(null, 0, mt);
    }

    /*
     * private void showProgressBar(boolean show) { if (show &&
     * connectProgress==null) { connectProgress =
     * ProgressUtil.createModalProgressMonitor(frame, 100, true,
     * 100,cancelConnectionACL );
     * connectProgress.start(PRMessages.getString("progress_dialog.connect_status"
     * )); } else if (!show && connectProgress!=null) {
     * connectProgress.setCurrent(null, connectProgress.getTotal(),null);
     * connectProgress = null; } }
     */

    /**
     * Create the application.
     * 
     * @param c
     */
    public PRMainWindow(Configuration c) {
        initialize();
        conf = c;
        mainConnector = new TCPConnector(connectionListener);
        commandManager = new CommandManager(commandListener);
        statusEP.getPane().addHyperlinkListener(statusHLL);
        try {
            c.load();
            String ids = c.get("connectors.id");
            if (ids != null && !ids.isEmpty()) {
                String[] ida = ids.trim().split(" ");
                connectors = new TCPConnector[ida.length];
                formatters = new MarqueeFormatter[ida.length];
                int i = 0;

                for (String id : ida) {
                    if (id.length() > 0) {
                        try {
                            connectors[i] = new TCPConnector(null);
                            if (connectors[i].connect(id, c)) {
                                formatters[i] = new MarqueeFormatter();
                                formatters[i].load(id, c);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            formatters[i] = null;
                            try {
                                connectors[i].stopByRequest();
                            } catch (Exception exc) {

                            }
                        }
                        i++;
                    }

                }
            } else
                formatters = new MarqueeFormatter[0];
            keepaliveT = c.getInt("connection.keepalive", 1, 900);
            alwaysOnTopMI.setSelected("true".equals(c.get("alwaystop")));
            Rectangle r = c.loadRectangle("main");
            if (r != null)
                frame.setBounds(r);
            else
                frame.pack();
            workoutEP.init();
            statusEP.init();
            showProgressBar(PROGRESS_CONNECT, true, null);
            if (!mainConnector.connect("connection", conf))
                throw new Exception();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            showProgressBar(PROGRESS_CONNECT, false, null);
            JOptionPane.showMessageDialog(
                    frame,
                    String.format(PRMessages.getString("error.invalid_conf"),
                            c.getPath()),
                    PRMessages.getString("error.invalid_conf_title"),
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        openSavedDialogs();
        /*
         * openChartDialog(false); openStatusDialog(false);
         */

    }

    private void openSavedDialogs() {
        /*
         * CHT if (conf.loadRectangle("chart")!=null) openChartDialog(false);
         */
        if (conf.loadRectangle("status") != null)
            openStatusDialog(false);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        // frame.setBounds(100, 100, 575, 497);
        frame.setTitle(PRMessages.getString("mainFRM.title"));
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                conf.saveRectangle(frame.getBounds(), "main");
                if (statusDLG == null)
                    conf.saveRectangle(null, "status");
                else
                    conf.saveRectangle(statusDLG.getBounds(), "status");
                /*
                 * CHT if (chartDLG==null) conf.saveRectangle(null,"chart");
                 * else conf.saveRectangle(chartDLG.getBounds(),"chart");
                 */
                conf.set("alwaystop", alwaysOnTopMI.getState() + "");
                try {
                    conf.store();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        // CHT chartPNL = new WorkoutChartPanel();
        workoutEP = new WorkoutPane();
        workoutSP = new JScrollPane();
        workoutSP.setViewportView(workoutEP.getPane());

        JButton startBTN = new JButton(""); //$NON-NLS-1$
        startBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConnector.write(commandManager.start());
            }
        });
        startBTN.setIcon(new ImageIcon(PRMainWindow.class
                .getResource("/mfz/movizremote/gui/images/play.med.png")));

        JButton pauseBTN = new JButton("");
        pauseBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConnector.write(commandManager.pause());
            }
        });
        pauseBTN.setIcon(new ImageIcon(PRMainWindow.class
                .getResource("/mfz/movizremote/gui/images/pause.med.png")));

        JButton connectBTN = new JButton("");
        connectBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConnector.write(commandManager.connect());
            }
        });
        connectBTN.setIcon(new ImageIcon(PRMainWindow.class
                .getResource("/mfz/movizremote/gui/images/connect.med.png")));

        JButton stopBTN = new JButton("");
        stopBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConnector.write(commandManager.stop());
            }
        });
        stopBTN.setIcon(new ImageIcon(PRMainWindow.class
                .getResource("/mfz/movizremote/gui/images/stop.med.png")));

        /*
         * JButton resetBTN = new JButton(""); resetBTN.addActionListener(new
         * ActionListener() { public void actionPerformed(ActionEvent e) {
         * connector.write(commandManager.reset()); } }); resetBTN.setIcon(new
         * ImageIcon(PRMainWindow.class.getResource(
         * "/mfz/movizremote/gui/images/reset.med.png")));
         */
        alwaysOnTopMI = new JCheckBoxMenuItem(
                PRMessages.getString("alwaysOnTopMI.label"));
        alwaysOnTopMI.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                frame.setAlwaysOnTop(e.getStateChange() == ItemEvent.SELECTED);
            }

        });

        /*
         * CHT viewGraphMI = new
         * JCheckBoxMenuItem(PRMessages.getString("viewGraphMI.label"));
         * viewGraphMI.addItemListener(new ItemListener() {
         * 
         * @Override public void itemStateChanged(ItemEvent e) { // TODO
         * Auto-generated method stub
         * openChartDialog(e.getStateChange()!=ItemEvent.SELECTED); }
         * 
         * });
         */
        viewStatusMI = new JCheckBoxMenuItem(
                PRMessages.getString("viewStatusMI.label"));
        viewStatusMI.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // TODO Auto-generated method stub
                openStatusDialog(e.getStateChange() != ItemEvent.SELECTED);
            }

        });
        JPopupMenu viewMNU = new JPopupMenu();
        viewMNU.add(viewStatusMI);
        // viewMNU.add(viewGraphMI);
        viewMNU.add(alwaysOnTopMI);

        // first instantiate the control
        JSplitButton viewBTN = new JSplitButton();
        viewBTN.setText(PRMessages.getString("viewBTN.text"));
        // register for listener
        viewBTN.addSplitButtonActionListener(new SplitButtonActionListener() {

            public void buttonClicked(ActionEvent e) {
                openSavedDialogs();
            }

            public void splitButtonClicked(ActionEvent e) {
            }
        });
        viewBTN.setPopupMenu(viewMNU);

        JButton upBTN = new JButton("");
        upBTN.setMnemonic('U');
        upBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConnector.write(commandManager.up(1));
            }
        });
        upBTN.setIcon(new ImageIcon(PRMainWindow.class
                .getResource("/mfz/movizremote/gui/images/up.med.png")));

        JButton downBTN = new JButton("");
        downBTN.setMnemonic('D');
        downBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConnector.write(commandManager.down(1));
            }
        });
        downBTN.setIcon(new ImageIcon(PRMainWindow.class
                .getResource("/mfz/movizremote/gui/images/down.med.png")));

        JButton resetBTN = new JButton("");
        resetBTN.setMnemonic('R');
        resetBTN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainConnector.write(commandManager.reset());
            }
        });
        resetBTN.setIcon(new ImageIcon(PRMainWindow.class
                .getResource("/mfz/movizremote/gui/images/reset.med.png")));

        JPanel button1PNL = new JPanel();
        button1PNL.setLayout(new FlowLayout());
        button1PNL.add(startBTN);
        button1PNL.add(stopBTN);
        button1PNL.add(pauseBTN);

        JPanel button2PNL = new JPanel();
        button2PNL.setLayout(new FlowLayout());

        button2PNL.add(connectBTN);
        // button2PNL.add(viewBTN);
        button2PNL.add(upBTN);
        button2PNL.add(downBTN);
        button2PNL.add(resetBTN);

        buttonPNL = new JPanel();
        buttonPNL.setLayout(new BoxLayout(buttonPNL, BoxLayout.Y_AXIS));
        buttonPNL.add(button1PNL);
        buttonPNL.add(button2PNL);

        statusEP = new StatusPane();
        statusSP = new JScrollPane();
        statusSP.setViewportView(statusEP.getPane());

        JPanel viewPNL = new JPanel();
        viewPNL.setLayout(new FlowLayout());
        viewPNL.add(viewBTN);
        frame.getContentPane().add(viewPNL, BorderLayout.NORTH);
        frame.getContentPane().add(workoutSP, BorderLayout.CENTER);
        // frame.getContentPane().add(buttonPNL,BorderLayout.SOUTH);
    }

    protected StatusPane getStatusEP() {
        return statusEP;
    }

    public WorkoutPane getWorkoutEP() {
        return workoutEP;
    }

    /*
     * CHT public WorkoutChartPanel getChartPNL() { return chartPNL; }
     */
    public JPanel getButtonPNL() {
        return buttonPNL;
    }
}
