package com.vei.ems.service;

/** Salary formulas. Plain Java with no Spring, so it is easy to unit test. */
public final class PayCalculator {

    public static final double HRA_RATE = 0.20;
    public static final double DA_RATE = 0.10;
    public static final double PF_RATE = 0.12;

    private PayCalculator() { }

    public static double hra(double basic) { return round(basic * HRA_RATE); }

    public static double da(double basic) { return round(basic * DA_RATE); }

    public static double gross(double basic) { return round(basic + hra(basic) + da(basic)); }

    public static double pf(double basic) { return round(basic * PF_RATE); }

    /** Full-time: basic + HRA + DA - PF. */
    public static double netFullTime(double basic) { return round(gross(basic) - pf(basic)); }

    /** Part-time: hourly rate x hours worked. */
    public static double netPartTime(double hourlyRate, int hours) { return round(hourlyRate * hours); }

    public static double round(double value) { return Math.round(value * 100.0) / 100.0; }
}
