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

    public ServerThread(Socket s) {
        socket = s;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())), true);
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
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
                            if (message == null || message == "") {
                                return;
                            }
                            if (message.equals("再见")) {
                                System.out.println("用户" + Common.getPort(socket) + "已下线");
                                Server.mp.remove(socket);
                                Server.set.remove(ServerThread.this);
                                return;
                            }
                            else {
                                System.out.println("用户" + Common.getPort(socket) + ": " + message);
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