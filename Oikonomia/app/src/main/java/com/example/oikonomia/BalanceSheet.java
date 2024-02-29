package com.example.oikonomia;

import android.database.Cursor;

public class BalanceSheet {
    final String fformat = "%.02f";
    public boolean isAllTimeData = true;
    public String searchFilterStartDate = "";
    public String searchFilterEndDate = "";
    private float totalIncome = 0;
    private float totalExpense = 0;
    private float balanceAmount = 0;
    private String dateBeforeEdit;
    private String dateAfterEdit;
    private float amountBeforeEdit;
    private float amountAfterEdit;
    private int recordTypeBeforeEdit;
    private int recordTypeAfterEdit;
    private String startDate = "";
    private String endDate = "";
    private String startDateIso = "";
    private String endDateIso = "";

    public void setStartDate(String date) {
        startDate = DateUtil.convertToOikonomiaDate(date);
        startDateIso = DateUtil.convertToISO8601Date(date);
    }

    public void setEndDate(String date) {
        endDate = DateUtil.convertToOikonomiaDate(date);
        endDateIso = DateUtil.convertToISO8601Date(date);
    }

    public String prepareReport() {
        String formattedTotalIncome = String.format(fformat, totalIncome);
        String formattedTotalExpense = String.format(fformat, totalExpense);
        String formattedBalance = String.format(fformat, balanceAmount);
        if (startDate == "" && endDate == "") {
            String report =
                    "Income: " + formattedTotalIncome + "\n"
                            + "Expense: " + formattedTotalExpense + "\n"
                            + "Balance: " + formattedBalance;
            return report;
        }
        if (isAllTimeData) {
            String report =
                    startDate + " to " + endDate + "\n"
                            + "Income: " + formattedTotalIncome + "\n"
                            + "Expense: " + formattedTotalExpense + "\n"
                            + "Balance: " + formattedBalance;
            return report;
        } else {
            String fromDate = DateUtil.convertToOikonomiaDate(searchFilterStartDate);
            String toDate = DateUtil.convertToOikonomiaDate(searchFilterEndDate);
            String report =
                    fromDate + " to " + toDate + "\n"
                            + "Income: " + formattedTotalIncome + "\n"
                            + "Expense: " + formattedTotalExpense + "\n"
                            + "Balance: " + formattedBalance;
            return report;
        }
    }

    public void saveBeforeEdit(int isIncomeInt, float amount, String date) {
        dateBeforeEdit = date;
        amountBeforeEdit = amount;
        recordTypeBeforeEdit = isIncomeInt;
    }

    public void saveAfterEdit(int isIncomeInt, float amount, String date) {
        dateAfterEdit = date;
        amountAfterEdit = amount;
        recordTypeAfterEdit = isIncomeInt;
    }

    public void extendTimePeriod(String isoDate) {
        int res1 = DateUtil.compareIsoDateStrings(isoDate, startDateIso);
        if (res1 < 0) {
            setStartDate(isoDate);
            return;
        }
        int res2 = DateUtil.compareIsoDateStrings(isoDate, endDateIso);
        if (res2 > 0) {
            setEndDate(isoDate);
        }
    }

    public void updateBalanceSheetAfterEdit() {
        float deltaExpense = 0;
        float deltaIncome = 0;
        boolean isDateChangedOutOfRange = !DateUtil.isDateInRange(dateAfterEdit, startDateIso, endDateIso);
        if (isDateChangedOutOfRange) {
            if (recordTypeBeforeEdit == OikonomiaRecord.INCOME) {
                deltaIncome = -amountBeforeEdit;
            } else {
                deltaExpense = -amountBeforeEdit;
            }
        } else {
            if (recordTypeAfterEdit == recordTypeBeforeEdit) {
                if (recordTypeAfterEdit == OikonomiaRecord.INCOME) {
                    deltaIncome = amountAfterEdit - amountBeforeEdit;
                } else {
                    deltaExpense = amountAfterEdit - amountBeforeEdit;
                }
            } else {
                if (recordTypeBeforeEdit == OikonomiaRecord.INCOME &&
                        recordTypeAfterEdit == OikonomiaRecord.EXPENSE) {
                    //income -> expense: expense should increase, income should decrease
                    deltaIncome = -amountBeforeEdit;
                    deltaExpense = amountAfterEdit;
                } else if (recordTypeBeforeEdit == OikonomiaRecord.EXPENSE &&
                        recordTypeAfterEdit == OikonomiaRecord.INCOME) {
                    //expense -> income: income should increase, expense should decrease
                    deltaIncome = amountAfterEdit;
                    deltaExpense = -amountBeforeEdit;
                }
            }
        }

        updateBalanceSheet(deltaIncome, deltaExpense);
    }

    public void updateBalanceSheet(float deltaIncome, float deltaExpense) {
        totalIncome += deltaIncome;
        totalExpense += deltaExpense;
        balanceAmount = totalIncome - totalExpense;
    }

    public void calculateBalanceSheet(Cursor cursor) {
        int dateIndex = -1;
        if (cursor.moveToFirst()) {
            int isIncomeIndex = cursor.getColumnIndex(DBConnector.KEY_IS_INCOME);
            int amountIndex = cursor.getColumnIndex(DBConnector.KEY_AMOUNT);
            if (amountIndex < 0 || isIncomeIndex < 0) {
                return;
            }
            dateIndex = cursor.getColumnIndex(DBConnector.KEY_DATE);
            if (dateIndex < 0) {
                return;
            }
            setEndDate(cursor.getString(dateIndex));
            do {
                int isIncomeInt = cursor.getInt(isIncomeIndex);
                float amount = cursor.getFloat(amountIndex);
                if (isIncomeInt == 0) {
                    totalExpense += amount;
                } else {
                    totalIncome += amount;
                }
            } while (cursor.moveToNext());
            balanceAmount = totalIncome - totalExpense;
        }
        if (cursor.moveToLast()) {
            setStartDate(cursor.getString(dateIndex));
        }
    }
}
