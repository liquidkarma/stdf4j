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

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;

import org.junit.Test;
import org.junit.Assert;

import com.tragicphantom.stdf.Record;
import com.tragicphantom.stdf.RecordData;
import com.tragicphantom.stdf.RecordType;
import com.tragicphantom.stdf.RecordDescriptor;
import com.tragicphantom.stdf.STDFWriter;

public class SampleFile{
   private static final Map<String, RecordDescriptor> types = createTypeMap(com.tragicphantom.stdf.v4.Types.getRecordDescriptors());

   private static Map<String, RecordDescriptor> createTypeMap(Map<RecordType, RecordDescriptor> spec){
      Map<String, RecordDescriptor> types = new HashMap<String, RecordDescriptor>();
      for(RecordDescriptor desc : spec.values())
         types.put(desc.getType().toUpperCase(), desc);
      return types;
   }

   private Record createRecord(String type, Object... values){
      RecordDescriptor desc = types.get(type);
      Assert.assertEquals(desc.size(), values.length);
      Object [] fields = new Object[desc.size()];
      for(int i = 0; i < values.length && i < desc.size(); i++)
         fields[i] = values[i];
      return new Record(desc, new RecordData(desc, fields));
   }

   private long today(){
      return Calendar.getInstance().getTimeInMillis() / 1000;
   }

   @Test
   public void testSampleFile() throws Exception{
      Map<String, RecordDescriptor> types = createTypeMap(com.tragicphantom.stdf.v4.Types.getRecordDescriptors());

      ArrayList<Record> records = new ArrayList<Record>();
      records.add(createRecord("FAR", 2, 4));
      records.add(createRecord("MIR", today(), today(), 1, " ", " ", " ", 65535, " ", "lotid", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
      records.add(createRecord("DTR", "hello world"));
      records.add(createRecord("DTR", "testing 123"));
      records.add(createRecord("DTR", "testing 456"));
      records.add(createRecord("DTR", "testing 789"));
      records.add(createRecord("PIR", 1, 1));
      records.add(createRecord("PRR", 1, 1, 0, 0, 1, 1, 20, 20, 5000, "p1", "", null));
      records.add(createRecord("MRR", today(), " ", null, null));

      STDFWriter writer = new STDFWriter("test.stdf");
      writer.write(records);
      writer.close();
   }
}
