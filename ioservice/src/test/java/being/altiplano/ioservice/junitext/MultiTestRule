package being.altiplano.ioservice.junitext;


import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class MultiTestRule implements TestRule {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.TYPE})
    public @interface Repeat {
        int value() default 1;

        long timeout() default 0L;

        boolean parallel() default false;
    }

    private final static boolean print;

    static {
        print = "true".equals(System.getProperty("repeat.print"));
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        Statement result = statement;
        Class<?> testClass = description.getTestClass();
        String methodName = testClass.getSimpleName() + "#" + description.getMethodName();
        Repeat repeat = description.getAnnotation(Repeat.class);
        if (repeat == null) {
            repeat = (Repeat) testClass.getAnnotation(Repeat.class);
        }
        if (repeat != null) {
            int times = repeat.value();
            long timeout = repeat.timeout();
            if (timeout > 0) {
                result = new FailOnTimeout(result, timeout);
            }
            if (times > 1) {
                result = new RepeatStatement(times, repeat.parallel(), result, methodName);
            }
        }
        return result;
    }

    private static class RepeatStatement extends Statement {

        private final int times;
        private final boolean parallel;
        private final Statement statement;
        private final String methodName;

        private RepeatStatement(int times, boolean parallel, Statement statement, String methodName) {
            this.times = times;
            this.parallel = parallel;
            this.statement = statement;
            this.methodName = methodName;
        }

        @Override
        public void evaluate() throws Throwable {
            long start = System.currentTimeMillis();
            if (parallel) {
                CountDownLatch done = new CountDownLatch(times);
                ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                final AtomicReference<Throwable> anyError = new AtomicReference<>(null);
                for (int i = 1; i <= times; i++) {
                    final int idx = i;
                    es.submit(() -> {
                            try {
                                if (anyError.compareAndSet(null, null)) {
                                    if (print) {
                                        System.out.println("Repeat(Parallel) " + methodName + ": " + idx + "/" + times);
                                    }
                                    statement.evaluate();
                                }
                            } catch (Throwable throwable) {
                                anyError.compareAndSet(null, throwable);
                            } finally {
                                done.countDown();
                            }
                        }
                    );
                }
                done.await();
                if (!anyError.compareAndSet(null, null)) {
                    throw anyError.get();
                }
            } else {
                for (int i = 1; i <= times; i++) {
                    long lapsed = System.currentTimeMillis() - start;
                    long each = (i == 1 ? 0 : lapsed / (i - 1));
                    if (print) {
                        System.out.println("Repeat " + methodName + ": " + i + "/" + times + " elapsed(ms):" + lapsed + " each(ms):" + each);
                    }
                    statement.evaluate();
                }
            }
        }
    }
}
