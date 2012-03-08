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

import org.junit.Test;
import org.junit.Assert;

public class ThreadTest{
   // TODO: example test case that should be updated with a real STDF
   //       from your project along with the applicable statistics
   @Test
   public void testThreadRead() throws Exception{
      QueueVisitor queue  = new QueueVisitor(100);
      ParserThread parser = new ParserThread(TestHelper.TEST_FILE, queue);
      ReaderThread reader = new ReaderThread(queue);

      Thread parserThread = new Thread(parser);
      Thread readerThread = new Thread(reader);

      parserThread.start();
      readerThread.start();

      parserThread.join();
      readerThread.join();

      TestHelper.validateCommonStats(reader.getStats());
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
            e.printStackTrace();
            Assert.fail(e.getMessage());
         }
      }
   }

   private static class ReaderThread implements Runnable{
      private QueueVisitor queue;
      private HashMap<String, Integer> stats = new HashMap<String, Integer>();

      public ReaderThread(QueueVisitor queue){
         this.queue = queue;
      }

      public HashMap<String, Integer> getStats(){
         return stats;
      }

      public void run(){
         try{
            Record record;
            while((record = queue.next()) != null){
               String key = record.getType().toUpperCase();
               if(stats.containsKey(key))
                  stats.put(key, stats.get(key) + 1);
               else
                  stats.put(key, 1);
            }
         }
         catch(Exception e){
            Assert.fail(e.getMessage());
         }
      }
   }
}
