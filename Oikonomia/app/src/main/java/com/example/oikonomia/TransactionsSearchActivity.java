package com.example.oikonomia;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class TransactionsSearchActivity extends Activity {
    private EditText etStartDate;
    private EditText etEndDate;
    private CheckBox cbShowIncome;
    private CheckBox cbShowExpense;

    private DayMonthYear dmyStartDate;
    private DayMonthYear dmyEndDate;

    private boolean validateDates(String fromDate, String toDate) {
        return DateUtil.isValidOikonomiaDate(fromDate) &&
                DateUtil.isValidOikonomiaDate(toDate);
    }

    private void showDatePickerDialogForStartDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(TransactionsSearchActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dmyStartDate.year = year;
                        dmyStartDate.month = month+1;
                        dmyStartDate.day = dayOfMonth;
                        String selectedDate = DateUtil.getOikonomiaDate(dayOfMonth,month+1,year);
                        etStartDate.setText(selectedDate);
                    }
                }, dmyStartDate.year, dmyStartDate.month-1, dmyStartDate.day);

        datePickerDialog.show();
    }

    private void showDatePickerDialogForEndDate() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(TransactionsSearchActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dmyEndDate.year = year;
                        dmyEndDate.month = month+1;
                        dmyEndDate.day = dayOfMonth;
                        String selectedDate = DateUtil.getOikonomiaDate(dayOfMonth,month+1,year);
                        etEndDate.setText(selectedDate);
                    }
                }, dmyEndDate.year, dmyEndDate.month-1, dmyEndDate.day);

        datePickerDialog.show();
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_filter);
        etStartDate = findViewById(R.id.txtStartDate);
        etEndDate = findViewById(R.id.txtEndDate);
        String defaultDate = DateUtil.currentOikonomiaDate();
        dmyStartDate = DateUtil.getDayMonthYearObjectForOikonomiaDate(defaultDate);
        dmyEndDate = new DayMonthYear(dmyStartDate);
        etStartDate.setText(defaultDate);
        etEndDate.setText(defaultDate);
        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialogForStartDate();
            }
        });
        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialogForEndDate();
            }
        });
        cbShowIncome = findViewById(R.id.checkBoxIncome);
        cbShowExpense = findViewById(R.id.checkBoxExpense);
    }

    public void onClickSearchTransactions(View view) {
        boolean showIncome = cbShowIncome.isChecked();
        boolean showExpense = cbShowExpense.isChecked();
        String startDate = etStartDate.getText().toString();
        String endDate = etEndDate.getText().toString();
        String startDateIso = DateUtil.convertToISO8601Date(startDate);
        String endDateIso = DateUtil.convertToISO8601Date(endDate);
        if (!validateDates(startDate, endDate)) {
            Toast.makeText(getBaseContext(), "Search is not valid.Code 1", Toast.LENGTH_LONG).show();
            return;
        }
        SearchFilter sf = new SearchFilter(startDateIso, endDateIso, showIncome, showExpense);
        if (sf.isValid()) {
            Intent intent = new Intent("com.example.TransactionsViewerActivity");
            intent.putExtra("hasSearchFilter", true);
            intent.putExtra("searchFilter", sf);
            startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), "Search is not vaiid. Code 2", Toast.LENGTH_LONG).show();
        }
    }
}

