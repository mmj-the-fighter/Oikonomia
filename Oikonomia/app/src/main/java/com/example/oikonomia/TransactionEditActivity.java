package com.example.oikonomia;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class TransactionEditActivity extends Activity {
    final String fformat = "%.02f";
    RadioButton radioIncome = null;
    RadioButton radioExpense = null;
    OikonomiaRecord orec;
    OikonomiaRecord orec2;
    String rowId = "";
    String initialDate;
    String initalDescription;
    String initialAmount;

    boolean isInitiallyIncomeSelected;
    private EditText etDate = null;
    private AutoCompleteTextView actvDescription = null;
    private EditText etAmount = null;
    private boolean isDataModified = false;
    private DBConnector dbConnector = null;
    private DayMonthYear dmy;
    private DayMonthYear initialDmy;

    private boolean isValidRecord(String date, String desc, String amount) {
        if (date.length() == 0 ||
                desc.length() == 0 ||
                amount.length() == 0) {
            return false;
        }
        if (!DateUtil.isValidOikonomiaDate(date)) {
            return false;
        }
        return amount.indexOf('-') < 0;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(TransactionEditActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dmy.year = year;
                        dmy.month = month+1;
                        dmy.day = dayOfMonth;
                        String selectedDate = DateUtil.getOikonomiaDate(dayOfMonth,month+1,year);
                        etDate.setText(selectedDate);
                    }
                }, dmy.year, dmy.month-1, dmy.day);

        datePickerDialog.show();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);
        etDate = findViewById(R.id.txtDate);
        actvDescription = findViewById(R.id.txtDesc);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,DescriptionsTracker.mostlyUsedDescriptions);
        actvDescription = (AutoCompleteTextView)findViewById(R.id.txtDesc);
        actvDescription.setThreshold(1);
        actvDescription.setAdapter(adapter);

        etAmount = findViewById(R.id.txtAmount);
        radioIncome = findViewById(R.id.radioIncome);
        radioExpense = findViewById(R.id.radioExpense);
        Intent intent = getIntent();
        rowId = intent.getStringExtra("rowId");
        orec = new OikonomiaRecord();
        dbConnector = new DBConnector(this);
        dbConnector.open();
        Cursor myCursor = dbConnector.getRowFromTransactions(rowId);
        if (myCursor != null) {
            int index = myCursor.getColumnIndex("isincome");
            if (index > 0) {
                orec.isIncomeInt = myCursor.getInt(index);
            }
            index = myCursor.getColumnIndex("date");
            if (index > 0) {
                orec.setIsoDate(myCursor.getString(index));
            }
            index = myCursor.getColumnIndex("description");
            if (index > 0) {
                orec.description = myCursor.getString(index);
            }
            index = myCursor.getColumnIndex("amount");
            if (index > 0) {
                orec.amount = myCursor.getFloat(index);
            }
        }
        dbConnector.close();
        String formattedAmount = String.format(fformat, orec.amount);
        etAmount.setText(formattedAmount);
        initialAmount = formattedAmount;
        dmy = DateUtil.getDayMonthYearObjectForIsoDate(orec.getIsoDate());
        initialDmy = new DayMonthYear(dmy);
        initialDate = DateUtil.convertToOikonomiaDate(orec.getIsoDate());
        etDate.setText(initialDate);
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        actvDescription.setText(orec.description);
        initalDescription = orec.description;
        if (orec.isIncomeInt == OikonomiaRecord.INCOME) {
            radioIncome.setChecked(true);
            radioExpense.setChecked(false);
            isInitiallyIncomeSelected = true;
        } else {
            radioIncome.setChecked(false);
            radioExpense.setChecked(true);
            isInitiallyIncomeSelected = false;
        }
        orec2 = new OikonomiaRecord();
    }

    public void onClickUpdateTransaction(View view) {
        String strDate = etDate.getText().toString();
        String strDesc = actvDescription.getText().toString();
        String strAmount = etAmount.getText().toString();

        if (!isValidRecord(strDate, strDesc, strAmount)) {
            Toast.makeText(getBaseContext(), "Record is not valid.", Toast.LENGTH_LONG).show();
            return;
        }

        if (radioExpense.isChecked()) {
            orec2.isIncomeInt = OikonomiaRecord.EXPENSE;
        } else {
            orec2.isIncomeInt = OikonomiaRecord.INCOME;
        }
        orec2.setIsoDate(DateUtil.convertToISO8601Date(strDate));
        orec2.description = strDesc;
        orec2.amount = Float.parseFloat(strAmount);
        orec2.rowId = rowId;
        isDataModified = !orec2.isIdentical(orec);
        Intent intent = new Intent();
        int numberOfRowsAffected = 0;
        if (isDataModified) {
            dbConnector.open();
            numberOfRowsAffected = dbConnector.updateRecordInTransactions(orec2);
            dbConnector.close();
            if (numberOfRowsAffected > 0) {
                intent.putExtra("isIncomeInt", orec2.isIncomeInt);//Mark
                intent.putExtra("amount", orec2.amount);
                intent.putExtra("date", orec2.getIsoDate());
                setResult(Activity.RESULT_OK, intent);
            } else {
                setResult(Activity.RESULT_CANCELED, intent);
            }
        } else {
            setResult(Activity.RESULT_CANCELED, intent);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    public void onClickUndoEdit(View view) {
        if (isInitiallyIncomeSelected) {
            radioIncome.setChecked(true);
            radioExpense.setChecked(false);
        } else {
            radioIncome.setChecked(false);
            radioExpense.setChecked(true);
        }
        etDate.setText(initialDate);
        actvDescription.setText(initalDescription);
        etAmount.setText(initialAmount);
        dmy.copy(initialDmy);
    }
}
