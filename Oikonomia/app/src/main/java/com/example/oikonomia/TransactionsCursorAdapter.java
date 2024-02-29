package com.example.oikonomia;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class TransactionsCursorAdapter extends ResourceCursorAdapter {
    final String fformat = "%.02f";

    public TransactionsCursorAdapter(Context context, int layout, Cursor cursor, int flags) {
        super(context, layout, cursor, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int index = cursor.getColumnIndex("isincome");
        if (index >= 0) {
            TextView txnType = view.findViewById(R.id.txtTxnType);
            int isIncome = cursor.getInt(index);
            if (isIncome == 1) {
                txnType.setText("Income");
            } else {
                txnType.setText("Expense");
            }
        }
        index = cursor.getColumnIndex("date");
        if (index >= 0) {
            TextView txnDate = view.findViewById(R.id.txtTxnDate);
            String date = DateUtil.convertToOikonomiaDate(cursor.getString(index));
            txnDate.setText(date);
        }
        index = cursor.getColumnIndex("description");
        if (index >= 0) {
            TextView txnDesc = view.findViewById(R.id.txtTxnDesc);
            txnDesc.setText(cursor.getString(index));
        }
        index = cursor.getColumnIndex("amount");
        if (index >= 0) {
            TextView txnAmount = view.findViewById(R.id.txtTxnAmount);
            float amount = cursor.getFloat(index);
            String formattedAmount = String.format(fformat, amount);
            txnAmount.setText(formattedAmount);
        }
    }
}