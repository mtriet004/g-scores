package io.github.mtriet004.gscores;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

interface ExamResultRepository extends Repository<ExamResult, String> {
    Optional<ExamResult> findById(String sbd);

    @Query("""
            select result.sbd as sbd, result.toan as toan, result.vatLi as vatLi,
                   result.hoaHoc as hoaHoc, (result.toan + result.vatLi + result.hoaHoc) as total
            from ExamResult result
            where result.toan is not null and result.vatLi is not null and result.hoaHoc is not null
            order by total desc, result.sbd asc
            """)
    List<RankedResult> rankGroupA(Limit limit);

    interface RankedResult {
        String getSbd();
        BigDecimal getToan();
        BigDecimal getVatLi();
        BigDecimal getHoaHoc();
        BigDecimal getTotal();
    }
}
