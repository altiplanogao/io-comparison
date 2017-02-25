package being.altiplano.ioservice.junitext;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Created by gaoyuan on 24/02/2017.
 */
public class PrintEntrance implements TestRule {
    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    System.out.println("Enter -->> " + description.getTestClass() + "#" + description.getMethodName());
                    statement.evaluate();
                } finally {
                    System.out.println("Leave -->> " + description.getMethodName());
                }
            }
        };
    }
}
