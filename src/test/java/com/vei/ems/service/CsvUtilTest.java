package com.vei.ems.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvUtilTest {

    @Test
    void plainValueIsUnchanged() {
        assertEquals("Arun Kumar", CsvUtil.cell("Arun Kumar"));
    }

    @Test
    void nullBecomesEmpty() {
        assertEquals("", CsvUtil.cell(null));
    }

    @Test
    void commaMakesTheCellQuoted() {
        assertEquals("\"Kumar, Arun\"", CsvUtil.cell("Kumar, Arun"));
    }

    @Test
    void quotesAreDoubled() {
        assertEquals("\"say \"\"hi\"\"\"", CsvUtil.cell("say \"hi\""));
    }

    @Test
    void formulaStartIsNeutralised() {
        assertEquals("'=SUM(A1)", CsvUtil.cell("=SUM(A1)"));
        assertEquals("'+91", CsvUtil.cell("+91"));
        assertEquals("'@evil", CsvUtil.cell("@evil"));
    }

    @Test
    void rowJoinsCellsWithCommas() {
        assertEquals("EMP001,Arun,59000.00", CsvUtil.row("EMP001", "Arun", "59000.00"));
        assertEquals("a,\"b,c\",5", CsvUtil.row("a", "b,c", 5));
    }
}
