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
package com.tragicphantom.stdf;

import java.util.HashMap;

import org.junit.Assert;

public class TestHelper{
   // TODO: example test case that should be updated with a real STDF
   //       from your project along with the applicable statistics
   public static final String TEST_FILE = "../testdata/test.stdf";

   public static HashMap<String, Integer> getStats(STDFContainer container){
      HashMap<String, Integer> stats = new HashMap<String, Integer>();

      for(Record record : container){
         String key = record.getType().toUpperCase();
         if(stats.containsKey(key))
            stats.put(key, stats.get(key) + 1);
         else
            stats.put(key, 1);
      }

      return stats;
   }

   public static void validateContainer(STDFContainer container){
      Assert.assertEquals(9, container.size());
      validateCommonStats(getStats(container));
   }

   public static void validateCommonStats(HashMap<String, Integer> stats){
      Assert.assertEquals(6, stats.size());
      Assert.assertEquals(1, (int)stats.get("FAR"));
      Assert.assertEquals(1, (int)stats.get("MIR"));
      Assert.assertEquals(1, (int)stats.get("MRR"));
      Assert.assertEquals(1, (int)stats.get("PIR"));
      Assert.assertEquals(1, (int)stats.get("PRR"));
      Assert.assertEquals(4, (int)stats.get("DTR"));
   }
}
