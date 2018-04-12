/**
 * Copyright 2009-2012 tragicphantom
 *
 * This file is part of stdf4j.
 *
 * Stdf4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Stdf4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with stdf4j.  If not, see <http://www.gnu.org/licenses/>.
**/
package com.tragicphantom.stdf.tools;

import java.util.Map;
import java.text.ParseException;
import java.util.HashMap;

import com.tragicphantom.stdf.STDFReader;
import com.tragicphantom.stdf.Record;
import com.tragicphantom.stdf.RecordData;
import com.tragicphantom.stdf.RecordVisitor;

public class Stats implements RecordVisitor{
   private HashMap<String, Integer> counts = new HashMap<String, Integer>();
   private int total = 0;
   private int dataLenSum = 0;

   public void beforeFile(){
   }

   public void afterFile(){
   }

   public void handleRecord(Record record){
//      String key = record.getType().toUpperCase();
      try {
		RecordData data = record.getData();
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
//      if(counts.containsKey(key))
//         counts.put(key, counts.get(key) + 1);
//      else
//         counts.put(key, 1);
      
//      dataLenSum += record.getDataLength();
//      total++;
   }

   public void print(){
      System.out.println("Total records: " + total);
//      System.out.println("Avg length: " + dataLenSum/total);

      for(Map.Entry<String, Integer> entry : counts.entrySet())
         System.out.println(entry.getKey() + ": " + entry.getValue());
   }

   public static void main(String [] args){
		com.tragicphantom.stdf.v4.Types.getRecordDescriptors();
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		long start = System.currentTimeMillis();
		for (String arg : args) {
			try {
				Stats stats = new Stats();
				STDFReader reader = new STDFReader(arg);
				reader.setErrorOnUnknown(false);
				reader.parse(stats);
				stats.print();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("time " + (end - start) / 1000.0);
		
		try {
		Thread.sleep(10000);
	} catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
      
   }
}
