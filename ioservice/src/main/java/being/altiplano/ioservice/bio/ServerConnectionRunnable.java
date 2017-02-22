package being.altiplano.ioservice.bio;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;
import being.altiplano.config.commands.*;
import being.altiplano.config.replies.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by gaoyuan on 20/02/2017.
 */

class ServerConnectionRunnable implements Runnable {
    private Socket socket;
    private final Gson gson;
    private long responseLag = 0;

    public ServerConnectionRunnable(Socket client) {
        socket = client;
        gson = new Gson();
    }

    public void run() {
        try (ServerConnection serverConnection = new ServerConnection(socket)){
            boolean nextCmd = true;
            do {
                Command command = serverConnection.readCommand();
                Reply reply = handle(command);
                serverConnection.writeReply(reply);
                if (reply instanceof StopReply) {
                    nextCmd = false;
                }
            } while (nextCmd);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private Reply handle(Command command) {
        switch (command.code()) {
            case Command.START:
                return handleStart((StartCommand) command);
            case Command.STOP:
                return handleStop((StopCommand) command);
        }
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(responseLag));
        switch (command.code()) {
            case Command.ECHO:
                return handleEcho((EchoCommand) command);
            case Command.COUNT:
                return handleCount((CountCommand) command);
            case Command.REVERSE:
                return handleReverse((ReverseCommand) command);
            case Command.LOWER_CAST:
                return handleLowerCast((LowerCastCommand) command);
            case Command.UPPER_CAST:
                return handleUpperCast((UpperCastCommand) command);
            default:
                throw new IllegalStateException();
        }
    }

    private StartReply handleStart(StartCommand command) {
        String content = command.getContent();
        StartCommand.Config config = gson.fromJson(content, StartCommand.Config.class);
        this.responseLag = config.lag;
        return new StartReply();
    }

    private StopReply handleStop(StopCommand command) {
        return new StopReply();
    }

    private EchoReply handleEcho(EchoCommand command) {
        int times = command.times();
        String content = command.getContent();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; ++i) {
            sb.append(content);
        }

        return new EchoReply(sb.toString());
    }

    private CountReply handleCount(CountCommand command) {
        String content = command.getContent();

        return new CountReply(content.length());
    }

    private ReverseReply handleReverse(ReverseCommand command) {
        String content = command.getContent();
        StringBuilder sb = new StringBuilder(content);
        return new ReverseReply(sb.reverse().toString());
    }

    private LowerCastReply handleLowerCast(LowerCastCommand command) {
        String content = command.getContent();
        return new LowerCastReply(content.toLowerCase());
    }

    private UpperCastReply handleUpperCast(UpperCastCommand command) {
        String content = command.getContent();
        return new UpperCastReply(content.toUpperCase());
    }
}