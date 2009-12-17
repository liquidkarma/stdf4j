package com.tragicphantom.stdf.tools.extract.output;

import java.util.Map;
import java.util.HashMap;

import com.tragicphantom.stdf.Record;

public class RawOutputFormatter implements OutputFormatter{
   public RawOutputFormatter(){
   }

   public void write(Record record){
      System.out.println(record.getType().toUpperCase());
      HashMap<String, Object> fields = record.getFields();
      for(Map.Entry<String, Object> field : fields.entrySet())
         System.out.println("   " + field.getKey() + " = " + field.getValue());
   }
}
