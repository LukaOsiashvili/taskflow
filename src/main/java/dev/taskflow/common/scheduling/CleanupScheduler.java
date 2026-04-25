package dev.taskflow.common.scheduling;

import dev.taskflow.attachment.AttachmentRepository;
import dev.taskflow.comment.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final AttachmentRepository attachmentRepository;
    private final CommentRepository commentRepository;

    //Every day at 2:00AM
    //Format: second minute hour dayOfMonth month dayOfWeek
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void dailyCleanupReport(){
        long attachmentCount = attachmentRepository.count();
        long commentCount = commentRepository.count();

        log.info("[SCHEDULER] Daily report - attachment: {}, comments: {}", attachmentCount, commentCount);
    }

    //Health log every 30 minutes
    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 60 * 1000)
    public void periodicHealthLog(){
        log.debug("[SCHEDULER] Taskflow scheduler heartbeat - system running normally");
    }
}
