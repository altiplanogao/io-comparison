package being.altiplano.ioservice.junitext.runner;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadRunner extends BlockJUnit4ClassRunner {

    private static final ExecutorService es;
    private CountDownLatch childrenFinish = null;

    static {
        int maxThreads = Runtime.getRuntime().availableProcessors();
        es = Executors.newFixedThreadPool(maxThreads);
    }

    public MultiThreadRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        es.submit(new Test(method, notifier));
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        childrenFinish = new CountDownLatch(getChildren().size());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                MultiThreadRunner.super.childrenInvoker(notifier).evaluate();
                childrenFinish.await();
            }
        };
    }

    class Test implements Runnable {
        private final FrameworkMethod method;
        private final RunNotifier notifier;

        public Test(final FrameworkMethod method, final RunNotifier notifier) {
            this.method = method;
            this.notifier = notifier;
        }

        @Override
        public void run() {
            try {
                MultiThreadRunner.super.runChild(method, notifier);
            } finally {
                childrenFinish.countDown();
            }
        }
    }
}
