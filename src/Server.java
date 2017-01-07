import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class Server {

    public static final int PORT = 8000;
    public static final String HOST = "127.0.0.1";
    public static HashSet<ServerThread> set = new HashSet<>();
    public Socket socket = null;
    private ServerSocket serverSocket = null;

    private JFrame frame;
    private JTextField inputPortTextField1;
    private JLabel sendToLabel;
    private JTextField inputPortTextField2;
    private JTextArea inputUnicastMsgTextArea;
    private JLabel queryWitchLabel;
    private JTextField inputPortTextField3;
    private JTextArea inputBroadCastMsgTextArea;
    private JButton okButton;

    private JRadioButton sendUnicastMsgRadioButton;
    private JRadioButton sendBroadCastMsgRadioButton;
    private JRadioButton forceOfflineRadioButton;
    private JRadioButton queryHostRadioButton;
    private JRadioButton queryOnlineUserRadioButton;
    private ButtonGroup buttonGroupOption;

    private JPanel queryOnlineUserPanel;
    private JPanel forceOfflinePanel;
    private JPanel sendUnicastMsgPanel;
    private JPanel queryHostPanel;
    private JPanel sendBroadCastMsgPanel;
    private JPanel okPanel;
    private JPanel panel;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("服务器启动成功");

            // 新开一个线程发广播或执行服务器的查询和强制下线功能
            new Thread(new Runnable() {
                @Override
                public void run() {
                    addServerUI();
                    synchronized (this) {
                        okButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (sendUnicastMsgRadioButton.isSelected()) {
                                    int port = Integer.parseInt(inputPortTextField1.getText());
                                    StringBuffer message = new StringBuffer(inputUnicastMsgTextArea.getText());
                                    boolean hasThisPort = false;
                                    for (ServerThread st : set) {
                                        try {
                                            if (Common.getPort(st.getSocket()).equals(port + "")) {
                                                st.sendMessage(message.toString(), st);
                                                hasThisPort = true;
                                                break;
                                            }
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                    if (!hasThisPort) {
                                        System.out.println(port + "号主机当前不在线");
                                    }
                                } else if (sendBroadCastMsgRadioButton.isSelected()) {
                                    StringBuffer message = new StringBuffer(inputBroadCastMsgTextArea.getText());
                                    if (set.isEmpty()) {
                                        System.out.println("当前没有主机在线");
                                    }
                                    for (ServerThread st : set) {
                                        try {
                                            st.sendMessage(message.toString(), st);
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                } else if (forceOfflineRadioButton.isSelected()) {
                                    int port = Integer.parseInt(inputPortTextField2.getText());
                                    boolean hasThisPort = false;
                                    for (ServerThread st : set) {
                                        if (Common.getPort(st.getSocket()).equals(port + "")) {
                                            try {
                                                hasThisPort = true;
                                                st.sendMessage("再见", st);
                                                break;
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    }
                                    if (!hasThisPort) {
                                        System.out.println(port + "号主机当前不在线");
                                    }
                                } else if (queryHostRadioButton.isSelected()) {
                                    int port = Integer.parseInt(inputPortTextField3.getText());
                                    try {
                                        if (!FileHelper.QueryIsExistFile(port + "")) {
                                            //System.out.println(port + "号主机未被使用过");
                                            JOptionPane.showMessageDialog(null, port + "号主机未被使用过");
                                        }
                                        else {
                                            StringBuffer dialogMsg = new StringBuffer(new FileHelper(port + "").readFromFile());
                                            JOptionPane.showMessageDialog(null, dialogMsg.toString());
                                        }
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                } else if (queryOnlineUserRadioButton.isSelected()) {
                                    List<ServerThread> list = new ArrayList<>();
                                    for (ServerThread st : set) {
                                        list.add(st);
                                    }
                                    if (list.isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "当前没有人在线");
                                        //System.out.println("当前没有人在线");
                                    } else {
                                        //System.out.println("当前在线用户有:");
                                        StringBuffer dialogMsg = new StringBuffer("当前在线用户有:\n");
                                        for (int i = 0; i < list.size(); i++) {
                                            //System.out.println(list.get(i).getUserName() + ", 主机号: " + Common.getPort(list.get(i).getSocket()));
                                            dialogMsg.append(list.get(i).getUserName() + ", 主机号: " + Common.getPort(list.get(i).getSocket()) + "\n");
                                        }
                                        JOptionPane.showMessageDialog(null, dialogMsg.toString());
                                    }
                                }
                            }
                        });
                    }
                }
            }).start();

            //监听新上线的客户
            while (true) {
                socket = serverSocket.accept();
                set.add(new ServerThread(socket));
            }
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addServerUI() {

        frame = new JFrame("服务器");

        sendUnicastMsgRadioButton = new JRadioButton("发送单播消息: ");
        sendToLabel = new JLabel("请输入主机号");
        inputPortTextField1 = new JTextField(5);
        inputUnicastMsgTextArea = new JTextArea(3, 10);

        sendBroadCastMsgRadioButton = new JRadioButton("发送广播消息: ");
        inputBroadCastMsgTextArea = new JTextArea(3, 10);

        forceOfflineRadioButton = new JRadioButton("强制下线: ");
        inputPortTextField2 = new JTextField(5);

        queryHostRadioButton = new JRadioButton("查询主机使用情况: ");
        queryWitchLabel = new JLabel("请输入主机标号");
        inputPortTextField3 = new JTextField(5);

        queryOnlineUserRadioButton = new JRadioButton("查询在线用户");

        okButton = new JButton("确定");

        buttonGroupOption = new ButtonGroup();
        buttonGroupOption.add(sendUnicastMsgRadioButton);
        buttonGroupOption.add(sendBroadCastMsgRadioButton);
        buttonGroupOption.add(forceOfflineRadioButton);
        buttonGroupOption.add(queryHostRadioButton);
        buttonGroupOption.add(queryOnlineUserRadioButton);

        sendUnicastMsgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sendUnicastMsgPanel.add(sendUnicastMsgRadioButton);
        sendUnicastMsgPanel.add(sendToLabel);
        sendUnicastMsgPanel.add(inputPortTextField1);
        sendUnicastMsgPanel.add(inputUnicastMsgTextArea);

        sendBroadCastMsgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sendBroadCastMsgPanel.add(sendBroadCastMsgRadioButton);
        sendBroadCastMsgPanel.add(inputBroadCastMsgTextArea);

        forceOfflinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        forceOfflinePanel.add(forceOfflineRadioButton);
        forceOfflinePanel.add(inputPortTextField2);

        queryHostPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryHostPanel.add(queryHostRadioButton);
        queryHostPanel.add(queryWitchLabel);
        queryHostPanel.add(inputPortTextField3);

        queryOnlineUserPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        queryOnlineUserPanel.add(queryOnlineUserRadioButton);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(sendUnicastMsgPanel);
        panel.add(sendBroadCastMsgPanel);
        panel.add(forceOfflinePanel);
        panel.add(queryHostPanel);
        panel.add(queryOnlineUserPanel);

        okPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        okPanel.add(new Label("                                                            "));
        okPanel.add(okButton);
        panel.add(okPanel);

        frame.setSize(430, 370);
        frame.add(panel);
        frame.setVisible(true);
        frame.validate();
        frame.setResizable(false);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
    }
}

