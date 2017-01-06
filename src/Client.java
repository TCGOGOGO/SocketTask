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
    private Scanner in = new Scanner(System.in);
    private long startTime = 0;
    private String portName;
    public static volatile String userName;
    private int port = Server.PORT;
    private FileHelper fileHelper = null;

    public Client() {
        try {
            InitWork();
            portName = Common.getLocalPort(socket);
            startTime = new Date().getTime();
            fileHelper = new FileHelper(portName);
            //新开一个线程收取服务器发来的消息
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
                        } catch (IOException e) {
                        }
                    }
                }
            }).start();

            while (Common.isCon(socket)) {
                String message = in.nextLine();
                pw.println(message);
                pw.flush();
                if (message.equals("再见")) {
                    socket.close();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                offline();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void InitWork() throws IOException {
        System.out.print("用户名: ");
        userName = in.next();
        while (socket == null) {
            System.out.println("随机分配(1) 或 选择主机(2)");
            int tp = in.nextInt();
            if (tp == 2) {
                System.out.print("请输入主机编号: ");
                port = in.nextInt();
                Process p = Runtime.getRuntime().exec("lsof -i:" + port + "");
                BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String tmp = "", line = "";
                int cnt = 0;
                while((line = br2.readLine()) != null && cnt < 4){
                    cnt ++;
                    tmp += line;
                }
                br2.close();
                if (!tmp.contains("ESTABLISHED")) {
                    socket = new Socket(Server.HOST, Server.PORT, InetAddress.getLocalHost(), port);
                }
                else {
                    System.out.println("该主机已被占用,请换一台或选择随机分配");
                }
            }
            else {
                socket = new Socket(Server.HOST, Server.PORT);
            }
        }
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
        }
        else {
            System.out.println("无法上线");
        }
    }

    private void offline() throws IOException {
        System.out.println("服务器: 再见");
        System.out.println("正在下线......");
        long endTime = new Date().getTime();
        String onlineTime = Common.getOnlineTime(startTime, endTime);
        String cost = Common.getCost(startTime, endTime);
        System.out.println("在线时长: " + onlineTime);
        System.out.println("需要付网费: " + cost);
        fileHelper.writeToFile(userName + "上机时间:" + onlineTime + " 网费:" + cost + "\n");
        this.socket.close();
        br.close();
        pw.close();
        System.out.println("已下线");
        java.lang.System.exit(0);
    }

    public static void main(String[] agrs) {
        new Client();
    }
}
