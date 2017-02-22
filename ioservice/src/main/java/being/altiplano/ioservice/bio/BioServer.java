package being.altiplano.ioservice.bio;

import being.altiplano.config.ServerConfig;
import being.altiplano.ioservice.IServer;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by gaoyuan on 20/02/2017.
 */
public class BioServer implements IServer {
    private final int PORT;
    private final ExecutorService executorService;
    private volatile ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private CountDownLatch connectionLatch;

    public BioServer() {
        this(ServerConfig.DEFAULT_PORT);
    }

    public BioServer(int PORT) {
        this.PORT = PORT;
        serverSocket = null;
        int cpu = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(cpu * 8);
    }

    @Override
    public void start() throws IOException {
        if(running.compareAndSet(false, true)){
            final ServerSocket ss = new ServerSocket(PORT);
            final CountDownLatch latch = new CountDownLatch(1);

            Runnable acceptConnectionRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        while (running.get()) {
                            try {
                                Socket client = ss.accept();
                                executorService.submit(new ServerConnectionRunnable(client));
                            } catch (IOException e) {
                                if (running.get()) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }finally {
                        latch.countDown();
                    }
                }
            };
            serverSocket = ss;
            connectionLatch = latch;
            executorService.submit(acceptConnectionRunnable);
        }
    }

    @Override
    public void stop(boolean waitDone) throws IOException, InterruptedException {
        final CountDownLatch latch = connectionLatch;
        if(running.compareAndSet(true, false)){
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
            if(waitDone){
                latch.await();
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            stop(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
