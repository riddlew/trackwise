package model;

import java.time.LocalDate;

public class Turnover {
    private int mId;
    private LocalDate mDate;
    private BasicIdName mShift;
    private BasicIdName mDepartment;
    private String mNotes;

    public Turnover() {}

    public Turnover(LocalDate date, BasicIdName shift, BasicIdName department, String notes) {
        this.mDate = date;
        this.mShift = shift;
        this.mDepartment = department;
        this.mNotes = notes;
    }

    public Turnover(int id, LocalDate date, BasicIdName shift, BasicIdName department, String notes) {
        this(date, shift, department, notes);
        this.mId = id;
    }

    public int getId() { return this.mId; }
    public void setId(int id) { this.mId = id; }

    public LocalDate getDate() { return this.mDate; }
    public void setDate(LocalDate date) { this.mDate = date; }

    public BasicIdName getShift() { return this.mShift; }
    public void setShift(BasicIdName shift) { this.mShift = shift; }

    public BasicIdName getDepartment() { return this.mDepartment; }
    public void setDepartment(BasicIdName department) { this.mDepartment = department; }

    public String getNotes() { return this.mNotes; }
    public void setNotes(String notes) { this.mNotes = notes; }
}