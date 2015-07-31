/*
    Description: An easy to use personal expense tracker
	Author: mmj-the-fighter 
    Copyright (C) 2015 mmj-the-fighter

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.
*/
package com.xyzsoftware.oikonomia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class OikonomiaActivity extends Activity {

	//debug
	String tag = "XYZSoftware::";

	//Inputs
	private EditText etDate=null;
	private EditText etItemName=null;
	private EditText etQuantity=null;
	private EditText etAmount=null;
	//Output
	private EditText etSummary=null;
	
	//Record set
	private ArrayList<OikonomiaRecord> alor = null;
	private ArrayList<String> als = null;
	
	//SQlite Database interface
	private DBConnector dbConnector = null;
	
	//For calculating summary
	OikonomiaQuickSummary oqs;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_oikonomia);
		
		//get summary
		oqs = new OikonomiaQuickSummary();
		String prefs = getString(R.string.app_prefs);
		SharedPreferences sp = getSharedPreferences(prefs,Activity.MODE_PRIVATE);
		//oqs.resetVarsAndSaveToPreferences(sp);
		oqs.loadVarsFromSystemPreferences(sp);
		
		
		etDate = (EditText)findViewById(R.id.txtDate);
		etItemName = (EditText)findViewById(R.id.txtItemName);
		etQuantity = (EditText)findViewById(R.id.txtQuantity);
		etAmount = (EditText)findViewById(R.id.txtAmount);
		etSummary = (EditText)findViewById(R.id.txtSummary);
		etSummary.setEnabled(false);
		findViewById(R.id.btnViewExpenses).requestFocus();
        
		alor = new ArrayList<OikonomiaRecord>();
		dbConnector = new DBConnector(this);
		als = new ArrayList<String>();
        
		loadRecords();
		
		etSummary.setEnabled(true);
		etSummary.setText(oqs.prepareSummary());
		etSummary.setEnabled(false);
		
		etDate.setText(currentDate());
		etQuantity.setText(Float.toString(1.0f));
	}
	
	//Load all records from database and add to arraylist.
	private void loadRecords(){
		dbConnector.open();
		Cursor c = dbConnector.getAllRowsFromExpenses();
		if (c.moveToFirst())
		{
			do {
				OikonomiaRecord orec = new OikonomiaRecord();
				orec.date = c.getString(1);
				orec.itemName = c.getString(2);
				orec.quantity = c.getFloat(3);
				orec.amount = c.getFloat(4);
				alor.add(orec);
			}while(c.moveToNext());
		}
		dbConnector.close();
	}
	
	//Add record to database and arraylist, Update the summary too
	private boolean addRecord(OikonomiaRecord orec) {
		boolean result = false;
		long r = -1;
		dbConnector.open();
		r = dbConnector.insertRecordIntoExpenses
				(orec.date, 
				orec.itemName, 
				orec.quantity, 
				orec.amount);
		dbConnector.close();
		if(r >= 0) {
			result = true;
			String date = currentDate();
			String[] dateComponents = date.split("/");
			int today 	= Integer.parseInt( dateComponents[0] );
			int thismonth 	= Integer.parseInt( dateComponents[1] );
			int thisyear 	= Integer.parseInt( dateComponents[2] );
			orec.parseDate();
			if(orec.compareDateForToday(today, thismonth, thisyear)){
				oqs.todaysExpense += orec.amount;
				oqs.thisMonthsExpense += orec.amount;
				oqs.thisYearsExpense += orec.amount;
			}else if(orec.compareDateForThisMonth(today, thismonth, thisyear)){
				oqs.thisMonthsExpense += orec.amount;
				oqs.thisYearsExpense += orec.amount;
			}else if(orec.compareDateForThisYear(today, thismonth, thisyear)){
				oqs.thisYearsExpense += orec.amount;
			}
			oqs.setReferenceDate(today, thismonth, thisyear);
			alor.add(orec);
		} else {
			result = false;
		}
		return result;
	}
	
	private void exportArrayListToCSV() {
		File sdCard = Environment.getExternalStorageDirectory();
		File directory = new File (sdCard.getAbsolutePath() + "/Oikonomia");
		directory.mkdirs();
		String csvFileName = "Oikonomia"+getTimeStamp()+".csv";
		File file = new File(directory,csvFileName);
		try {
			FileOutputStream fout = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fout);
			for(int i=0; i<alor.size(); i++) {
				OikonomiaRecord orec = alor.get(i);
				orec.writeCSV(osw);
			}
			osw.flush();
			osw.close();
			Toast.makeText(
					getBaseContext(),
					"Database is exported to\n /Oikonomia @sdcard",
					Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			e.printStackTrace();
			Toast.makeText(
					getBaseContext(),
					"Cannot export database to\n /Oikonomia @sdcard",
					Toast.LENGTH_LONG).show();
		}
	}
	
	private String getTimeStamp() {
		Calendar c = Calendar.getInstance();
		String s = c.getTime().toString();
		String r = s.replace(" ", "_").replace(":", "_");
		return r;
	}
	
	//not used: kept for future
//	public String generateFileName(String fileNamePrefix,int sequenceNumber,String fileNameExtension) {
// 		char[] buffer = new char[8];
// 		for(int i = buffer.length-1; i >= 0; --i) {
// 			if(sequenceNumber != 0) {
// 				buffer[i] = Character.forDigit((sequenceNumber%10), 10);
// 				sequenceNumber /= 10;
// 			}else{
// 				buffer[i] = '0';
// 			}
// 		}
// 		return fileNamePrefix + String.valueOf(buffer) + fileNameExtension;
// 	}
	
	
	private String currentDate() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String formattedDate = df.format( c.getTime() );
		return formattedDate;
	}
	
	public void onClickAddToExpenses(View view) {
		String strDate = etDate.getText().toString();
		String strItemName = etItemName.getText().toString();
		String strQuantity = etQuantity.getText().toString();
		String strAmount = etAmount.getText().toString();
		
		//A simple sanity check
		if(strDate.length()==0 ||
				strItemName.length()==0 ||
				strQuantity.length()==0||
				strAmount.length()==0) {
			Toast.makeText(getBaseContext(),"Record is not complete.",Toast.LENGTH_LONG).show();
			return;	
		}
		
		OikonomiaRecord orec = new OikonomiaRecord();
		orec.date = strDate;
		orec.itemName = strItemName;
		orec.quantity = Float.parseFloat(strQuantity);
		orec.amount = Float.parseFloat(strAmount);
		if( addRecord(orec) ) {
			etSummary.setEnabled(true);
			etSummary.setText(oqs.prepareSummary());
			etSummary.setEnabled(false);
			Toast.makeText(getBaseContext(),"Saved to expenses.",Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getBaseContext(),"DBError \non saving data.",Toast.LENGTH_LONG).show();
		}
		etDate.setText(currentDate());
		etQuantity.setText(Float.toString(1.0f));
	}
	
	
	public void onClickViewExpenses(View view) {
		int alorSize = alor.size();
		if(alorSize == 0) {
			Toast.makeText(getBaseContext(),"No Records to show.",Toast.LENGTH_LONG).show();
			return;
		}
		als.clear();
		for(int i=0; i<alorSize; i++) {
			OikonomiaRecord orec = alor.get(i);
			als.add(orec.toPresentableString());
		}
		Intent intent = new Intent("com.xyzsoftware.ExpensesViewerActivity");
		intent.putStringArrayListExtra("expensespresentation",als);
		startActivity(intent);
	}
	
	
	public void onClickDeleteExpenses(View view) {
		if(alor.size() == 0) {
			Toast.makeText(getBaseContext(),"No Records to delete.",Toast.LENGTH_LONG).show();
			return;
		}

		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle("Erasing Database")
		.setMessage("This will export the database to a .csv file on sdcard \nand erase the database.\nThis is expected to be a done on monthly or yearly basis.\nDo you wish to continue?")
		.setPositiveButton("Yes", 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						exportArrayListToCSV();
						dbConnector.open();
						dbConnector.eraseExpenses();
						dbConnector.close();
						Toast.makeText(getBaseContext(),"Expenses table is cleared.",Toast.LENGTH_LONG).show();
						alor.clear();
					}
				})
		.setNegativeButton("No", null)
		.show();
	}
	
	@Override
  	 public void onDestroy() {
		super.onDestroy();
		// The activity is about to be destroyed.
		//save summary
		String prefs = getString(R.string.app_prefs);
		SharedPreferences sp = getSharedPreferences(prefs,Activity.MODE_PRIVATE);
		oqs.saveVarsToSystemPreferences(sp);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.oikonomia, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
