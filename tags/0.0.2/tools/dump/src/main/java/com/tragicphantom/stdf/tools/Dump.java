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
