package dev.taskflow.common.metrics;

import dev.taskflow.task.TaskStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TaskMetrics {

    private final Counter taskCreatedCounter;
    private final MeterRegistry registry;

    public TaskMetrics(MeterRegistry registry){
        this.registry = registry;

        this.taskCreatedCounter = Counter
                .builder("taskflow.tasks.created")
                .description("Total number of tasks created")
                .register(registry);
    }

    public void recordTaskCreated(){
        taskCreatedCounter.increment();
    }

    public void recordStatusTransition(TaskStatus from, TaskStatus to){
        Counter.builder("taskflow.tasks.status.transitions")
                .description("Task status transition count")
                .tag("from", from.name())
                .tag("to", to.name())
                .register(registry)
                .increment();
    }

}
