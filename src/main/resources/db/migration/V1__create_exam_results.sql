CREATE TABLE exam_results (
    sbd varchar(8) PRIMARY KEY CHECK (sbd ~ '^[0-9]{8}$'),
    toan numeric CHECK (toan BETWEEN 0 AND 10),
    ngu_van numeric CHECK (ngu_van BETWEEN 0 AND 10),
    ngoai_ngu numeric CHECK (ngoai_ngu BETWEEN 0 AND 10),
    vat_li numeric CHECK (vat_li BETWEEN 0 AND 10),
    hoa_hoc numeric CHECK (hoa_hoc BETWEEN 0 AND 10),
    sinh_hoc numeric CHECK (sinh_hoc BETWEEN 0 AND 10),
    lich_su numeric CHECK (lich_su BETWEEN 0 AND 10),
    dia_li numeric CHECK (dia_li BETWEEN 0 AND 10),
    gdcd numeric CHECK (gdcd BETWEEN 0 AND 10),
    ma_ngoai_ngu varchar(8)
);
