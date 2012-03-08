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

import java.io.File;

import org.junit.Test;
import org.junit.Assert;

public class WriterTest{
   private static final String STATIC_VALUE = "AAAAAA";

   private void validateWrite(STDFContainer container, String fileName) throws Exception{
      STDFWriter writer = new STDFWriter(fileName);
      writer.write(container);
      writer.close();

      STDFContainer containerCheck = new STDFContainer(fileName);
      TestHelper.validateContainer(containerCheck);

      for(Record record : containerCheck){
         if(record.getType().toUpperCase().equals("DTR")){
            RecordData data = record.getData();
            if(data.hasField("TEXT_DAT")){
               String value = (String)data.getField("TEXT_DAT");
               Assert.assertEquals(STATIC_VALUE, value);
            }
         }
      }

      new File(fileName).delete();
   }

   @Test
   public void testWriter() throws Exception{
      STDFContainer container = new STDFContainer(TestHelper.TEST_FILE);
      TestHelper.validateContainer(container);

      for(Record record : container){
         if(record.getType().toUpperCase().equals("DTR")){
            RecordData data = record.getData();
            if(data.hasField("TEXT_DAT"))
               data.setField("TEXT_DAT", STATIC_VALUE);
         }
      }

      // validate regular file write
      String tmpOutput = "../testdata/tmp_test_writer.std";
      validateWrite(container, tmpOutput);

      // validate compressed file write
      validateWrite(container, tmpOutput + ".gz");
   }
}
