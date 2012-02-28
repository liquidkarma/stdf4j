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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;

import java.nio.ByteOrder;

import java.util.Map;

import java.util.zip.GZIPInputStream;

import java.text.ParseException;

import com.tragicphantom.stdf.util.ByteArray;

/**
 * STDFReader
 * Based on pystdf <http://code.google.com/p/pystdf/>
 *     and libstdf <http://freestdf.sourceforge.net/>
 */
public class STDFReader{
   private InputStream stream         = null;
   private int         available      = 0;
   private int         totalBytes     = 0;
   private ByteArray   byteArray      = new ByteArray();
   private boolean     errorOnUnknown = true;

   public STDFReader(String fileName) throws FileNotFoundException, IOException{
      this(new FileInputStream(fileName));
   }

   public STDFReader(File file) throws FileNotFoundException, IOException{
      this(new FileInputStream(file));
   }

   public STDFReader(InputStream stream) throws IOException{
      InputStream bufis = new BufferedInputStream(stream);
      bufis.mark(2);
      int header = ((bufis.read() & 0xFF) << 8) + (bufis.read() & 0xFF);
      bufis.reset();
      if(header == 0x1F8B /*GZIP*/)
         this.stream = new BufferedInputStream(new GZIPInputStream(bufis));
      else
         this.stream = bufis;
   }

   public void setErrorOnUnknown(boolean errorOnUnknown){
      this.errorOnUnknown = errorOnUnknown;
   }

   public void parse(RecordVisitor visitor) throws FileNotFoundException,
                                                   IOException,
                                                   ParseException{
      // default to v4 types if none specified
      parse(visitor, com.tragicphantom.stdf.v4.Types.getRecordDescriptors());
   }

   public void parse(RecordVisitor visitor,
                     Map<RecordType, RecordDescriptor> records)
               throws FileNotFoundException,
                      IOException,
                      ParseException{
      if(stream == null)
         throw new FileNotFoundException();

      byteArray.setByteOrder(ByteOrder.nativeOrder());

      visitor.beforeFile();

      try{
         Header header = new Header();

         // verify first record in file is a FAR
         readHeader(header);
         if(header.getType() != 0 && header.getSubType() != 10)
            throw new ParseException("Invalid header sequence", 0);

         Record record = readRecord(header, records);

         if(record == null)
            throw new ParseException("Unknown record type cannot be first in file", 0);

         // set byte order based on FAR contents
         RecordData far = record.getData();
         long cpuType = (Long)far.getField("CPU_TYPE");
         byteArray.setByteOrder((cpuType == 1) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);

         visitor.handleRecord(record);

         // read until IOException
         while(true){
            readHeader(header);
            record = readRecord(header, records);
            if(record != null)
               visitor.handleRecord(record);
         }
      }
      catch(IOException e){
         // Ignore
         //e.printStackTrace();
      }
      finally{
         stream.close();
         stream = null;
      }

      visitor.afterFile();
   }

   protected void readHeader(Header header) throws IOException,
                                                   ParseException{
      available = 4;
      header.set(readUnsignedInt(2),
                 readUnsignedInt(1),
                 readUnsignedInt(1));
   }

   protected Record readRecord(Header header,
                               Map<RecordType, RecordDescriptor> records)
                    throws IOException,
                           ParseException{
      Record record = null;

      available = header.getLength();

      //System.err.println(totalBytes + "[" + String.format("0x%x", totalBytes) + "]: " + header.getType() + ", " + header.getSubType() + ": " + available + " bytes");

      if(records.containsKey(header.getRecordType())){
         record = new Record(records.get(header.getRecordType()),
                             totalBytes,
                             getBytes(header.getLength()),
                             byteArray.getByteOrder());
      }
      else{
         // this may just be a user-defined record type not specified
         // in the provided specification
         // error out for now, but we may want an option to just warn if
         // file is still valid and want to read anyway
         if(errorOnUnknown){
            throw new ParseException("Unknown record type found at offset " + totalBytes + " (" + String.format("0x%x", totalBytes) + "): " + header.getType() + ", " + header.getSubType(), totalBytes);
         }
         else{
            System.err.println("WARNING: Skipping unknown record type: " + header.getType() + ", " + header.getSubType() + " [" + header.getLength() + " bytes] at offset " + totalBytes);
            getBytes(header.getLength());
         }
      }

      return record;
   }

   protected int readUnsignedInt(int length) throws IOException{
      return byteArray.toUnsignedInt(getBytes(length), length);
   }

   protected byte[] getBytes(int numBytes) throws IOException{
      available  -= numBytes;
      totalBytes += numBytes;

      if(available < 0){
         numBytes   += available;
         totalBytes += available;
         available   = 0;
      }

      byte [] bytes = new byte[numBytes];
      int actualBytes = 0;
      if((actualBytes = stream.read(bytes, 0, numBytes)) != numBytes){
         int offset = 0;
         while(actualBytes > 0){
            numBytes -= actualBytes;
            offset   += actualBytes;
            if((actualBytes = stream.read(bytes, offset, numBytes)) == numBytes)
               break;
         }

         if(actualBytes != numBytes)
            throw new IOException("Invalid number of bytes read (expected: " + numBytes + ", got: " + actualBytes + ")");
      }

      return bytes;
   }
}
