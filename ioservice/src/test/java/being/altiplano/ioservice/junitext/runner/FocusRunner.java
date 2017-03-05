package being.altiplano.ioservice.junitext.runner;

import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public class FocusRunner extends BlockJUnit4ClassRunner {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Focus {
    }

    public FocusRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    private Boolean exceptEnabled = null;

    private boolean isFocusEnabled() {
        if (exceptEnabled == null) {
            getChildren();
        }
        return exceptEnabled;
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        if (isFocusEnabled()) {
            List<TestRule> old = super.getTestRules(target);
            old.removeIf(rule -> rule instanceof Timeout);
            return old;
        } else {
            return super.getTestRules(target);
        }
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> tests = super.getChildren();
        List<FrameworkMethod> focuses = getTestClass().getAnnotatedMethods(Focus.class);
        focuses.retainAll(tests);
        if (focuses.isEmpty()) {
            exceptEnabled = false;
            return tests;
        } else {
            exceptEnabled = true;
            return focuses;
        }
    }
}
