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
import com.tragicphantom.stdf.RecordVisitor;
import com.tragicphantom.stdf.QueueVisitor;
import com.tragicphantom.stdf.STDFReader;

// A timing helper to test dump times for various read methods
public class MTDump{
   private static long _start;

   private static void start(){
      _start = System.nanoTime();
   }

   private static void stop(String type){
      System.err.println(type + " Time: " + ((System.nanoTime() - _start) / 1000000.0));
   }

   public static void containerDump(String fileName) throws Exception{
      STDFContainer container = new STDFContainer(fileName);

      System.out.println("Record count: " + container.size());

      for(Record record : container)
         System.out.print(record.toString());
   }

   public static void visitorDump(String fileName) throws Exception{
      new STDFReader(fileName).parse(new RecordVisitor(){
         public void beforeFile(){
         }

         public void afterFile(){
         }

         public void handleRecord(Record record){
            System.out.print(record.toString());
         }
      });
   }

   public static void threadDump(String fileName) throws Exception{
      QueueVisitor queue  = new QueueVisitor(100);

      Thread parserThread = new Thread(new ParserThread(fileName, queue));
      Thread outputThread = new Thread(new OutputThread(queue));
      /*
      Thread [] outputThreads = new Thread[]{
         new Thread(new OutputThread(queue)),
         new Thread(new OutputThread(queue)),
         new Thread(new OutputThread(queue))
      };
      */

      parserThread.start();
      outputThread.start();
      /*
      for(Thread ot : outputThreads)
         ot.start();
      */

      parserThread.join();
      outputThread.join();
      /*
      for(Thread ot : outputThreads)
         ot.join();
      */
   }

   private static class ParserThread implements Runnable{
      private STDFReader   reader;
      private QueueVisitor queue;

      public ParserThread(String file, QueueVisitor queue) throws Exception{
         this.reader = new STDFReader(file);
         this.queue  = queue;
      }

      public void run(){
         try{
            reader.parse(queue);
         }
         catch(Exception e){
            throw new RuntimeException(e);
         }
      }
   }

   private static class OutputThread implements Runnable{
      private QueueVisitor queue;

      public OutputThread(QueueVisitor queue){
         this.queue = queue;
      }

      public void run(){
         try{
            Record record;
            while((record = queue.next()) != null)
               System.out.println(record.toString());
         }
         catch(Exception e){
            throw new RuntimeException(e);
         }
      }
   }

   public static void main(String [] args){
      for(String arg : args){
         try{
            start();
            containerDump(arg);
            stop("Container");

            start();
            visitorDump(arg);
            stop("Visitor");

            start();
            threadDump(arg);
            stop("Thread");
         }
         catch(Exception e){
            e.printStackTrace();
         }
      }
   }
}
