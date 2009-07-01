package com.tragicphantom.stdf.tools;

import com.tragicphantom.stdf.STDFContainer;
import com.tragicphantom.stdf.Record;

public class Dump{
   public static void dumpSTDF(String fileName) throws Exception{
      STDFContainer container = new STDFContainer(fileName);

      System.out.println("Record count: " + container.size());

      for(Record record : container)
         System.out.print(record.toString());
   }

   public static void main(String [] args){
      for(String arg : args){
         try{
            dumpSTDF(arg);
         }
         catch(Exception e){
            e.printStackTrace();
         }
      }
   }
}
