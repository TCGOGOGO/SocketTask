import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    public static final int PORT = 8000;
    public static final String HOST = "127.0.0.1";
    public static HashSet<ServerThread> set = new HashSet<>();
    public static Map<Socket, String> mp = new HashMap<>();
    public Scanner in = new Scanner(System.in);
    public Socket socket = null;
    private ServerSocket serverSocket = null;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            if (serverSocket != null) {
                System.out.println("服务器启动成功");
            }

            // 新开一个线程发广播或执行服务器的查询和强制下线功能
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        synchronized (this) {
                            String message = in.nextLine();
                            if (message.equals("查询在线用户")) {
                                boolean exist = false;
                                List<Socket> list = new ArrayList<>();
                                for (ServerThread st : set) {
                                    exist = true;
                                    list.add(st.getSocket());
                                }
                                if (!exist) {
                                    System.out.println("当前没有人在线");
                                }
                                else {
                                    System.out.println("当前在线用户有:");
                                    for (int i = 0; i < list.size(); i ++) {
                                        System.out.println(Common.getPort(list.get(i)));
                                    }
                                }
                            }
                            else if (message.indexOf("强制下线 ") != -1) {
                                String name = "";
                                int idx = 5;
                                for (; idx < message.length(); idx ++) {
                                    name += message.charAt(idx);
                                }
                                for (ServerThread st : set) {
                                    if (Common.getPort(st.getSocket()).equals(name)) {
                                        try {
                                            st.sendMessage("再见", st);
                                            break;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                 }
                            }
                            else {
                                for (ServerThread st : set) {
                                    try {
                                        st.sendMessage(message, st);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }).start();

            //监听新上线的客户
            while (true) {
                socket = serverSocket.accept();
                mp.put(socket, Common.getPort(socket));
                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                System.out.println("客户"  + mp.get(socket) + "连接成功, 上线时间为" + sdf.format(new Date()));
                set.add(new ServerThread(socket));
            }
        } catch (Exception e) {
            try {
                socket.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] agrs) throws IOException {
        new Server();
    }
}

