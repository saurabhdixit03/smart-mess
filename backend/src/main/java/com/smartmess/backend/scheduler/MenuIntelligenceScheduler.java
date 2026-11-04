
package com.smartmess.backend.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.service.intelligence.ai.MenuIntelligenceAnalysisService;

@Component
@ConditionalOnProperty(
        name = "spring.ai.model.chat",
        havingValue = "google-genai"
)
public class MenuIntelligenceScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(MenuIntelligenceScheduler.class);

    private static final ZoneId SCHEDULER_ZONE =
            ZoneId.of("Asia/Kolkata");

    private final MessRepository messRepository;
    private final MenuIntelligenceAnalysisService analysisService;

    private final AtomicBoolean analysisRunning =
            new AtomicBoolean(false);

    private volatile boolean applicationReady;

    public MenuIntelligenceScheduler(
            MessRepository messRepository,
            MenuIntelligenceAnalysisService analysisService) {

        this.messRepository = messRepository;
        this.analysisService = analysisService;
    }

    /*
     * Wait until application startup and initialization finish.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        applicationReady = true;
    }

    /*
     * Runs every Saturday at 11 PM by default.
     * The cron expression can be overridden for development testing.
     */
    @Scheduled(
            cron = "${app.menu-intelligence.scheduler.cron:0 0 23 ? * SAT}",
            zone = "${app.menu-intelligence.scheduler.zone:Asia/Kolkata}"
    )
    public void analyzeAllMesses() {

        if (!applicationReady) {
            return;
        }

        /*
         * Prevent overlapping executions within this application instance.
         */
        if (!analysisRunning.compareAndSet(false, true)) {
            log.warn(
                    "Previous menu intelligence cycle is still running. "
                            + "Skipping this trigger."
            );
            return;
        }

        try {
            LocalDate currentPeriodEnd =
                    LocalDate.now(SCHEDULER_ZONE).with(
                            TemporalAdjusters.previousOrSame(
                                    DayOfWeek.SATURDAY
                            )
                    );

            LocalDate currentPeriodStart =
                    currentPeriodEnd.minusDays(6);

            LocalDate previousPeriodEnd =
                    currentPeriodStart.minusDays(1);

            LocalDate previousPeriodStart =
                    previousPeriodEnd.minusDays(6);

            log.info(
                    "Starting menu intelligence analysis. "
                            + "periodStart={}, periodEnd={}",
                    currentPeriodStart,
                    currentPeriodEnd
            );

            List<Mess> messes = messRepository.findAll();

            for (Mess mess : messes) {
                Long messId = mess.getMessId();

                try {
                    boolean successful = analysisService.analyzeMess(
                            messId,
                            currentPeriodStart,
                            currentPeriodEnd,
                            previousPeriodStart,
                            previousPeriodEnd
                    );

                    if (!successful) {
                        log.warn(
                                "Menu intelligence analysis unsuccessful "
                                        + "for messId={}",
                                messId
                        );
                    }

                } catch (RuntimeException exception) {
                    log.error(
                            "Could not process menu intelligence for "
                                    + "messId={}. failureType={}",
                            messId,
                            exception.getClass().getSimpleName()
                    );
                }
            }

            log.info(
                    "Menu intelligence analysis cycle finished. messCount={}",
                    messes.size()
            );

        } catch (RuntimeException exception) {
            log.error(
                    "Menu intelligence scheduler cycle failed. failureType={}",
                    exception.getClass().getSimpleName()
            );

        } finally {
            analysisRunning.set(false);
        }
    }
}
