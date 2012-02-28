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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class QueueVisitor implements RecordVisitor{
   private BlockingQueue<Record> queue;

   public QueueVisitor(int size){
      queue = new LinkedBlockingQueue(size);
   }

   public void beforeFile(){
   }

   public void afterFile(){
      try{
         queue.put(new Record(null, null));
      }
      catch(InterruptedException e){
         throw new RuntimeException(e);
      }
   }

   public void handleRecord(Record record){
      try{
         queue.put(record);
      }
      catch(InterruptedException e){
         throw new RuntimeException(e);
      }
   }

   public Record next() throws InterruptedException{
      return queue.take();
   }

   public Record next(long timeout, TimeUnit unit) throws InterruptedException{
      return queue.poll(timeout, unit);
   }
}
