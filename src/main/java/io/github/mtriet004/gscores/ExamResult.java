package io.github.mtriet004.gscores;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "exam_results")
public class ExamResult {
    private static final Pattern REGISTRATION_NUMBER = Pattern.compile("[0-9]{8}");

    @Id
    @Column(length = 8)
    private String sbd;
    private BigDecimal toan;
    private BigDecimal nguVan;
    private BigDecimal ngoaiNgu;
    private BigDecimal vatLi;
    private BigDecimal hoaHoc;
    private BigDecimal sinhHoc;
    private BigDecimal lichSu;
    private BigDecimal diaLi;
    private BigDecimal gdcd;
    @Column(length = 8)
    private String maNgoaiNgu;

    protected ExamResult() {
    }

    ExamResult(String sbd, Map<Subject, BigDecimal> scores, String maNgoaiNgu) {
        this.sbd = sbd;
        this.toan = scores.get(Subject.TOAN);
        this.nguVan = scores.get(Subject.NGU_VAN);
        this.ngoaiNgu = scores.get(Subject.NGOAI_NGU);
        this.vatLi = scores.get(Subject.VAT_LI);
        this.hoaHoc = scores.get(Subject.HOA_HOC);
        this.sinhHoc = scores.get(Subject.SINH_HOC);
        this.lichSu = scores.get(Subject.LICH_SU);
        this.diaLi = scores.get(Subject.DIA_LI);
        this.gdcd = scores.get(Subject.GDCD);
        this.maNgoaiNgu = maNgoaiNgu;
    }

    static boolean isValidSbd(String sbd) { return REGISTRATION_NUMBER.matcher(sbd).matches(); }

    public String getSbd() { return sbd; }

    public BigDecimal getToan() { return toan; }

    public BigDecimal getNguVan() { return nguVan; }

    public BigDecimal getNgoaiNgu() { return ngoaiNgu; }

    public BigDecimal getVatLi() { return vatLi; }

    public BigDecimal getHoaHoc() { return hoaHoc; }

    public BigDecimal getSinhHoc() { return sinhHoc; }

    public BigDecimal getLichSu() { return lichSu; }

    public BigDecimal getDiaLi() { return diaLi; }

    public BigDecimal getGdcd() { return gdcd; }

    public String getMaNgoaiNgu() { return maNgoaiNgu; }
}
