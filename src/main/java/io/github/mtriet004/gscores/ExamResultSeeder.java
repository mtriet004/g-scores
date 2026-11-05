package io.github.mtriet004.gscores;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.EntityManager;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("seed")
public class ExamResultSeeder implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ExamResultSeeder.class);
    private static final int BATCH_SIZE = 500;
    private static final String SBD = "sbd";
    private static final String LANGUAGE_CODE = "ma_ngoai_ngu";
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader().setSkipHeaderRecord(true).setTrim(true).get();

    private final EntityManager entityManager;
    private final String csvPath;

    ExamResultSeeder(EntityManager entityManager, @Value("${GSCORES_CSV:}") String csvPath) {
        this.entityManager = entityManager;
        this.csvPath = csvPath;
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public void run(ApplicationArguments args) throws IOException {
        entityManager.createNativeQuery("LOCK TABLE exam_results IN SHARE ROW EXCLUSIVE MODE").executeUpdate();
        if (!entityManager.createQuery("select result.sbd from ExamResult result", String.class)
                .setMaxResults(1).getResultList().isEmpty()) {
            LOG.info("Exam results already seeded. Skipping CSV import.");
            return;
        }
        if (csvPath.isBlank()) {
            throw new IllegalArgumentException("Set GSCORES_CSV to the path of the exam results CSV.");
        }

        int count = 0;
        try (var reader = Files.newBufferedReader(Path.of(csvPath));
                var parser = FORMAT.parse(reader)) {
            Set<String> expectedHeaders = new HashSet<>(Set.of(SBD, LANGUAGE_CODE));
            for (Subject subject : Subject.values()) {
                expectedHeaders.add(subject.getCode());
            }
            if (!expectedHeaders.equals(parser.getHeaderMap().keySet())
                    || parser.getHeaderNames().size() != expectedHeaders.size()) {
                throw new IllegalArgumentException("CSV header must contain exactly " + expectedHeaders);
            }

            for (CSVRecord record : parser) {
                entityManager.persist(parse(record));
                count++;
                if (count % BATCH_SIZE == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }
        if (count == 0) {
            throw new IllegalArgumentException("CSV contains no exam results.");
        }
        entityManager.flush();
        LOG.info("Imported {} exam results.", count);
    }

    private static ExamResult parse(CSVRecord record) {
        try {
            if (!record.isConsistent() || !ExamResult.isValidSbd(record.get(SBD))) {
                throw new IllegalArgumentException("Expected 11 fields and an eight-digit registration number.");
            }
            var scores = new EnumMap<Subject, BigDecimal>(Subject.class);
            for (Subject subject : Subject.values()) {
                String value = record.get(subject.getCode());
                if (!value.isEmpty()) {
                    BigDecimal score = new BigDecimal(value);
                    if (score.signum() < 0 || score.compareTo(BigDecimal.TEN) > 0) {
                        throw new IllegalArgumentException("Score outside [0, 10] for " + subject.getCode());
                    }
                    scores.put(subject, score);
                }
            }
            String code = record.get(LANGUAGE_CODE);
            if (code.length() > 8) {
                throw new IllegalArgumentException("Foreign-language code is longer than eight characters.");
            }
            return new ExamResult(record.get(SBD), scores, code.isEmpty() ? null : code);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("CSV record " + record.getRecordNumber() + ": "
                    + exception.getMessage(), exception);
        }
    }
}
