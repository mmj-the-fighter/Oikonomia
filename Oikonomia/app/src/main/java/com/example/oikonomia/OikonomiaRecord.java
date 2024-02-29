package com.example.oikonomia;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class OikonomiaRecord {
    public final static int INCOME = 1;
    public final static int EXPENSE = 0;
    private static final String comma = ",";
    private static final String newLine = "\n";
    private static final String incomeString = "1";
    private static final String expenseString = "0";
    private static final int[] index = new int[3];
    public String rowId;
    public int isIncomeInt;
    public String description;
    public float amount;
    private String isoDate;

    public boolean isIdentical(OikonomiaRecord other) {
        return isIncomeInt == other.isIncomeInt &&
                isoDate == other.isoDate &&
                description == other.description &&
                amount == other.amount;
    }

    public String getIsoDate() {
        return isoDate;
    }

    public void setIsoDate(String dt) {
        isoDate = dt;
    }

    public void writeCSV(OutputStreamWriter osw) throws IOException {
        //1
        if (isIncomeInt == 0) {
            osw.write(expenseString);
        } else {
            osw.write(incomeString);
        }
        osw.write(comma);

        //2
        osw.write(isoDate);
        osw.write(comma);

        //3
        osw.write(description);
        osw.write(comma);

        //4
        osw.write(Float.toString(amount));
        osw.write(newLine);
    }

    public boolean readCsv(String line) {
        int k = 0;
        int length = line.length();
        int start = 0;
        for (int i = 0; i < length; i++) {
            if (line.charAt(i) == ',') {
                if (k < 3) {
                    index[k] = i;
                    k++;
                } else {
                    return false;
                }
            }
        }
        if (k != 3) {
            return false;
        }
        isIncomeInt = Integer.parseInt(line.substring(start, index[0]));//1
        isoDate = line.substring(index[0] + 1, index[1]);//2
        description = line.substring(index[1] + 1, index[2]);//3
        amount = Float.parseFloat(line.substring(index[2] + 1, length));//4
        return true;
    }
}

