import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public static final int PORT = 8000;
    public static final String HOST = "127.0.0.1";
    public static HashSet<ServerThread> set = new HashSet<>();
    public Scanner in = new Scanner(System.in);
    public Socket socket = null;
    private ServerSocket serverSocket = null;

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("服务器启动成功");

            // 新开一个线程发广播或执行服务器的查询和强制下线功能
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        synchronized (this) {
                            String message = in.nextLine();
                            if (message.equals("查询在线用户")) {
                                List<ServerThread> list = new ArrayList<>();
                                for (ServerThread st : set) {
                                    list.add(st);
                                }
                                if (list.isEmpty()) {
                                    System.out.println("当前没有人在线");
                                }
                                else {
                                    System.out.println("当前在线用户有:");
                                    for (int i = 0; i < list.size(); i ++) {
                                        System.out.println(list.get(i).getUserName() + " 主机号: " + Common.getPort(list.get(i).getSocket()));
                                    }
                                }
                            }
                            else if (message.contains("强制下线 ")) {
                                String name = "";
                                int idx = 5;
                                for (; idx < message.length(); idx ++) {
                                    name += message.charAt(idx);
                                }
                                for (ServerThread st : set) {
                                    if (st.getUserName().equals(name)) {
                                        try {
                                            st.sendMessage("再见", st);
                                            break;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                 }
                            }
                            else if (message.contains("发送单播消息")) {
                                System.out.print("请输入主机号: ");
                                int port = in.nextInt();
                                System.out.println("发送内容: ");
                                for (ServerThread st : set) {
                                    try {
                                        message = in.next();
                                        if (Common.getPort(st.getSocket()).equals(port + "")) {
                                            st.sendMessage(message, st);
                                            break;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            else if (message.contains("查询主机使用情况")) {
                                System.out.print("请输入主机标号: ");
                                int port = in.nextInt();
                                try {
                                    new FileHelper(port + "").readFromFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
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

