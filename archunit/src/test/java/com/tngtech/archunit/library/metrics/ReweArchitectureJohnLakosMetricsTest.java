package com.tngtech.archunit.library.metrics;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.Test;

import java.text.DecimalFormat;

import static org.assertj.core.api.Assertions.assertThat;

import static com.tngtech.archunit.core.domain.TestUtils.importClasses;

public class ReweArchitectureJohnLakosMetricsTest {

    @Test
    public void compute_correct_cumulative_components() {
        JavaClasses classes = importClasses(A.class, B.class, C.class, D.class, E.class, F.class);
        ReweArchitectureJohnLakosMetrics metrics = new ReweArchitectureJohnLakosMetrics(classes);

        DecimalFormat df = new DecimalFormat("0.00");

        assertThat(metrics.getCumulativeComponentDependency()).isEqualTo(14);
        assertThat(df.format(metrics.getAverageComponentDependency())).isEqualTo(Double.toString(2.33));

        assertThat(metrics.getNormalizedCumulativeComponentDependency()).isEqualTo(2);
        df = new DecimalFormat("0.");
        assertThat(df.format(metrics.getRelativeAverageComponentDependency())).isEqualTo(Double.toString(39));
    }


    private static class A {
        private C c;
        private D d;
    }

    private static class B {
        private C c;
        private F f;
    }

    private static class C {
        private E e;
    }

    private static class D {
        private E e;
    }

    private static class E {
    }

    private static class F {
    }
}
