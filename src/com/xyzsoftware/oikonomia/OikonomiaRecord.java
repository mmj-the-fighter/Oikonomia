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

import java.io.IOException;
import java.io.OutputStreamWriter;

public class OikonomiaRecord {
	public String date;//for db
	public int  day;
	public int month;
	public int year;
	
	public String itemName;//for db
	
	public float quantity;//for db
	
	public float amount;//for db
	
	public void parseDate() {
		String[] dateComponents = date.split("/");
		day 	= Integer.parseInt( dateComponents[0] );
		month 	= Integer.parseInt( dateComponents[1] );
		year 	= Integer.parseInt( dateComponents[2] );
	}

	public boolean compareDateForToday(int d,int m, int y) {
		return day==d && month==m && year==y;
	}
	
	public boolean compareDateForThisMonth(int d,int m, int y) {
		return month==m && year==y;
	}
	
	public boolean compareDateForThisYear(int d,int m, int y) {
		return year==y;
	}
	

	public String toPresentableString() {
		String s = date +"\n" 
					+ itemName + "\n"
					+ String.valueOf(quantity) + "\n"
					+ String.valueOf(amount);
		return s;
	}
	
	public void writeCSV(OutputStreamWriter osw) throws IOException{
		osw.write(date);osw.write(",");
		osw.write(itemName);osw.write(",");
		osw.write(Float.toString(quantity));osw.write(",");
		osw.write(Float.toString(amount));osw.write("\n");
	}
}

