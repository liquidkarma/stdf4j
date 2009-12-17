/**
 * Copyright 2009 tragicphantom
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

import java.io.File;

import org.junit.Test;
import org.junit.Assert;

public class WriterTest{
   private static final String STATIC_VALUE = "AAAAAA";

   private void validateContainer(STDFContainer container){
      Assert.assertEquals(153580, container.size());

      HashMap<String, Integer> stats = TestHelper.getStats(container);

      Assert.assertEquals(300   , (int)stats.get("BPS"));
      Assert.assertEquals(9683  , (int)stats.get("DTR"));
      Assert.assertEquals(300   , (int)stats.get("EPS"));
      Assert.assertEquals(1     , (int)stats.get("FAR"));
      Assert.assertEquals(4     , (int)stats.get("HBR"));
      Assert.assertEquals(1     , (int)stats.get("MIR"));
      Assert.assertEquals(1     , (int)stats.get("MRR"));
      Assert.assertEquals(2     , (int)stats.get("PCR"));
      Assert.assertEquals(85    , (int)stats.get("PGR"));
      Assert.assertEquals(300   , (int)stats.get("PIR"));
      Assert.assertEquals(1     , (int)stats.get("PLR"));
      Assert.assertEquals(409   , (int)stats.get("PMR"));
      Assert.assertEquals(300   , (int)stats.get("PRR"));
      Assert.assertEquals(141667, (int)stats.get("PTR"));
      Assert.assertEquals(7     , (int)stats.get("SBR"));
      Assert.assertEquals(1     , (int)stats.get("SDR"));
      Assert.assertEquals(518   , (int)stats.get("TSR"));
   }

   private void validateContainer2(STDFContainer container){
      Assert.assertEquals(367736, container.size());

      HashMap<String, Integer> stats = TestHelper.getStats(container);

      Assert.assertEquals(994   , (int)stats.get("BPS"));
      Assert.assertEquals(22793 , (int)stats.get("DTR"));
      Assert.assertEquals(994   , (int)stats.get("EPS"));
      Assert.assertEquals(1     , (int)stats.get("FAR"));
      Assert.assertEquals(23135 , (int)stats.get("FTR"));
      Assert.assertEquals(12    , (int)stats.get("HBR"));
      Assert.assertEquals(1     , (int)stats.get("MIR"));
      Assert.assertEquals(1     , (int)stats.get("MRR"));
      Assert.assertEquals(2     , (int)stats.get("PCR"));
      Assert.assertEquals(46    , (int)stats.get("PGR"));
      Assert.assertEquals(994   , (int)stats.get("PIR"));
      Assert.assertEquals(1     , (int)stats.get("PLR"));
      Assert.assertEquals(360   , (int)stats.get("PMR"));
      Assert.assertEquals(994   , (int)stats.get("PRR"));
      Assert.assertEquals(317036, (int)stats.get("PTR"));
      Assert.assertEquals(15    , (int)stats.get("SBR"));
      Assert.assertEquals(1     , (int)stats.get("SDR"));
      Assert.assertEquals(353   , (int)stats.get("TSR"));
      Assert.assertEquals(1     , (int)stats.get("WCR"));
      Assert.assertEquals(1     , (int)stats.get("WIR"));
      Assert.assertEquals(1     , (int)stats.get("WRR"));
   }

   private void validateWrite(STDFContainer container, String fileName) throws Exception{
      STDFWriter writer = new STDFWriter(fileName);
      writer.write(container);
      writer.close();

      STDFContainer containerCheck = new STDFContainer(fileName);
      validateContainer(containerCheck);

      for(Record record : containerCheck){
         if(record.getType().toUpperCase().equals("DTR")){
            if(record.hasField("TEXT_DAT")){
               String value = (String)record.getField("TEXT_DAT");
               Assert.assertEquals(STATIC_VALUE, value);
            }
         }
      }

      new File(fileName).delete();
   }

   @Test
   public void testWriter() throws Exception{
      STDFContainer container = new STDFContainer("testdata/test_file.std.gz");
      validateContainer(container);

      for(Record record : container){
         if(record.getType().toUpperCase().equals("DTR")){
            if(record.hasField("TEXT_DAT"))
               record.setField("TEXT_DAT", STATIC_VALUE);
         }
      }

      // validate regular file write
      String tmpOutput = "testdata/tmp_test_writer.std";
      validateWrite(container, tmpOutput);

      // validate compressed file write
      validateWrite(container, tmpOutput + ".gz");
   }
}
