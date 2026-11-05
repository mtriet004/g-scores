package io.github.mtriet004.gscores;

import org.springframework.data.domain.Limit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
class ScoreController {
    private final ExamResultRepository results;
    private final ScoreReport report;

    ScoreController(ExamResultRepository results, ScoreReport report) {
        this.results = results;
        this.report = report;
    }

    @ModelAttribute("subjects")
    Subject[] subjects() {
        return Subject.values();
    }

    @GetMapping("/")
    String lookup(@RequestParam(name = "sbd", required = false) String input, Model model) {
        if (input != null) {
            String sbd = input.strip();
            model.addAttribute("sbd", sbd);
            if (!ExamResult.isValidSbd(sbd)) {
                model.addAttribute("message", "Số báo danh phải gồm 8 chữ số.");
            } else {
                results.findById(sbd).ifPresentOrElse(
                        result -> model.addAttribute("result", result),
                        () -> model.addAttribute("message", "Không tìm thấy số báo danh này."));
            }
        }
        return "lookup";
    }

    @GetMapping("/reports")
    String reports(@RequestParam(name = "subject", defaultValue = "toan") String code, Model model) {
        Subject subject = Subject.fromCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Môn học không hợp lệ."));
        model.addAttribute("selectedSubject", subject);
        model.addAttribute("bands", report.forSubject(subject));
        return "reports";
    }

    @GetMapping("/top-10")
    String topTen(Model model) {
        model.addAttribute("ranking", results.rankGroupA(Limit.of(10)));
        return "top-ten";
    }
}
