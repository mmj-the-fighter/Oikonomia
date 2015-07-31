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

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

//private ListView list_expenses;
//listViewExpenses


public class ExpensesViewerActivity extends ListActivity {
	
	//private ListView lvExpenses = null;
	private ArrayAdapter<String> adapter = null;
	private ArrayList<String> expensesPresentationList = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_expenses_viewer);
		//lvExpenses = (ListView)findViewById(R.id.listViewExpenses);
		Intent intent = getIntent();
		expensesPresentationList = intent.getStringArrayListExtra("expensespresentation");
		adapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, expensesPresentationList);
		//lvExpenses.setAdapter(adapter);
		setListAdapter(adapter);
	}
}
