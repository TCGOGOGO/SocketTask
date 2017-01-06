import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

public class Client {

    public volatile Socket socket = null;
    private static BufferedReader br = null;
    private static PrintWriter pw = null;
    public Scanner in = new Scanner(System.in);
    public long startTime = 0;

    public Client() {
        try {
            socket = new Socket(Server.HOST, Server.PORT);
            // 读取从客户端发来的消息
            br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            // 写入信息到服务器端
            pw = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())));
            startTime = new Date().getTime();

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
                            if (message == null || message == "") {
                                return;
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

    public void offline() throws IOException {
        System.out.println("服务器: 再见");
        System.out.println("正在下线......");
        System.out.println("在线时长: " + Common.getOnlineTime(this.startTime, new Date().getTime()));
        this.socket.close();
        this.br.close();
        this.pw.close();
        System.out.println("已下线");
        java.lang.System.exit(0);
    }

    public static void main(String[] agrs) {
        new Client();
    }
}
