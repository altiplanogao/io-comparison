package being.altiplano.ioservice.junitext;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Created by gaoyuan on 24/02/2017.
 */
public class DebugDedicatedRunner extends BlockJUnit4ClassRunner {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Except{
    }

    public DebugDedicatedRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    private boolean debugOn() {
        return ("true".equals(System.getProperty("maven.surefire.debug")));
    }

    private Boolean exceptEnabled = null;

    private boolean isExceptEnabled() {
        if(exceptEnabled == null){
            getChildren();
        }
        return exceptEnabled;
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        if(isExceptEnabled()) {
            List<TestRule> old = super.getTestRules(target);
            old.removeIf(rule -> rule instanceof Timeout);
            return old;
        } else {
            return super.getTestRules(target);
        }
    }
    
    @Override
    protected List<FrameworkMethod> getChildren() {
        if (debugOn()) {
            List<FrameworkMethod> tests = super.getChildren();
            List<FrameworkMethod> excepts = getTestClass().getAnnotatedMethods(Except.class);
            excepts.retainAll(tests);
            if (excepts.isEmpty()) {
                exceptEnabled = false;
                return tests;
            }else {
                exceptEnabled = true;
                return excepts;
            }
        } else {
            exceptEnabled = false;
            return super.getChildren();
        }
    }
}
