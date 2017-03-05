package being.altiplano.ioservice;

import being.altiplano.ioservice.aio.AioClient;
import being.altiplano.ioservice.aio.AioServer;
import being.altiplano.ioservice.bio.BioClient;
import being.altiplano.ioservice.bio.BioServer;
import being.altiplano.ioservice.junitext.rules.MultiTestRule;
import being.altiplano.ioservice.junitext.rules.PrintEntrance;
import being.altiplano.ioservice.mina.MinaClient;
import being.altiplano.ioservice.mina.MinaServer;
import being.altiplano.ioservice.netty.NettyClient;
import being.altiplano.ioservice.netty.NettyServer;
import being.altiplano.ioservice.nio.NioClient;
import being.altiplano.ioservice.nio.NioServer;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

@MultiTestRule.Repeat(value = 2, timeout = ServerClientTest.EACH_TIMEOUT, printStep = true)
public class ServerClientTest extends ServerClientTestBase {

    public static final int EACH_TIMEOUT = 10_000;

    @Rule
    public MultiTestRule multiTestRule = new MultiTestRule();

    @Rule
    public PrintEntrance printEntrance = new PrintEntrance(true, true);

    protected void doTest_SC(final Class<? extends IServer> serverClz,
                             final Class<? extends IClient> clientClz) throws IOException {
        final String testName = "[" + serverClz.getSimpleName() + " with " + clientClz.getSimpleName() + "] ";
        boolean success = false;
        try (IServer server = createServer(serverClz)) {
            server.start();
            IClient client = createClient(clientClz);

            checkEcho(client);
            checkCount(client);
            checkReverse(client);
            checkUpperCast(client);
            checkLowerCast(client);

            checkRandom(client, 5 + random.nextInt(10));

            closeClient(client);
            success = true;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            collector.addError(e);
        } finally {
            collector.checkThat(testName + "failed", success, CoreMatchers.is(true));
        }
    }

    public void doTest_SnC(final Class<? extends IServer> serverClz,
                           final Class<? extends IClient> clientClz) throws IOException {
        int clientCount = 1 + random.nextInt(10);
        doTest_SnC(serverClz, clientClz, clientCount);
    }

    public void doTest_SnC(final Class<? extends IServer> serverClz,
                           final Class<? extends IClient> clientClz, final int clientCount) throws IOException {
        boolean success = false;
        final AtomicInteger successCounter = new AtomicInteger(0);
        final CountDownLatch clientLatch = new CountDownLatch(clientCount);
        final CyclicBarrier clientBarrier = new CyclicBarrier(clientCount);
        final ExecutorService es = Executors.newCachedThreadPool();


        try (IServer server = createServer(serverClz)) {
            server.start();

            for (int c = 0; c < clientCount; ++c) {
                final int cIdx = c;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IClient client = createClient(clientClz);
                            clientBarrier.await();

                            checkEcho(client);
                            checkCount(client);
                            checkReverse(client);
                            checkUpperCast(client);
                            checkLowerCast(client);

                            checkRandom(client, 5 + new Random().nextInt(10));

                            closeClient(client);
                            successCounter.incrementAndGet();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            collector.addError(e);
                        } finally {
                            clientLatch.countDown();
                        }

                    }
                };
                es.submit(runnable);
            }
            clientLatch.await();
            success = true;
            es.shutdown();
            while (!es.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                LockSupport.park(10_000_000);
            }
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            collector.addError(e);
        } finally {
            collector.checkThat("" + serverClz + "," + clientClz, clientCount, CoreMatchers.equalTo(successCounter.get()));
            collector.checkThat("" + serverClz + "," + clientClz, success, CoreMatchers.is(true));
        }
    }

    protected final static Class<? extends IServer>[] serverTypes = new Class[]{
            BioServer.class,
            NioServer.class,
            AioServer.class,
            MinaServer.class,
            NettyServer.class,
    };
    protected final static Class<? extends IClient>[] clientTypes = new Class[]{
            BioClient.class,
            NioClient.class,
            AioClient.class,
            MinaClient.class,
            NettyClient.class,
    };

    @Test
    public void test_BioServer_BioClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SC(serverClz, clientClz);
    }

    @Test
    public void test_NioServer() throws IOException {
        final Class<? extends IServer> serverClz = NioServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SC(serverClz, clientClz);
    }

    @Test
    public void test_NioClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = NioClient.class;
        doTest_SC(serverClz, clientClz);
    }

    @Test
    public void test_AioServer() throws IOException {
        final Class<? extends IServer> serverClz = AioServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SC(serverClz, clientClz);
    }

    @Test
    public void test_AioClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = AioClient.class;
        doTest_SC(serverClz, clientClz);
    }

    @Test
    public void test_NettyServer() throws IOException {
        final Class<? extends IServer> serverClz = NettyServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SC(serverClz, clientClz);
    }

    @Test
    public void test_NettyClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = NettyClient.class;
        doTest_SC(serverClz, clientClz);
    }

    @Test
    public void test_MinaServer() throws IOException {
        final Class<? extends IServer> serverClz = MinaServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SC(serverClz, clientClz);
    }

    @Test
    public void test_MinaClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = MinaClient.class;
        doTest_SC(serverClz, clientClz);
    }


    @Test
    public void testN_BioServer_BioClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    public void testN_NioServer() throws IOException {
        final Class<? extends IServer> serverClz = NioServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    public void testN_NioClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = NioClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    public void testN_AioServer() throws IOException {
        final Class<? extends IServer> serverClz = AioServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    public void testN_AioClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = AioClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    public void testN_NettyServer() throws IOException {
        final Class<? extends IServer> serverClz = NettyServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    public void testN_NettyClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = NettyClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    public void testN_MinaServer() throws IOException {
        final Class<? extends IServer> serverClz = MinaServer.class;
        final Class<? extends IClient> clientClz = BioClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    public void testN_MinaClient() throws IOException {
        final Class<? extends IServer> serverClz = BioServer.class;
        final Class<? extends IClient> clientClz = MinaClient.class;
        doTest_SnC(serverClz, clientClz);
    }

    @Test
    @MultiTestRule.TimeoutOverride(times = 25)
    public void test_XServer_XClient() throws IOException {
        for (Class<? extends IServer> serverClz : serverTypes) {
            for (Class<? extends IClient> clientClz : clientTypes) {
                doTest_SC(serverClz, clientClz);
            }
        }
    }

    @Test
    @MultiTestRule.TimeoutOverride(times = 250)
    public void test_XServer_XClient_N() throws IOException {
        for (Class<? extends IServer> serverClz : serverTypes) {
            for (Class<? extends IClient> clientClz : clientTypes) {
                doTest_SnC(serverClz, clientClz);
            }
        }
    }
}