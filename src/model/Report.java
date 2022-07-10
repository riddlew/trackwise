package model;

import java.time.LocalDate;

public class Report {
    private LocalDate mDate;
    private BasicIdName mShift;
    private int mIssues;

    public Report() {}

    public Report(LocalDate date, BasicIdName shift, int issues) {
        this.mDate = date;
        this.mShift = shift;
        this.mIssues = issues;
    }

    public LocalDate getDate() {
        return mDate;
    }

    public void setDate(LocalDate mDate) {
        this.mDate = mDate;
    }

    public int getIssues() {
        return mIssues;
    }

    public void setIssues(int mIssues) {
        this.mIssues = mIssues;
    }

    public BasicIdName getShift() {
        return mShift;
    }

    public void setShift(BasicIdName mShift) {
        this.mShift = mShift;
    }
}
