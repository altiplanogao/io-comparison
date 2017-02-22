package being.altiplano.config;

import being.altiplano.config.commands.*;
import being.altiplano.config.replies.*;

import java.io.UnsupportedEncodingException;

/**
 * Created by gaoyuan on 21/02/2017.
 */
public class MsgConverter {
    public static final String ENCODING = "UTF-8";

    public static Command convert(Msg msg) {
        int code = msg.code;
        switch (code) {
            case Command.START:
                return new StartCommand(msg.data);
            case Command.STOP:
                return new StopCommand();
            case Command.ECHO:
                return new EchoCommand(msg.data);
            case Command.COUNT:
                return new CountCommand(msg.data);
            case Command.REVERSE:
                return new ReverseCommand(msg.data);
            case Command.LOWER_CAST:
                return new LowerCastCommand(msg.data);
            case Command.UPPER_CAST:
                return new UpperCastCommand(msg.data);
            default:
                throw new IllegalStateException("msg code not supported.");
        }
    }

    public static Reply convertReply(Msg msg) {
        int code = msg.code;
        switch (code) {
            case Command.START:
                return new StartReply();
            case Command.STOP:
                return new StopReply();
            case Command.ECHO:
                return new EchoReply(msg.data);
            case Command.COUNT:
                return new CountReply(msg.data);
            case Command.REVERSE:
                return new ReverseReply(msg.data);
            case Command.LOWER_CAST:
                return new LowerCastReply(msg.data);
            case Command.UPPER_CAST:
                return new UpperCastReply(msg.data);
            default:
                throw new IllegalStateException("msg code not supported.");
        }
    }

    public static byte[] stringToBytes(String data) {
        try {
            return data.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static String bytesToString(byte[] data) {
        try {
            return new String(data, ENCODING);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

}
