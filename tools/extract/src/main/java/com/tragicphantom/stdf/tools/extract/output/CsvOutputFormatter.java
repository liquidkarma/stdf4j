package com.tragicphantom.stdf.tools.extract.output;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.ArrayList;

import com.tragicphantom.stdf.Record;

public class CsvOutputFormatter implements OutputFormatter{
   private String lastHeaderType = "";

   public CsvOutputFormatter(){
   }

   private String join(Iterable<?> values, String delim){
      boolean       useDelim = false;
      StringBuilder sb       = new StringBuilder();
      for(Object value : values){
         if(useDelim)
            sb.append(delim);
         else
            useDelim = true;

         if(value != null)
            sb.append(value.toString());
      }
      return sb.toString();
   }

   public void write(Record record){
      String                  type   = record.getType().toUpperCase();
      TreeMap<String, Object> fields = new TreeMap<String, Object>(record.getFields());

      if(!type.equals(lastHeaderType)){
         lastHeaderType = type;

         Set<String>       keys    = fields.keySet();
         ArrayList<String> newKeys = new ArrayList<String>();

         for(String key : keys)
            newKeys.add(type + ":" + key);

         System.out.println(join(newKeys, ","));
      }

      System.out.println(join(fields.values(), ","));
   }
}
