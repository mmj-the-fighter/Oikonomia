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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.SharedPreferences;

public class OikonomiaQuickSummary {
	public float thisYearsExpense;
	public float thisMonthsExpense;
	public float todaysExpense;
	public int  refDay;
	public int refMonth;
	public int refYear;
	
	public OikonomiaQuickSummary() {}
	
	public String prepareSummary() {
		String s = 
				"Today: "+String.valueOf(todaysExpense) + "\n"
				+"This Month: "+String.valueOf(thisMonthsExpense) + "\n"
				+"This Year: "+String.valueOf(thisYearsExpense);
		return s;
	}
	
	public void setReferenceDate(int d, int m, int y) {
		refDay = d;
		refMonth = m;
		refYear = y;
	}
	
	public void loadVarsFromSystemPreferences(SharedPreferences sp) {
		//Calculate "today" before loading
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String formattedDate = df.format( c.getTime() );
		String[] dateComponents = formattedDate.split("/");
		int d = Integer.parseInt( dateComponents[0] );
		int m = Integer.parseInt( dateComponents[1] );
		int y = Integer.parseInt( dateComponents[2] );
		int lrefDay = sp.getInt("refDay", -1);
		int lrefMonth = sp.getInt("refMonth", -1);
		int lrefYear = sp.getInt("refYear", -1);
		//hint:l stands for loaded
		if(lrefDay==d && lrefMonth==m && lrefYear==y) {
			thisYearsExpense = sp.getFloat("thisYearsExpense", 0.0f);
			thisMonthsExpense = sp.getFloat("thisMonthsExpense", 0.0f);
			todaysExpense = sp.getFloat("todaysExpense", 0.0f);
		} else if(lrefMonth==m && lrefYear==y){
			thisYearsExpense = sp.getFloat("thisYearsExpense", 0.0f);
			thisMonthsExpense = sp.getFloat("thisMonthsExpense", 0.0f);
			todaysExpense = 0.0f;
		} else if(lrefYear==y) {
			thisYearsExpense = sp.getFloat("thisYearsExpense", 0.0f);
			thisMonthsExpense = 0.0f;
			todaysExpense = 0.0f;
		} else {
			thisYearsExpense = 0.0f;
			thisMonthsExpense = 0.0f;
			todaysExpense = 0.0f;			
		}
		refDay = d;
		refMonth = m;
		refYear = y;
	}
	
	public void saveVarsToSystemPreferences(SharedPreferences sp) {
		SharedPreferences.Editor spe = sp.edit();
		//Calculate "today" before saving
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String formattedDate = df.format( c.getTime() );
		String[] dateComponents = formattedDate.split("/");
		int d = Integer.parseInt( dateComponents[0] );
		int m = Integer.parseInt( dateComponents[1] );
		int y = Integer.parseInt( dateComponents[2] );
		
		if(refDay==d && refMonth==m && refYear==y) {
			spe.putFloat("thisYearsExpense",thisYearsExpense);
			spe.putFloat("thisMonthsExpense",thisMonthsExpense);
			spe.putFloat("todaysExpense",todaysExpense);
		} else if(refMonth==m && refYear==y) {
			spe.putFloat("thisYearsExpense",thisYearsExpense);
			spe.putFloat("thisMonthsExpense",thisMonthsExpense);
			spe.putFloat("todaysExpense",0.0f);
		} else if(refYear==y) {
			spe.putFloat("thisYearsExpense",thisYearsExpense);
			spe.putFloat("thisMonthsExpense",0.0f);
			spe.putFloat("todaysExpense",0.0f);
		} else {
			spe.putFloat("thisYearsExpense",0.0f);
			spe.putFloat("thisMonthsExpense",0.0f);
			spe.putFloat("todaysExpense",0.0f);
		}
		spe.putInt("refDay",d);
		spe.putInt("refMonth",m);
		spe.putInt("refYear",y);
		spe.apply();
	}
	
	public void resetVarsAndSaveToPreferences(SharedPreferences sp) {
		SharedPreferences.Editor spe = sp.edit();
		spe.putFloat("thisYearsExpense",0.0f);
		spe.putFloat("thisMonthsExpense",0.0f);
		spe.putFloat("todaysExpense",0.0f);
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String formattedDate = df.format( c.getTime() );
		String[] dateComponents = formattedDate.split("/");
		int d = Integer.parseInt( dateComponents[0] );
		int m = Integer.parseInt( dateComponents[1] );
		int y = Integer.parseInt( dateComponents[2] );
		spe.putInt("refDay",d);
		spe.putInt("refMonth",m);
		spe.putInt("refYear",y);
		spe.apply();
	}
}