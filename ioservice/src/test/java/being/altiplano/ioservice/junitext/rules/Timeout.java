package being.altiplano.ioservice.junitext.rules;

import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A Timeout Rule which will be disabled on maven-debugging
 */
public class Timeout implements TestRule {
    private final int fMillis;

    /**
     * @param millis the millisecond timeout
     */
    public Timeout(int millis) {
        fMillis = millis;
    }

    protected boolean disabled() {
        return "true".equals(System.getProperty("maven.surefire.debug"));
    }

    public Statement apply(Statement base, Description description) {
        if (disabled()) {
            return base;
        } else {
            return new FailOnTimeout(base, fMillis);
        }
    }
}