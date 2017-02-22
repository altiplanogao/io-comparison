package being.altiplano.ioservice;

import being.altiplano.ioservice.bio.BioClient;
import being.altiplano.ioservice.bio.BioServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by gaoyuan on 22/02/2017.
 */
public class BioServerClientTest extends SocketServerClientTestBase {
    private Random random = new Random();

    protected void testFunction(final Class<? extends IServer> serverClz,
                                final Class<? extends IClient> clientClz) throws IOException {
        boolean success = false;
        try (IServer server = createSocketServer(serverClz)) {
            server.start();
            IClient client = createSocketClient(clientClz);

            checkEcho(client);
            checkCount(client);
            checkReverse(client);
            checkUpperCast(client);
            checkLowerCast(client);

            checkRandom(client, 5 + random.nextInt(10));

            closeSocketClient(client);
            success = true;
        } catch (IOException e) {
            throw e;
        } finally {
            Assert.assertTrue(success);
        }
    }

    public void testNClientFunction(final Class<? extends IServer> serverClz,
                                    final Class<? extends IClient> clientClz) throws IOException {
        boolean success = false;
        int clientCount = 1 + random.nextInt(10);
        final AtomicInteger successCounter = new AtomicInteger(0);
        final CountDownLatch clientLatch = new CountDownLatch(clientCount);
        final CyclicBarrier clientBarrier = new CyclicBarrier(clientCount);

        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);
        try (IServer server = createSocketServer(serverClz)) {
            server.start();

            for (int c = 0; c < clientCount; ++c) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IClient client = createSocketClient(clientClz);
                            clientBarrier.await();

                            checkEcho(client);
                            checkCount(client);
                            checkReverse(client);
                            checkUpperCast(client);
                            checkLowerCast(client);

                            checkRandom(client, 5 + new Random().nextInt(10));

                            closeSocketClient(client);
                            successCounter.incrementAndGet();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (BrokenBarrierException e) {
                            e.printStackTrace();
                        } finally {
                            clientLatch.countDown();
                        }
                    }
                };

                es.submit(runnable);
            }
            clientLatch.await();
            success = true;
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Assert.assertEquals(clientCount, successCounter.get());
            Assert.assertTrue(success);
        }
    }

    @Test(timeout = 10_000)
    public void testFunction() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        testFunction(serverClz, clientClz);
    }

    @Test(timeout = 10_000)
    public void testNClientFunction() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        testNClientFunction(serverClz, clientClz);
    }
}