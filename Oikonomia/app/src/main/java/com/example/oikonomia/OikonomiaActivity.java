package com.example.oikonomia;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class OikonomiaActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    OikonomiaRecord orec = null;
    private EditText etDate = null;
    AutoCompleteTextView actvDescription = null;
    private EditText etAmount = null;
    private DBConnector dbConnector = null;

    private DayMonthYear dmy;

    String[] strArrDescriptions;
    public static final int MAX_INCOME_DESCRIPTIONS_IN_ACTV = 100;
    public static final int MAX_EXPENSE_DESCRIPTIONS_IN_ACTV = 100;

    //Attribution:https://stackoverflow.com/users/7608371/noor-hossain
    //Ref: https://stackoverflow.com/questions/65637610/saving-files-in-android-11-to-external-storagesdk-30
    public static File commonDocumentDirPath(String folderName) {
        File dir = null;
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
//		{
        dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + folderName);
//		}
//		else
//		{
//			dir = new File(Environment.getExternalStorageDirectory() + "/" + folderName);
//		}

        // Make sure the path directory exists.
        if (!dir.exists()) {
            // Make it, if it doesn't exit
            boolean success = dir.mkdirs();
            if (!success) {
                dir = null;
            }
        }
        return dir;
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(OikonomiaActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        dmy.year = year;
                        dmy.month = month + 1;
                        dmy.day = dayOfMonth;
                        String selectedDate = DateUtil.getOikonomiaDate(dayOfMonth, month + 1, year);
                        etDate.setText(selectedDate);
                    }
                }, dmy.year, dmy.month - 1, dmy.day);

        datePickerDialog.show();
    }

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

    AutoCompleteTextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oikonomia);
        dbConnector = new DBConnector(this);
        try {
            int uniqueDescriptionsCount = 0;
            if (DescriptionsTracker.mostlyUsedDescriptions == null) {
                dbConnector.open();
                Cursor c = dbConnector.getAllRowsFromTransactions();
                uniqueDescriptionsCount = DescriptionsTracker.update(c);
                dbConnector.close();
                if (uniqueDescriptionsCount > 0) {
                    DescriptionsTracker.generateMostlyUsedDescriptions(MAX_INCOME_DESCRIPTIONS_IN_ACTV, MAX_EXPENSE_DESCRIPTIONS_IN_ACTV);
                    strArrDescriptions = DescriptionsTracker.mostlyUsedDescriptions;
                } else {
                    strArrDescriptions = DescriptionsTracker.defaultDescriptions;
                }
            } else {
                if (DescriptionsTracker.mostlyUsedDescriptions.length == 0) {
                    strArrDescriptions = DescriptionsTracker.defaultDescriptions;
                } else {
                    strArrDescriptions = DescriptionsTracker.mostlyUsedDescriptions;
                }
            }
        } catch (Exception e) {
            strArrDescriptions = DescriptionsTracker.defaultDescriptions;
        }
        ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, strArrDescriptions);
        actvDescription = (AutoCompleteTextView) findViewById(R.id.txtDesc);
        actvDescription.setThreshold(1);
        actvDescription.setAdapter(actvAdapter);

        etDate = findViewById(R.id.txtDate);
        String defaultDate = DateUtil.currentOikonomiaDate();
        etDate.setText(defaultDate);
        dmy = DateUtil.getDayMonthYearObjectForOikonomiaDate(defaultDate);
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        etAmount = findViewById(R.id.txtAmount);
        findViewById(R.id.btnViewTransactions).requestFocus();
        orec = new OikonomiaRecord();
    }

    private boolean addRecord(OikonomiaRecord orec) {
        boolean result = false;
        long r = -1;
        dbConnector.open();
        r = dbConnector.insertRecordIntoTransactions(orec);
        dbConnector.close();
        return (r >= 0);
    }

    public int importFromCsv(Uri uri) {
        int count = 0;
        try {
            dbConnector.open();
            ContentResolver contentResolver = getBaseContext().getContentResolver();
            InputStream is = contentResolver.openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            OikonomiaRecord orec = new OikonomiaRecord();

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (orec.readCsv(line)) {
                    if (dbConnector.insertRecordIntoTransactions(orec) >= 0) {
                        ++count;
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            dbConnector.close();
            return count;
        }

    }

    private boolean exportDatabaseToCsv(String folderName, String csvFileName) {
        File dir = commonDocumentDirPath(folderName);
        if (dir == null) {
            return false;
        }
        boolean isExportedOK = true;
        //Dir
        dbConnector.open();
        Cursor cursor = dbConnector.getAllRowsFromTransactions();
        File file = new File(dir, csvFileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fout = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fout);
            OikonomiaRecord orec = new OikonomiaRecord();
            if (cursor.moveToFirst()) {
                int isIncomeIndex = cursor.getColumnIndex(DBConnector.KEY_IS_INCOME);
                int amountIndex = cursor.getColumnIndex(DBConnector.KEY_AMOUNT);
                int dateIndex = cursor.getColumnIndex(DBConnector.KEY_DATE);
                int descIndex = cursor.getColumnIndex(DBConnector.KEY_DESC);
                int idIndex = cursor.getColumnIndex(DBConnector.KEY_ID);
                if (amountIndex < 0 || isIncomeIndex < 0
                        || dateIndex < 0 || descIndex < 0 || idIndex < 0
                ) {
                    throw new Exception("illegal index");
                }
                do {
                    orec.rowId = cursor.getString(idIndex);
                    orec.isIncomeInt = cursor.getInt(isIncomeIndex);
                    orec.setIsoDate(cursor.getString(dateIndex));
                    orec.description = cursor.getString(descIndex);
                    orec.amount = cursor.getFloat(amountIndex);
                    orec.writeCSV(osw);
                } while (cursor.moveToNext());
            }
            osw.flush();
            osw.close();
        } catch (Exception e) {
            isExportedOK = false;
            e.printStackTrace();
            Toast.makeText(
                    getBaseContext(),
                    e.toString(),
                    Toast.LENGTH_LONG).show();
        } finally {
            dbConnector.close();
            return isExportedOK;
        }
    }

    public void onClickAddToExpenses(View view) {
        String strOikonomiaDate = etDate.getText().toString();
        String strDesc = actvDescription.getText().toString();
        String strAmount = etAmount.getText().toString();

        //Validate
        if (!isValidRecord(strOikonomiaDate, strDesc, strAmount)) {
            Toast.makeText(getBaseContext(), "Record is not valid.", Toast.LENGTH_LONG).show();
            return;
        }
        orec.isIncomeInt = OikonomiaRecord.EXPENSE;
        orec.setIsoDate(DateUtil.convertToISO8601Date(strOikonomiaDate));
        orec.description = strDesc;
        orec.amount = Float.parseFloat(strAmount);

        if (!addRecord(orec)) {
            Toast.makeText(getBaseContext(), "DB Error on saving data.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), "Saved Expense.", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickAddToIncomes(View view) {
        String strOikonomiaDate = etDate.getText().toString();
        String strDesc = actvDescription.getText().toString();
        String strAmount = etAmount.getText().toString();
        //Validate
        if (!isValidRecord(strOikonomiaDate, strDesc, strAmount)) {
            Toast.makeText(getBaseContext(), "Record is not complete.", Toast.LENGTH_LONG).show();
            return;
        }
        orec.isIncomeInt = OikonomiaRecord.INCOME;
        orec.setIsoDate(DateUtil.convertToISO8601Date(strOikonomiaDate));
        orec.description = strDesc;
        orec.amount = Float.parseFloat(strAmount);

        if (!addRecord(orec)) {
            Toast.makeText(getBaseContext(), "DB Error on saving data", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), "Saved income.", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickViewAllTransactions(View view) {
        Intent intent = new Intent("com.example.TransactionsViewerActivity");
        intent.putExtra("hasSearchFilter", false);
        startActivity(intent);
    }

    public void onClickSearchAllTransactions(View view) {
        Intent intent = new Intent("com.example.TransactionsSearchActivity");
        startActivity(intent);
    }

    public void onClickExportAllTransactions(View view) {
        String csvFileName = "InExData_" + DateUtil.getTimeStamp() + ".txt";
        String folderName = "Oikonomia2";
        if (exportDatabaseToCsv(folderName, csvFileName)) {
            Toast.makeText(getBaseContext(),
                    "Exported to /" + folderName,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(),
                    "Cannot export to /" + folderName,
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onClickDeleteTransactions(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Make sure to export \nthe database first.\nDo you wish \nto continue?");
        builder.setTitle("Erasing Transactions");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbConnector.open();
                dbConnector.eraseTransactions();
                dbConnector.close();
                Toast.makeText(getBaseContext(), "Transactions table is cleared.", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("No", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onClickImportTransactions(View view) {
        //Invoke File chooser and get the uri of the file in onActivityResult
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (intent == null) {
                return;
            }
            Uri uri = intent.getData();
            int count = 0;
            if (uri != null) {
                count = importFromCsv(uri);
            }
            if (count == 0) {
                Toast.makeText(
                        getBaseContext(),
                        "No records imported.",
                        Toast.LENGTH_LONG).show();
            } else {
                if (count == 1) {
                    Toast.makeText(
                            getBaseContext(),
                            "1 record imported.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(
                            getBaseContext(),
                            count + " records imported.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
