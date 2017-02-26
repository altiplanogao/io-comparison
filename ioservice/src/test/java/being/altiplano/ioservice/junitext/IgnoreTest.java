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
public class IgnoreTest extends BlockJUnit4ClassRunner {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Except{
    }

    public IgnoreTest(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        List<FrameworkMethod> tests = super.getChildren();
        List<FrameworkMethod> except = getTestClass().getAnnotatedMethods(Except.class);
        except.retainAll(tests);
        if(except == null || except.isEmpty()){
            return tests;
        }
        return except;
    }
}
