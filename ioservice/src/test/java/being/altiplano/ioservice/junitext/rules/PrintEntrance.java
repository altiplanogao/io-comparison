package being.altiplano.ioservice.junitext.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Created by gaoyuan on 24/02/2017.
 */
public class PrintEntrance implements TestRule {
    private boolean printPre = false;
    private boolean printPost = false;

    public PrintEntrance() {
        this(true, false);
    }

    public PrintEntrance(boolean printPre, boolean printPost) {
        this.printPre = printPre;
        this.printPost = printPost;
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    if (printPre) {
                        System.out.println("Enter -->> " + description.getTestClass() + "#" + description.getMethodName());
                    }
                    statement.evaluate();
                } finally {
                    if (printPost) {
                        System.out.println("Leave -->> " + description.getMethodName());
                    }
                }
            }
        };
    }
}
