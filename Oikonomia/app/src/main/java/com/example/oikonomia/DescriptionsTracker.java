package com.example.oikonomia;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DescriptionCountPair{
    public String description;
    public Integer count;
    public void set(String d, Integer c){
        description = d;
        count = c;
    }
}
class DescriptionCountComparator implements Comparator<DescriptionCountPair> {

    // override the compare() method
    public int compare(DescriptionCountPair dc1, DescriptionCountPair dc2)
    {
        return dc2.count.compareTo(dc1.count);
    }
}
public class DescriptionsTracker {
    private static final int FETCH_COUNT_LIMIT = 2048;
    private static final int STORE_SIZE_LIMIT = 2048;
    private static HashMap<String, Integer> incomeDescriptionCountMap = new HashMap<>();
    private static List<DescriptionCountPair> incomeDescriptionCountList = new ArrayList<>();

    private static HashMap<String, Integer> expenseDescriptionCountMap = new HashMap<>();
    private static List<DescriptionCountPair> expenseDescriptionCountList = new ArrayList<>();
    private static DescriptionCountComparator comparator = new DescriptionCountComparator();

    public static String [] mostlyUsedDescriptions;
    public static String[] defaultDescriptions={"food","travel","cloths", "rent","salary"};

    private static void addIncomeDescription(String decription){
        if(incomeDescriptionCountMap.size() <= STORE_SIZE_LIMIT)
        {
            incomeDescriptionCountMap.put(decription, incomeDescriptionCountMap.getOrDefault(decription, 0) + 1);
        }
    }

    private static void addExpenseDescription(String decription){
        if(expenseDescriptionCountMap.size() <= STORE_SIZE_LIMIT)
        {
            expenseDescriptionCountMap.put(decription, expenseDescriptionCountMap.getOrDefault(decription, 0) + 1);
        }
    }

    public static void generateMostlyUsedDescriptions(int incomeDescLimit, int expenseDescLimit)
    {
        int incomeDecriptionCountListSize = 0;
        if(incomeDescLimit > 0){
            incomeDescriptionCountList.clear();
            for (Map.Entry<String, Integer> entry : incomeDescriptionCountMap.entrySet()) {
                DescriptionCountPair dc= new DescriptionCountPair();
                dc.set(entry.getKey(), entry.getValue());
                incomeDescriptionCountList.add(dc);
            }
            incomeDecriptionCountListSize = incomeDescriptionCountList.size();
        }

        int expenseDecriptionCountListSize = 0;
        if(expenseDescLimit > 0){
            expenseDescriptionCountList.clear();
            for (Map.Entry<String, Integer> entry : expenseDescriptionCountMap.entrySet()) {
                DescriptionCountPair dc= new DescriptionCountPair();
                dc.set(entry.getKey(), entry.getValue());
                expenseDescriptionCountList.add(dc);
            }
            expenseDecriptionCountListSize = expenseDescriptionCountList.size();
        }


        if(incomeDecriptionCountListSize == 0 && expenseDecriptionCountListSize == 0) {
            mostlyUsedDescriptions = defaultDescriptions;
            return;
        }
        Collections.sort(incomeDescriptionCountList, comparator);
        Collections.sort(expenseDescriptionCountList, comparator);
        int nIncome = incomeDescLimit;
        if(incomeDescLimit > incomeDecriptionCountListSize){
            nIncome = incomeDecriptionCountListSize;
        }
        int nExpense = expenseDescLimit;
        if(expenseDescLimit > expenseDecriptionCountListSize){
            nExpense = expenseDecriptionCountListSize;
        }
        int n = nIncome + nExpense;
        mostlyUsedDescriptions = new String[n];
        int i=0;
        for(; i<nIncome; i++){
            mostlyUsedDescriptions[i] = incomeDescriptionCountList.get(i).description;
        }

        for(int k=0; k<nExpense; k++){
            mostlyUsedDescriptions[i] = expenseDescriptionCountList.get(k).description;
            i++;
        }
    }

    public static int update(Cursor cursor) {
        int dateIndex = -1;

        if (cursor.moveToFirst()) {
            int descIndex = cursor.getColumnIndex(DBConnector.KEY_DESC);
            if (descIndex < 0) {
                return 0;
            }
            int isIncomeIntIndex = cursor.getColumnIndex(DBConnector.KEY_IS_INCOME);
            if (isIncomeIntIndex < 0) {
                return 0;
            }
            int fetchCount = 1;
            do {
                String desc = cursor.getString(descIndex);
                int isIncomeInt = cursor.getInt(isIncomeIntIndex);
                if(isIncomeInt == 0){
                    addExpenseDescription(desc);
                }else{
                    addIncomeDescription(desc);
                }
                if(fetchCount > FETCH_COUNT_LIMIT){
                    break;
                }
                ++fetchCount;
            } while (cursor.moveToNext());
        }
        int size1 = incomeDescriptionCountMap.size();
        int size2 = incomeDescriptionCountMap.size();
        return size1 + size2;
    }
}
