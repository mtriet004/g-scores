package io.github.mtriet004.gscores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.datasource.url=${GSCORES_TEST_DB_URL:jdbc:postgresql://localhost:5432/gscores_test}")
@Transactional
class GScoresApplicationTests {
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired ScoreReport reports;
    @Autowired ExamResultRepository results;

    @Test
    void pagesReferenceStylesheetByItsContentVersion() throws Exception {
        String html = mvc.perform(get("/top-10")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var stylesheet = Pattern.compile("href=\"(/app-[a-f0-9]{32}\\.css)\"").matcher(html);
        assertThat(stylesheet.find()).isTrue();
        mvc.perform(get(stylesheet.group(1))).andExpect(status().isOk())
                .andExpect(content().string(containsString(".ranking-table tbody td")));
        String reportHtml = mvc.perform(get("/reports")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var script = Pattern.compile("src=\"(/reports-[a-f0-9]{32}\\.js)\"").matcher(reportHtml);
        assertThat(script.find()).isTrue();
        mvc.perform(get(script.group(1))).andExpect(status().isOk())
                .andExpect(content().string(containsString("new Chart")));
    }

    @Test
    void lookupPreservesRegistrationNumberAndDistinguishesMissingScores() throws Exception {
        jdbc.update("insert into exam_results (sbd, toan) values ('01000001', 0)");

        mvc.perform(get("/").param("sbd", " 01000001 "))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Kết quả của <span>01000001</span>")))
                .andExpect(content().string(containsString("<dd>0</dd>")))
                .andExpect(content().string(containsString("<dd>—</dd>")));
        mvc.perform(get("/").param("sbd", "1000001"))
                .andExpect(content().string(containsString("Số báo danh phải gồm 8 chữ số.")));
        mvc.perform(get("/").param("sbd", "99999999"))
                .andExpect(content().string(containsString("Không tìm thấy số báo danh này.")));
    }

    @Test
    void reportsIncludeExactBoundariesAndIgnoreNulls() throws Exception {
        jdbc.update("""
                insert into exam_results (sbd, toan) values ('01000001', 8), ('01000002', 6),
                ('01000003', 4), ('01000004', 0), ('01000005', null)
                """);
        assertThat(reports.forSubject(Subject.TOAN)).extracting(ScoreReport.Band::count)
                .containsExactly(1L, 1L, 1L, 1L);
        assertThat(reports.forSubject(Subject.HOA_HOC)).extracting(ScoreReport.Band::count)
                .containsExactly(0L, 0L, 0L, 0L);
        mvc.perform(get("/reports")).andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"score-chart\"")));
        mvc.perform(get("/reports").param("subject", "invalid")).andExpect(status().isBadRequest());
    }

    @Test
    void ranksOnlyCompleteGroupAResultsAndBreaksTiesByRegistrationNumber() throws Exception {
        jdbc.update("""
                insert into exam_results (sbd, toan, vat_li, hoa_hoc) values
                ('01000002', 8, 9, 9), ('01000001', 9, 8, 9), ('01000003', 10, 10, null)
                """);
        jdbc.update("""
                insert into exam_results (sbd, toan, vat_li, hoa_hoc)
                select lpad(i::text, 8, '0'), 1, 1, 1 from generate_series(1, 10) as i
                """);

        var ranking = results.rankGroupA(Limit.of(10));
        assertThat(ranking).hasSize(10).extracting(ExamResultRepository.RankedResult::getSbd)
                .startsWith("01000001", "01000002").doesNotContain("01000003");
        assertThat(ranking.getFirst().getTotal()).isEqualByComparingTo("26");
        mvc.perform(get("/top-10")).andExpect(status().isOk())
                .andExpect(content().string(containsString("<td>01000001</td>")));
    }
}
