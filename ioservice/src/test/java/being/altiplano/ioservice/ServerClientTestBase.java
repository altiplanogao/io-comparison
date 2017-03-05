package being.altiplano.ioservice;

import being.altiplano.config.commands.*;
import being.altiplano.config.replies.*;
import net.moznion.random.string.RandomStringGenerator;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.rules.ErrorCollector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerClientTestBase {
    @Rule
    public ErrorCollector collector = new ErrorCollector();

    static final RandomStringGenerator generator = new RandomStringGenerator();
    static final String stringRegex = "\\w+\\d*\\s[0-9]{0,3}X";
    static final Method[] checkMethods;
    static final Random random = new Random();

    static {
        List<Method> lm = new ArrayList<>();
        Method[] allMethods = ServerClientTestBase.class.getDeclaredMethods();
        for (Method m : allMethods) {
            if (m.getName().startsWith("check")) {
                if (m.getName().contains("Start"))
                    continue;
                if (m.getName().contains("Stop"))
                    continue;
                Class<?>[] paramTypes = m.getParameterTypes();
                if (paramTypes.length == 1 && paramTypes[0].equals(IClient.class)) {
                    lm.add(m);
                }
            }
        }
        checkMethods = lm.toArray(new Method[lm.size()]);
    }

    protected void checkStart(IClient client) throws IOException {
        StartCommand.Config startConf = new StartCommand.Config();
        startConf.lag = 20;
        StartReply reply = client.call(new StartCommand(startConf));
        collector.checkThat(reply, CoreMatchers.notNullValue());
    }

    protected void checkStop(IClient client) throws IOException {
        StopReply reply = client.call(new StopCommand());
        collector.checkThat(reply, CoreMatchers.notNullValue());
    }

    protected void checkLowerCast(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        checkLowerCast(client, contentReference);
    }

    protected void checkLowerCast(IClient client, String contentReference) throws IOException {
        LowerCastReply reply = client.call(new LowerCastCommand(contentReference));
        collector.checkThat(reply, CoreMatchers.notNullValue());
        collector.checkThat(reply.getContent(), CoreMatchers.equalTo(contentReference.toLowerCase()));
    }

    protected void checkUpperCast(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        checkUpperCast(client, contentReference);
    }

    protected void checkUpperCast(IClient client, String contentReference) throws IOException {
        UpperCastReply reply = client.call(new UpperCastCommand(contentReference));
        collector.checkThat(reply, CoreMatchers.notNullValue());
        collector.checkThat(reply.getContent(), CoreMatchers.equalTo(contentReference.toUpperCase()));
    }

    protected void checkReverse(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        checkReverse(client, contentReference);
    }

    protected void checkReverse(IClient client, String contentReference) throws IOException {
        ReverseReply reply = client.call(new ReverseCommand(contentReference));
        collector.checkThat(reply, CoreMatchers.notNullValue());
        String expect = new StringBuilder().append(contentReference).reverse().toString();
        collector.checkThat(reply.getContent(), CoreMatchers.equalTo(expect));
    }

    protected void checkCount(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        checkCount(client, contentReference);
    }

    protected void checkCount(IClient client, String contentReference) throws IOException {
        CountReply reply = client.call(new CountCommand(contentReference));
        collector.checkThat(reply, CoreMatchers.notNullValue());
        collector.checkThat(reply.getCount(), CoreMatchers.equalTo(contentReference.length()));
    }

    protected void checkEcho(IClient client) throws IOException {
        String contentReference = generator.generateByRegex(stringRegex);
        int times = 1 + random.nextInt(5);
        checkEcho(client, contentReference, times);
    }

    protected void checkEcho(IClient client, String contentReference, int times) throws IOException {
        EchoReply reply = client.call(new EchoCommand(times, contentReference));
        collector.checkThat(reply, CoreMatchers.notNullValue());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; ++i) {
            sb.append(contentReference);
        }
        collector.checkThat(reply.getContent(), CoreMatchers.equalTo(sb.toString()));
    }

    protected void checkRandom(IClient client, int times) throws IOException {
        int ms = checkMethods.length;
        try {
            for (int i = 0; i < times; ++i) {
                Method m = checkMethods[random.nextInt(ms)];
                m.invoke(this, client);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            collector.addError(e);
        }
    }

    protected void closeClient(IClient client) throws IOException {
        checkStop(client);
        try {
            client.disconnect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected IClient createClient(Class<? extends IClient> clientClz) throws IOException {
        IClient client = null;
        try {
            client = clientClz.getConstructor(String.class, int.class)
                    .newInstance("localhost", TestConfig.PORT);
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            collector.addError(e);
        }
        try {
            client.connect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        checkStart(client);
        return client;
    }

    protected IServer createServer(Class<? extends IServer> serverClz) throws IOException {
        IServer server = null;
        try {
            server = serverClz.getConstructor(int.class).newInstance(TestConfig.PORT);
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException e) {
            collector.addError(e);
        }
        return server;
    }
}