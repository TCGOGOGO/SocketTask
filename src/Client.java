import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client {

    public volatile Socket socket = null;
    private static BufferedReader br = null;
    private static PrintWriter pw = null;
    private long startTime = 0;
    public static volatile String userName;
    private int port = Server.PORT;
    private FileHelper fileHelper = null;

    private JFrame frame;
    private JLabel userNameLabel;
    private JTextField userNameTextFile;
    private JRadioButton randomRadioButton;
    private JRadioButton selectRadioButton;
    private JTextField selectPortTextField;
    private ButtonGroup myRadioButton;
    private JButton okButton;
    private JPanel enterPanel;
    private JLabel sendMsgLabel;
    private JTextArea inputMsgTextArea;
    private JButton sendButton;
    private JButton offlineButton;
    private JPanel otherPanel;
    private JPanel panel;


    public Client() {
        try {
            addClientUI();
            InitWork();
            while(!Common.isCon(socket)) {}
            rebuildUI();
            startTime = new Date().getTime();
            fileHelper = new FileHelper(Common.getLocalPort(socket));
            //新开一个线程收取服务器发来的消息
            offlineButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        offline();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String message = "";
                    while (true) {
                        try {
                            if (socket.isClosed()) {
                                return;
                            }
                            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            message = br.readLine();
                            if (message == null) {
                                return;
                            }
                            if (message.length() == 0) {
                                continue;
                            }
                            if (message.equals("再见")) {
                                pw.println(message);
                                pw.flush();
                                offline();
                                return;
                            }
                            else {
                                System.out.println("服务器: " + message);
                            }
                        } catch (IOException e1) {
                        }
                    }
                }
            }).start();

            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String message = inputMsgTextArea.getText();
                    pw.println(message);
                    pw.flush();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void InitWork() throws IOException {

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = userNameTextFile.getText();
                if (randomRadioButton.isSelected()) {
                    try {
                        socket = new Socket(Server.HOST, Server.PORT);
                        online(socket);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                else if (selectRadioButton.isSelected()) {
                    port = Integer.parseInt(selectPortTextField.getText());
                    try {
                        if (port != Server.PORT) {
                            Process p = null;
                            String tmp = "", line = "";
                            int cnt = 0;
                            p = Runtime.getRuntime().exec("lsof -i:" + port + "");
                            BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            while ((line = br2.readLine()) != null && cnt < 4) {
                                cnt++;
                                tmp += line;
                            }
                            br2.close();
                            if (!tmp.contains("ESTABLISHED")) {
                                socket = new Socket(Server.HOST, Server.PORT, InetAddress.getLocalHost(), port);
                                online(socket);
                            } else {
                                System.out.println("该主机已被占用,请换一台或选择随机分配");
                            }
                        }
                        else {
                            JOptionPane.showMessageDialog(null, "请输入主机号");
                        }
                    } catch(IOException e1){
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public void online(Socket socket) {
        try {
            if (Common.isCon(socket)) {
                // 读取从客户端发来的消息
                br = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                // 写入信息到服务器端
                pw = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())));
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                pw.println(userName + "连接成功," + " 所用主机号: " + Common.getLocalPort(socket) + ", 上线时间: " + sdf.format(new Date()));
                pw.flush();
                System.out.println("您已成功上线");
                JOptionPane.showMessageDialog(null, "在" + Common.getLocalPort(socket) +"号主机上线成功");
            } else {
                System.out.println("无法上线");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void offline() throws IOException {
        pw.println("再见");
        pw.flush();
        System.out.println("服务器: 再见");
        System.out.println("正在下线......");
        long endTime = new Date().getTime();
        String onlineTime = Common.getOnlineTime(startTime, endTime);
        String cost = Common.getCost(startTime, endTime);
        System.out.println("在线时长: " + onlineTime);
        System.out.println("需要付网费: " + cost);
        fileHelper.writeToFile(userName + " 上机时间:" + onlineTime + " 网费:" + cost + "\n");
        this.socket.close();
        br.close();
        pw.close();
        System.out.println("已下线");
        JOptionPane.showMessageDialog(null, userName + " 您已下线\n" + "在线时长: " + onlineTime + "\n需要付网费: " + cost + "\n");
        java.lang.System.exit(0);
    }

    public void addClientUI() {

        frame = new JFrame("客户端");

        userNameLabel = new JLabel("用户名: ");
        userNameTextFile = new JTextField(5);

        randomRadioButton = new JRadioButton("随机分配");
        selectRadioButton = new JRadioButton("选择主机");
        selectPortTextField = new JTextField(5);
        okButton = new JButton("上线");
        myRadioButton = new ButtonGroup();
        myRadioButton.add(randomRadioButton);
        myRadioButton.add(selectRadioButton);
        enterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        enterPanel.add(new Label("  "));
        enterPanel.add(userNameLabel);
        enterPanel.add(userNameTextFile);
        enterPanel.add(new Label("                                  "));
        enterPanel.add(randomRadioButton);
        enterPanel.add(new Label("                                          "));
        enterPanel.add(selectRadioButton);
        enterPanel.add(selectPortTextField);
        enterPanel.add(new Label("                      "));
        enterPanel.add(okButton);

        sendMsgLabel = new JLabel("消息: ");
        inputMsgTextArea = new JTextArea(3, 10);
        sendButton = new JButton("发送");
        offlineButton = new JButton("下线");
        otherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        otherPanel.add(sendMsgLabel);
        otherPanel.add(inputMsgTextArea);
        otherPanel.add(new Label("    "));
        otherPanel.add(sendButton);
        otherPanel.add(new Label("                                                                           "));
        otherPanel.add(offlineButton);

        panel = new JPanel(new GridLayout(2, 1));
        panel.add(enterPanel);
        panel.add(otherPanel);

        frame.setSize(300, 300);
        frame.add(panel);
        frame.setVisible(true);
        frame.validate();
        frame.setResizable(false);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    offline();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    public void rebuildUI() {
        frame.dispose();
        JFrame reFrame = new JFrame(Common.getLocalPort(socket) + "号主机" + " 在线用户: " + userName);
        reFrame.add(otherPanel);
        reFrame.setBounds(400, 300, 350, 150);
        reFrame.setVisible(true);
        reFrame.setResizable(false);
        reFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                try {
                    offline();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });
    }
}
