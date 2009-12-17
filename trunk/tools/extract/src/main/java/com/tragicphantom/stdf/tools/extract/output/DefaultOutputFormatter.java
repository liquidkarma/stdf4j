package com.tragicphantom.stdf.tools.extract.output;

import com.tragicphantom.stdf.Record;

public class DefaultOutputFormatter implements OutputFormatter{
   public DefaultOutputFormatter(){
   }

   public void write(Record record){
      System.out.println(record);
   }
}
