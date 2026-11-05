package io.github.mtriet004.gscores;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Repository;

@Repository
class ScoreReport {
    private static final BigDecimal EIGHT = BigDecimal.valueOf(8);
    private static final BigDecimal SIX = BigDecimal.valueOf(6);
    private static final BigDecimal FOUR = BigDecimal.valueOf(4);

    private final EntityManager entityManager;

    ScoreReport(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    List<Band> forSubject(Subject subject) {
        var builder = entityManager.getCriteriaBuilder();
        var query = builder.createTupleQuery();
        var score = query.from(ExamResult.class).<BigDecimal>get(subject.attribute());
        query.select(builder.tuple(
                count(builder, builder.ge(score, EIGHT)),
                count(builder, builder.and(builder.ge(score, SIX), builder.lt(score, EIGHT))),
                count(builder, builder.and(builder.ge(score, FOUR), builder.lt(score, SIX))),
                count(builder, builder.lt(score, FOUR))));

        var counts = entityManager.createQuery(query).getSingleResult();
        return List.of(
                new Band("≥ 8", counts.get(0, Long.class)),
                new Band("6–<8", counts.get(1, Long.class)),
                new Band("4–<6", counts.get(2, Long.class)),
                new Band("<4", counts.get(3, Long.class)));
    }

    private static Expression<Long> count(CriteriaBuilder builder, Predicate predicate) {
        return builder.count(builder.<Integer>selectCase().when(predicate, 1).otherwise((Integer) null));
    }

    public record Band(String label, long count) {
    }
}
