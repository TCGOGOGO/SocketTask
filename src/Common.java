import java.net.Socket;


public class Common {

    public static String getOnlineTime (long start, long end) {
        long dif = (end - start) / 1000;
        int hour = (int) dif / 3600;
        int minute = (int) (dif - 3600 * hour) / 60;
        return String.valueOf(hour) + "时" + String.valueOf(minute) + "分";
    }

    public static String getPort(Socket socket) {
        String data = socket.toString();
        String name = "";
        int idx = data.indexOf("port") + 5;
        while (idx < data.length() && data.charAt(idx) <= '9' && data.charAt(idx) >= '0') {
            name += data.charAt(idx ++);
        }
        return name;
    }

    public static boolean isCon(Socket socket) {
        if (!socket.isClosed() && socket.isConnected()) {
            return true;
        }
        return false;
    }
}
