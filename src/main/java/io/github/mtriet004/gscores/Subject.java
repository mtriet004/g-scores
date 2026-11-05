package io.github.mtriet004.gscores;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public enum Subject {
    TOAN("toan", "Toán", "toan", ExamResult::getToan),
    NGU_VAN("ngu_van", "Ngữ văn", "nguVan", ExamResult::getNguVan),
    NGOAI_NGU("ngoai_ngu", "Ngoại ngữ", "ngoaiNgu", ExamResult::getNgoaiNgu),
    VAT_LI("vat_li", "Vật lí", "vatLi", ExamResult::getVatLi),
    HOA_HOC("hoa_hoc", "Hóa học", "hoaHoc", ExamResult::getHoaHoc),
    SINH_HOC("sinh_hoc", "Sinh học", "sinhHoc", ExamResult::getSinhHoc),
    LICH_SU("lich_su", "Lịch sử", "lichSu", ExamResult::getLichSu),
    DIA_LI("dia_li", "Địa lí", "diaLi", ExamResult::getDiaLi),
    GDCD("gdcd", "Giáo dục công dân", "gdcd", ExamResult::getGdcd);

    private final String code;
    private final String label;
    private final String attribute;
    private final Function<ExamResult, BigDecimal> scoreReader;

    Subject(String code, String label, String attribute, Function<ExamResult, BigDecimal> scoreReader) {
        this.code = code;
        this.label = label;
        this.attribute = attribute;
        this.scoreReader = scoreReader;
    }

    public String getCode() { return code; }

    public String getLabel() { return label; }

    String attribute() { return attribute; }

    public BigDecimal scoreOf(ExamResult result) { return scoreReader.apply(result); }

    static Optional<Subject> fromCode(String code) {
        return Arrays.stream(values()).filter(subject -> subject.code.equals(code)).findFirst();
    }
}
