package com.example.oikonomia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class TransactionsViewerActivity extends Activity {

    private static final int TRANSACTION_EDIT_ACTIVITY_REQUEST_CODE = 1;
    DBConnector dbConnector = null;
    Cursor cursor;
    TransactionsCursorAdapter adapter;
    ListView listView;
    boolean searchFilterAvailable = false;
    SearchFilter searchFilter;
    String currentSearchQuery;
    String incomeAndExpenseSearchQuery;
    BalanceSheet balanceSheet;

    EditText etBalanceSheet;

    public void updateBalanceSheetGUI() {
        etBalanceSheet.setEnabled(true);
        etBalanceSheet.setText(balanceSheet.prepareReport());
        etBalanceSheet.setEnabled(false);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_viewer);
        listView = findViewById(R.id.txnListView);
        balanceSheet = new BalanceSheet();
        etBalanceSheet = findViewById(R.id.txtBalanceSheet);
        etBalanceSheet.setText("");
        etBalanceSheet.setEnabled(false);
        Intent intent = getIntent();
        if (intent.getExtras().getBoolean("hasSearchFilter")) {
            searchFilterAvailable = true;
            searchFilter = (SearchFilter) intent.getSerializableExtra("searchFilter");
            currentSearchQuery = searchFilter.generateQueryString();
            if (!searchFilter.isBothIncomeAndExpenseSelected()) {
                incomeAndExpenseSearchQuery = searchFilter.generateQueryStringForBothIncomeAndExpense();
            }
            balanceSheet.isAllTimeData = false;
            balanceSheet.searchFilterStartDate = searchFilter.getStartDate();
            balanceSheet.searchFilterEndDate = searchFilter.getEndDate();
            //Toast.makeText(getBaseContext(),"got search filter",Toast.LENGTH_LONG).show();
        } else {
            searchFilterAvailable = false;
            balanceSheet.isAllTimeData = true;
        }

        dbConnector = new DBConnector(this);
        dbConnector.open();
        if (searchFilterAvailable) {
            cursor = dbConnector.query(currentSearchQuery);
            if (searchFilter.isBothIncomeAndExpenseSelected()) {
                balanceSheet.calculateBalanceSheet(cursor);
            }
        } else {
            cursor = dbConnector.getAllRowsFromTransactions();
            balanceSheet.calculateBalanceSheet(cursor);
        }
        adapter = new TransactionsCursorAdapter(
                this, R.layout.transactions_list_view_row, cursor, 0);
        listView.setAdapter(adapter);
        dbConnector.close();

        dbConnector.open();
        if (searchFilterAvailable && !searchFilter.isBothIncomeAndExpenseSelected()) {
            Cursor otherCursor = dbConnector.query(incomeAndExpenseSearchQuery);
            balanceSheet.calculateBalanceSheet(otherCursor);
        }
        dbConnector.close();
        updateBalanceSheetGUI();
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor myCursor = (Cursor) parent.getItemAtPosition(position);
                int index = myCursor.getColumnIndex("_id");
                if (index < 0) {
                    Toast.makeText(getBaseContext(), "Cannot delete; code 1", Toast.LENGTH_LONG).show();
                    return true;
                }
                String rowId = myCursor.getString(index);
                int indexIncome = myCursor.getColumnIndex(DBConnector.KEY_IS_INCOME);
                int indexAmount = myCursor.getColumnIndex(DBConnector.KEY_AMOUNT);
                if (indexIncome < 0 || indexAmount < 0) {
                    Toast.makeText(getBaseContext(), "Cannot delete; code 2", Toast.LENGTH_LONG).show();
                    return true;
                }
                int isIncomeInt = myCursor.getInt(indexIncome);
                float amount = myCursor.getFloat(indexAmount);
                AlertDialog.Builder builder = new AlertDialog.Builder(TransactionsViewerActivity.this);
                builder.setMessage("Do you want to delete this transaction?");
                builder.setTitle("Alert !");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbConnector.open();
                        boolean r = dbConnector.deleteRow(rowId);
                        if (r) {
                            Toast.makeText(getBaseContext(), "Deleted " + rowId, Toast.LENGTH_LONG).show();
                            float deltaIncome = 0;
                            float deltaExpense = 0;
                            if (isIncomeInt == 0) {
                                deltaExpense = -amount;
                            } else {
                                deltaIncome = -amount;
                            }
                            balanceSheet.updateBalanceSheet(deltaIncome, deltaExpense);
                            Cursor newCursor;
                            if (searchFilterAvailable) {
                                newCursor = dbConnector.query(currentSearchQuery);
                            } else {
                                newCursor = dbConnector.getAllRowsFromTransactions();
                            }
                            adapter.changeCursor(newCursor);
                            adapter.notifyDataSetChanged();
                            updateBalanceSheetGUI();
                        } else {
                            Toast.makeText(getBaseContext(), "Not deleted " + rowId, Toast.LENGTH_LONG).show();
                        }
                        dbConnector.close();
                    }
                });
                builder.setNegativeButton("No", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true; // true: event has been consumed
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor myCursor = (Cursor) parent.getItemAtPosition(position);
                int index = myCursor.getColumnIndex("_id");
                if (index < 0) {
                    Toast.makeText(getBaseContext(), "Cannot update code 1", Toast.LENGTH_LONG).show();
                    return;
                }
                int indexIncome = myCursor.getColumnIndex(DBConnector.KEY_IS_INCOME);
                int indexAmount = myCursor.getColumnIndex(DBConnector.KEY_AMOUNT);
                if (indexIncome < 0 || indexAmount < 0) {
                    Toast.makeText(getBaseContext(), "Cannot update code 2", Toast.LENGTH_LONG).show();
                    return;
                }
                int indexDate = myCursor.getColumnIndex(DBConnector.KEY_DATE);
                if (indexDate < 0) {
                    Toast.makeText(getBaseContext(), "Cannot update code 3", Toast.LENGTH_LONG).show();
                    return;
                }
                int isIncomeInt = myCursor.getInt(indexIncome);
                float amount = myCursor.getFloat(indexAmount);
                String date = myCursor.getString(indexDate);

                balanceSheet.saveBeforeEdit(isIncomeInt, amount, date);
                String rowId = myCursor.getString(index);
                Intent intent = new Intent("com.example.TransactionEditActivity");
                intent.putExtra("rowId", rowId);
                startActivityForResult(intent, TransactionsViewerActivity.TRANSACTION_EDIT_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == TRANSACTION_EDIT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                int isIncomeInt = intent.getIntExtra("isIncomeInt", OikonomiaRecord.EXPENSE);
                float amount = intent.getFloatExtra("amount", 0);
                String date = intent.getStringExtra("date");

                balanceSheet.saveAfterEdit(isIncomeInt, amount, date);
                dbConnector.open();
                Cursor newCursor;
                if (searchFilterAvailable) {
                    newCursor = dbConnector.query(currentSearchQuery);
                } else {
                    balanceSheet.extendTimePeriod(date);//compare new date and adjust date in balance sheet
                    newCursor = dbConnector.getAllRowsFromTransactions();
                }
                adapter.changeCursor(newCursor);
                adapter.notifyDataSetChanged();
                dbConnector.close();
                balanceSheet.updateBalanceSheetAfterEdit();
                updateBalanceSheetGUI();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getBaseContext(), "No data changed.", Toast.LENGTH_LONG).show();
            }
        }
    }


}
