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
import java.util.HashMap;

import com.tragicphantom.stdf.STDFReader;
import com.tragicphantom.stdf.Record;
import com.tragicphantom.stdf.RecordVisitor;

public class Stats implements RecordVisitor{
   private HashMap<String, Integer> counts = new HashMap<String, Integer>();
   private int total = 0;

   public void beforeFile(){
   }

   public void afterFile(){
   }

   public void handleRecord(Record record){
      String key = record.getType().toUpperCase();
      if(counts.containsKey(key))
         counts.put(key, counts.get(key) + 1);
      else
         counts.put(key, 1);
      total++;
   }

   public void print(){
      System.out.println("Total records: " + total);

      for(Map.Entry<String, Integer> entry : counts.entrySet())
         System.out.println(entry.getKey() + ": " + entry.getValue());
   }

   public static void main(String [] args){
      for(String arg : args){
         try{
            Stats      stats  = new Stats();
            STDFReader reader = new STDFReader(arg);
            reader.setErrorOnUnknown(false);
            reader.parse(stats);
            stats.print();
         }
         catch(Exception e){
            e.printStackTrace();
         }
      }
   }
}
