package being.altiplano.ioservice.junitext;

import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class Timeout implements TestRule {
    private final int fMillis;

    public Timeout(int millis) {
        this.fMillis = millis;
    }

    protected boolean disabled() {
        return "true".equals(System.getProperty("maven.surefire.debug"));
    }

    public Statement apply(Statement base, Description description) {
        if (disabled()) {
            return base;
        } else {
            return new FailOnTimeout(base, (long) this.fMillis);
        }
    }
}
