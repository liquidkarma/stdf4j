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
package com.tragicphantom.stdf.tools.extract;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;

import com.tragicphantom.stdf.Record;

import com.tragicphantom.stdf.tools.extract.output.OutputFormatter;
import com.tragicphantom.stdf.tools.extract.output.DefaultOutputFormatter;
import com.tragicphantom.stdf.tools.extract.output.RawOutputFormatter;
import com.tragicphantom.stdf.tools.extract.output.CsvOutputFormatter;

public class CommandLine{
   private static Options setupOptions(){
      return new Options()
             .addOption("x", "raw"  , false, "raw output")
             .addOption("c", "csv"  , false, "csv output (this feature is broken)")
             .addOption("t", "types", true , "comma-delimited list of record types to extract");
   }

   public static void main(String [] args){
      try{
         Options                            options = setupOptions();
         org.apache.commons.cli.CommandLine cl      = new GnuParser().parse(options, args);

         if(cl.hasOption("t")){
            HashSet<String> types = new HashSet<String>(Arrays.asList(cl.getOptionValue("t").split(",")));

            OutputFormatter outputFormatter;

            if(cl.hasOption("x"))
               outputFormatter = new RawOutputFormatter();
            else if(cl.hasOption("c"))
               outputFormatter = new CsvOutputFormatter();
            else
               outputFormatter = new DefaultOutputFormatter();

            for(String file : cl.getArgs()){
               System.out.println(file);

               Visitor visitor = new Visitor(file, types);

               System.out.println(visitor.size() + " records found");

               for(Record record : visitor)
                  outputFormatter.write(record);
            }
         }
         else
            new HelpFormatter().printHelp("extract", options);
      }
      catch(Exception e){
         e.printStackTrace();
      }
   }
}
