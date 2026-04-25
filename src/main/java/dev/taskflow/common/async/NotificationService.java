package dev.taskflow.common.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    @Async("taskExecutor")
    public void notifyTaskAssigned(UUID taskId, String taskTitle, String assigneeEmail){
        try{
            log.info("[ASYNC] Sending assignment notification - task: '{}' ({}), to: {}", taskTitle, taskId, assigneeEmail);

            Thread.sleep(200); //Simulating slow I/O call instead of email service

            log.info("[ASYNC] Notification sent successfully to {}", assigneeEmail);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[ASYNC] Notification interrupted for task {}", taskId);
        }
    }

    @Async("taskExecutor")
    public void notifyTaskUnassigned(UUID taskId, String taskTitle){
        log.info("[ASYNC] Task '{}' ({}) was unassigned", taskTitle, taskId);
    }

}
