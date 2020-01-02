package util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerUtil {
    private static ServerUtil INSTANCE = null;

    private ServerUtil() {
    }

    public static ServerUtil getInstance() {
        if (INSTANCE == null) INSTANCE = new ServerUtil();
        return INSTANCE;
    }

    public String byteToString(ByteBuffer byteBuffer) {
        return StandardCharsets.UTF_8.decode(byteBuffer).toString();
    }

    public ByteBuffer stringToByteBuffer(String s) {
        return StandardCharsets.UTF_8.encode(s);
    }
}
