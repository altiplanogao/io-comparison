package being.altiplano.ioservice.bio;

import being.altiplano.config.Command;
import being.altiplano.config.Reply;
import being.altiplano.config.replies.StopReply;
import being.altiplano.ioservice.ServerCommandHandler;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by gaoyuan on 20/02/2017.
 */

class ServerConnectionRunnable implements Runnable {
    private final Socket socket;
    private final ServerCommandHandler commandHandler = new ServerCommandHandler();

    private void log(String content) {
        System.out.println("    Server " + content);
    }


    public ServerConnectionRunnable(Socket client) {
        socket = client;
    }

    public void run() {
        Thread.currentThread().setName(this.getClass().getSimpleName());
        try (BioServerConnection serverConnection = new BioServerConnection(socket)) {
            boolean nextCmd = true;
            do {
                Command command = serverConnection.readCommand();
                Reply reply = commandHandler.handle(command);

                serverConnection.writeReply(reply);
                if (reply instanceof StopReply) {
                    nextCmd = false;
                }
            } while (nextCmd);
        } catch (EOFException e) {
            if (!socket.isClosed()) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }
}