package urfu.cake.shop.user.profile.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Domain-level metrics for the user profile service.
 *
 * <p>All meters are exported through the Prometheus registry and are visible at
 * {@code /actuator/prometheus}. Out-of-the-box HTTP/JVM/system metrics are
 * provided automatically by Spring Boot Actuator + Micrometer.
 */
@Component
public class UserProfileMetrics {

    private static final String OPERATION_METRIC = "user_profile.operation";
    private static final String AUTH_FAILURE_METRIC = "user_profile.auth.failures";
    private static final String TAG_OPERATION = "operation";
    private static final String TAG_OUTCOME = "outcome";
    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_FAILURE = "failure";

    private final MeterRegistry registry;

    public UserProfileMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Times a business operation and records its outcome. The resulting timer
     * exposes both the invocation count and the latency, tagged by operation and
     * outcome (success/failure).
     *
     * @param operation logical operation name (create/get/update/delete)
     * @param action    the work to execute
     * @param <T>       return type
     * @return the value produced by {@code action}
     */
    public <T> T record(String operation, Supplier<T> action) {
        Timer.Sample sample = Timer.start(registry);
        String outcome = OUTCOME_SUCCESS;
        try {
            return action.get();
        } catch (RuntimeException ex) {
            outcome = OUTCOME_FAILURE;
            throw ex;
        } finally {
            sample.stop(Timer.builder(OPERATION_METRIC)
                    .tag(TAG_OPERATION, operation)
                    .tag(TAG_OUTCOME, outcome)
                    .register(registry));
        }
    }

    /** Counts a rejected (invalid or expired) authentication attempt. */
    public void recordAuthFailure() {
        registry.counter(AUTH_FAILURE_METRIC).increment();
    }
}
