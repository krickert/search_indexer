package com.krickert.search.wiki.dump.file.component;

import com.google.common.collect.Maps;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Singleton
public class WikiDumpFileCounter {
    private static final Logger log = LoggerFactory.getLogger(WikiDumpFileCounter.class);

    AtomicInteger numberOfFilesCompleted = new AtomicInteger(0);
    AtomicInteger numberOfDocumentsExtracted = new AtomicInteger(0);
    AtomicInteger numberOfDocumentsSkipped= new AtomicInteger(0);

    Map<String, Long> wikiFileBeginTimes = Maps.newConcurrentMap();
    Map<String, Long> wikiFileEndTimes = Maps.newConcurrentMap();


    public Integer incrementAndGetNumberOfFilesCompleted() {
        return numberOfFilesCompleted.incrementAndGet();
    }

    public void recordFileBegin(String fullFilePath) {
        wikiFileBeginTimes.put(fullFilePath, System.currentTimeMillis());
        log.info("Begin parsing file: {}", fullFilePath);
    }

    public void recordFileEnd(String fullFilePath) {
        wikiFileEndTimes.put(fullFilePath, System.currentTimeMillis());
        log.info("End parsing file: {}", fullFilePath);
    }

    public void incrementDocumentCount() {
        numberOfDocumentsExtracted.incrementAndGet();
    }

    public void incrementDocumentSkip() {
        numberOfDocumentsSkipped.incrementAndGet();
    }

    public Object getElapsedTime(String fullFilePath) {
        Long beginTime = wikiFileBeginTimes.get(fullFilePath);
        Long endTime = wikiFileEndTimes.get(fullFilePath);
        if (notNull(beginTime) && notNull(endTime)) {
            return endTime - beginTime;
        } else {
            log.error("Time is mysterious.");
            return -1;
        }
    }
    private static <T> boolean notNull(T obj) {
        return obj != null;
    }

    public static class WikiDumpFileCounterTimer
            extends TimerTask {
        final WikiDumpFileCounter counter;
        WikiDumpFileCounterTimer(WikiDumpFileCounter counter) {
            this.counter = counter;
        }
        @Override
        public void run() {
            log.info("********** PROGRESS CHECK *************\n" +
                            "Counters:\n" +
                            "\tNumber of Files completed: {}\n" +
                            "\tNumber of Documents Extracted: {}\n" +
                            "\tNumber of Documents Skipped: {}\n" +
                            "\tNumber of Files completed: {}\n" +
                            "Files in progress: {}\n" +
                            "Files completed: {}\n",
                    counter.numberOfFilesCompleted.get(),
                    counter.numberOfDocumentsExtracted.get(),
                    counter.numberOfDocumentsSkipped.get(),
                    counter.numberOfFilesCompleted.get(),
                    printFilesInProgress(),
                    printFilesCompleted());
        }

        private String printFilesCompleted() {
            return String.join(",", new ArrayList<>(counter.wikiFileEndTimes.keySet()));
        }


        public String printFilesInProgress() {
            return String.join(",", intersectKeys(counter.wikiFileBeginTimes, counter.wikiFileEndTimes));

        }
        public List<String> intersectKeys(Map<String, ?> map1, Map<String, ?> map2) {
            return map1.keySet().stream()
                    .filter(map2::containsKey)
                    .collect(Collectors.toList());
        }
    }
}
