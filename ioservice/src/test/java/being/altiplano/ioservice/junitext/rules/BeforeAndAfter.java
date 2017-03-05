package being.altiplano.ioservice.junitext.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Run Runnable before and after test
 */
public class BeforeAndAfter implements TestRule {
    public interface Runnable {
        void run() throws Exception;
    }

    private final Runnable before;
    private final Runnable after;

    public BeforeAndAfter(Runnable before, Runnable after) {
        this.before = before;
        this.after = after;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (before == null && after == null)
            return base;
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (before != null) {
                    try {
                        before.run();
                    } catch (Exception e) {
                    }
                }
                base.evaluate();
                if (after != null) {
                    try {
                        after.run();
                    } catch (Exception e) {
                    }
                }
            }
        };
    }
}