package com.example.oikonomia;

import java.io.Serializable;

public class SearchFilter implements Serializable {
    private String startDate;
    private String endDate;
    private boolean showIncome;
    private boolean showExpense;

    public SearchFilter(String startDate, String endDate, boolean showIncome, boolean showExpense) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.showIncome = showIncome;
        this.showExpense = showExpense;
    }

    public boolean isValid() {
        if (!DateUtil.isValidIsoDate(startDate) || !DateUtil.isValidIsoDate(endDate)) {
            return false;
        }
        return (showIncome || showExpense) && startDate.length() != 0 && endDate.length() != 0;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isShowIncome() {
        return showIncome;
    }

    public void setShowIncome(boolean showIncome) {
        this.showIncome = showIncome;
    }

    public boolean isShowExpense() {
        return showExpense;
    }

    public void setShowExpense(boolean showExpense) {
        this.showExpense = showExpense;
    }

    public boolean isBothIncomeAndExpenseSelected() {
        return showIncome && showExpense;
    }

    public String generateQueryStringForBothIncomeAndExpense() {
        String selectQuery = "SELECT * FROM " + DBConnector.TRANSACTIONS_TABLE +
                " WHERE " + DBConnector.KEY_DATE +
                " BETWEEN '" + startDate + "' AND '" + endDate + "'" +
                " ORDER BY " + DBConnector.KEY_DATE + " DESC";
        return selectQuery;
    }

    public String generateQueryString() {
        if (showIncome && !showExpense) {
            String selectQuery = "SELECT * FROM " + DBConnector.TRANSACTIONS_TABLE +
                    " WHERE " + DBConnector.KEY_IS_INCOME + " = 1" +
                    " AND " + DBConnector.KEY_DATE + " BETWEEN '" + startDate + "' AND '" + endDate + "'" +
                    " ORDER BY " + DBConnector.KEY_DATE + " DESC";
            return selectQuery;
        }
        if (!showIncome && showExpense) {
            String selectQuery = "SELECT * FROM " + DBConnector.TRANSACTIONS_TABLE +
                    " WHERE " + DBConnector.KEY_IS_INCOME + " = 0" +
                    " AND " + DBConnector.KEY_DATE + " BETWEEN '" + startDate + "' AND '" + endDate + "'" +
                    " ORDER BY " + DBConnector.KEY_DATE + " DESC";
            return selectQuery;
        }
        return generateQueryStringForBothIncomeAndExpense();
    }
}
