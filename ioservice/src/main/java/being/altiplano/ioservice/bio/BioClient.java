package being.altiplano.ioservice.bio;

import being.altiplano.config.ServerConfig;
import being.altiplano.config.commands.*;
import being.altiplano.config.commands.StopCommand;
import being.altiplano.config.replies.*;
import being.altiplano.config.replies.StartReply;
import being.altiplano.config.replies.StopReply;
import being.altiplano.ioservice.IClient;

import java.io.*;
import java.net.Socket;

/**
 * Created by gaoyuan on 22/02/2017.
 */
public class BioClient implements IClient {
    public final String IP_ADDR;
    public final int PORT;
    private ClientConnection clientConnection;

    public BioClient(String IP_ADDR, int PORT) {
        this.IP_ADDR = IP_ADDR;
        this.PORT = PORT;
    }

    public BioClient(int PORT) {
        this("localhost", PORT);
    }

    public BioClient() {
        this(ServerConfig.DEFAULT_PORT);
    }

    @Override
    public void connect()throws IOException{
        close();
        clientConnection = new ClientConnection(new Socket(IP_ADDR, PORT));
    }

    @Override
    public void disConnect() throws IOException {
        close();
    }

    public void close() throws IOException {
        if(clientConnection != null){
            clientConnection.close();
            clientConnection = null;
        }
    }

    @Override
    public StartReply call(StartCommand command) throws IOException {
        synchronized (clientConnection) {
            clientConnection.writeCommand(command);
            return (StartReply) clientConnection.readReply();
        }
    }

    @Override
    public StopReply call(StopCommand command) throws IOException {
        synchronized (clientConnection) {
            clientConnection.writeCommand(command);
            return (StopReply) clientConnection.readReply();
        }
    }

    @Override
    public EchoReply call(EchoCommand command) throws IOException {
        synchronized (clientConnection) {
            clientConnection.writeCommand(command);
            return (EchoReply) clientConnection.readReply();
        }
    }

    @Override
    public CountReply call(CountCommand command) throws IOException {
        synchronized (clientConnection) {
            clientConnection.writeCommand(command);
            return (CountReply) clientConnection.readReply();
        }
    }

    @Override
    public ReverseReply call(ReverseCommand command) throws IOException {
        synchronized (clientConnection) {
            clientConnection.writeCommand(command);
            return (ReverseReply) clientConnection.readReply();
        }
    }

    @Override
    public LowerCastReply call(LowerCastCommand command) throws IOException {
        synchronized (clientConnection) {
            clientConnection.writeCommand(command);
            return (LowerCastReply) clientConnection.readReply();
        }
    }

    @Override
    public UpperCastReply call(UpperCastCommand command) throws IOException {
        synchronized (clientConnection) {
            clientConnection.writeCommand(command);
            return (UpperCastReply) clientConnection.readReply();
        }
    }
}
