import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerThread extends Thread {

    private volatile Socket socket = null;
    private static BufferedReader br = null;
    private static PrintWriter pw = null;
    private String userName;

    public ServerThread(Socket s) {
        socket = s;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())), true);
            String linkMessage = br.readLine();
            System.out.println(linkMessage);
            userName = genUserName(linkMessage);
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUserName() {
        return userName;
    }

    public String genUserName(String str) {
        String name = "";
        int end = str.indexOf("连接成功");
        for (int i = 0; i < end; i ++) {
            name += str.charAt(i);
        }
        return name;
    }

    public void run() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
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
                                System.out.println("用户 " + userName + "已下线");
                                Server.set.remove(ServerThread.this);
                                return;
                            }
                            else {
                                System.out.println("用户 " + userName + ": " + message);
                            }
                        } catch (IOException e) {
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                        br.close();
                        pw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void sendMessage(String message, ServerThread st) throws IOException {
        pw = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(st.getSocket().getOutputStream())), true);
        pw.println(message);
        pw.flush();
    }
}