package being.altiplano.ioservice.junitext.runner;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OrderedRunner extends BlockJUnit4ClassRunner {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Order {
        int value();
    }

    public OrderedRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> list = super.computeTestMethods();
        Collections.sort(list, new Comparator<FrameworkMethod>() {
            @Override
            public int compare(FrameworkMethod f1, FrameworkMethod f2) {
                Order o1 = f1.getAnnotation(Order.class);
                Order o2 = f2.getAnnotation(Order.class);

                if (o1 == null) {
                    if (o2 != null) { // o1 == null, o2 != null
                        return 1;
                    } else {  // o1 == null, o2 == null
                        return f1.getName().compareTo(f2.getName());
                    }
                } else {
                    if (o2 != null) {//o1 != null, o2 != null
                        return o1.value() - o2.value();
                    } else { //o1 != null, o2 == null
                        return -1;
                    }
                }
            }
        });
        return list;
    }
}
