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
package com.tragicphantom.stdf.tools.viewer;

import java.awt.Component;

import javax.swing.*;
import javax.swing.tree.*;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import com.tragicphantom.stdf.STDFContainer;
import com.tragicphantom.stdf.Record;
import com.tragicphantom.stdf.STDFWriter;

public class FileLoader{
   private static Vector<STDFContainer> stdfContainers = new Vector<STDFContainer>();
   private static Vector<String>        fileNames      = new Vector<String>();

   private Panel panel = null;

   private DefaultMutableTreeNode root      = null;
   private DefaultTreeModel       treeModel = null;

   private String lotId            = "";
   private int    recordCount      = 0;
   private int    totalRecordCount = 0;
   private int    totalUnits       = 0;
   private int    startDepth       = -1;

   private static int nextFileId = 0;

   public FileLoader(Panel panel){
      this.panel = panel;
   }

   public boolean addFile(String fileName){
      boolean loaded = false;

      try{
         stdfContainers.addElement(new STDFContainer(fileName));
         fileNames.add(fileName);
         loaded = true;
      }
      catch(Exception e){
         e.printStackTrace();
      }

      return loaded;
   }

   public static void closeFiles(){
      stdfContainers.clear();
      fileNames.clear();
   }

   public static int getNumFiles(){
      return stdfContainers.size();
   }

   public static String getFileName(int index){
      if(index >= 0 && index < fileNames.size())
         return fileNames.elementAt(index);
      else
         return null;
   }

   public static List<Record> getAllSTDFRecordsOfType(String type){
      type = type.toUpperCase();

      ArrayList<Record> records = new ArrayList<Record>();

      for(STDFContainer stdf : stdfContainers){
         for(Record record : stdf){
            if(type.equals(record.getType().toUpperCase()))
               records.add(record);
         }
      }

      return records;
   }

   public static boolean hasRecordOfType(String type){
      type = type.toUpperCase();

      for(STDFContainer stdf : stdfContainers){
         for(Record record : stdf){
            if(type.equals(record.getType().toUpperCase()))
               return true;
         }
      }

      return false;
   }

   private String makeStdfWaferKey(Record record){
      String key = null;
      if(record.hasField("HEAD_NUM") && record.getField("HEAD_NUM") != null)
         key = record.getField("HEAD_NUM").toString();
      return key;
   }

   private String makeStdfUnitKey(Record record){
      String key = null;
      if(record.hasField("HEAD_NUM") && record.getField("HEAD_NUM") != null &&
         record.hasField("SITE_NUM") && record.getField("SITE_NUM") != null){
         key = new StringBuilder(record.getField("HEAD_NUM").toString())
               .append(",")
               .append(record.getField("SITE_NUM").toString())
               .toString();
      }
      return key;
   }

   private void addStdfRecord(Record record,
                              MutableTreeNode parent,
                              int depth, int fileId,
                              HashMap<String, MutableTreeNode> waferParents,
                              HashMap<String, MutableTreeNode> unitParents){
      String label = record.getType().toUpperCase();

      if(label.equals("MIR")){
         Object lotObj = record.getField("LOT_ID");
         lotId = lotObj == null ? "" : lotObj.toString();
         if(lotId.length() > 0 && !lotId.equals(" "))
            label += " - " + lotId;
      }
      else if(label.equals("WIR")){
         label += " - " + record.getField("WAFER_ID");
      }
      else if(label.equals("PIR")){
         label += " - " + record.getField("HEAD_NUM")
                        + ", "
                        + record.getField("SITE_NUM");

         totalUnits++;
      }
      else if(label.equals("PRR")){
         label += " - " + record.getField("HEAD_NUM")
                        + ", "
                        + record.getField("SITE_NUM")
                        + " - HB = " + record.getField("HARD_BIN")
                        + ", SB = " + record.getField("SOFT_BIN");
      }
      else if(label.equals("DTR")){
         Object textObj = record.getField("TEXT_DAT");
         String text = textObj == null ? "" : textObj.toString();
         if(text.length() > 30)
            text = text.substring(0, 30) + "...";
         label += " - \"" + text + "\"";
      }
      else if(label.equals("PGR"))
         label += " - " + record.getField("GRP_NAM");
      else if(label.equals("PTR")
              || label.equals("MPR")
              || label.equals("FTR"))
         label += " - " + record.getField("TEST_TXT")
                        + " [" + record.getField("TEST_NUM") + "]";
      else if(label.equals("BPS"))
         label += " - " + record.getField("SEQ_NAME");
      else if(label.equals("HBR")){
         label += " - " + record.getField("HBIN_NAM")
                        + " [" + record.getField("HBIN_NUM")
                        + "] - " + record.getField("HBIN_PF")
                        + " = " + record.getField("HBIN_CNT");
      }
      else if(label.equals("SBR")){
         label += " - " + record.getField("SBIN_NAM")
                        + " [" + record.getField("SBIN_NUM")
                        + "] - " + record.getField("SBIN_PF")
                        + " = " + record.getField("SBIN_CNT");
      }
      else if(label.equals("TSR"))
         label += " - " + record.getField("TEST_NAM")
                        + " [" + record.getField("TEST_NUM") + "]";

      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TreeRecord(record, label, fileId));

      if(label.startsWith("WIR")){
         String waferKey = makeStdfWaferKey(record);
         if(waferKey != null)
            waferParents.put(waferKey, node);
         waferParents.put("LATEST", node);
      }
      else if(label.startsWith("WRR")){
         String waferKey = makeStdfWaferKey(record);
         if(waferKey != null){
            parent = waferParents.get(waferKey);
            waferParents.remove(waferKey);
         }
         if(waferParents.containsKey("LATEST"))
            waferParents.remove("LATEST");
      }
      else if(label.startsWith("PIR")){
         String unitKey = makeStdfUnitKey(record);
         if(unitKey != null)
            unitParents.put(unitKey, node);
         unitParents.put("LATEST", node);

         String waferKey = makeStdfWaferKey(record);
         if(waferKey != null && waferParents.containsKey(waferKey))
            parent = waferParents.get(waferKey);
      }
      else if(label.startsWith("PRR")){
         String unitKey = makeStdfUnitKey(record);
         if(unitKey != null){
            parent = unitParents.get(unitKey);
            unitParents.remove(unitKey);
         }
         if(unitParents.containsKey("LATEST"))
            unitParents.remove("LATEST");
      }
      else if(label.startsWith("DTR")){
         if(unitParents.containsKey("LATEST"))
            parent = unitParents.get("LATEST");
         else if(waferParents.containsKey("LATEST"))
            parent = waferParents.get("LATEST");
      }
      else{
         String key = makeStdfUnitKey(record);
         if(key != null && unitParents.containsKey(key))
            parent = unitParents.get(key);
         else{
            key = makeStdfWaferKey(record);
            if(key != null && waferParents.containsKey(key))
               parent = waferParents.get(key);
         }
      }

      treeModel.insertNodeInto(node, parent, parent.getChildCount());
   }

   private void processStdfRecords(STDFContainer stdf, boolean fromRoot,
                                   int fileId, ArrayList<Integer> fileIdList){
      HashMap<String, MutableTreeNode> waferParents =
         new HashMap<String, MutableTreeNode>();
      HashMap<String, MutableTreeNode> unitParents  =
         new HashMap<String, MutableTreeNode>();

      for(Record record : stdf){
         addStdfRecord(record, root, startDepth, fileId,
                       waferParents, unitParents);
      }
   }

   private void initStdfVariables(STDFContainer stdf){
      totalRecordCount += stdf.size();
   }

   private void initGlobals(){
      totalRecordCount = 0;
      recordCount      = 0;

      lotId            = "";
      totalUnits       = 0;

      root.removeAllChildren();
      treeModel.reload();
   }

   // files should have already been opened/added by now
   public void run(RecordTree tree, DefaultMutableTreeNode _root, boolean isBase) throws Exception{
      root      = _root;
      treeModel = (DefaultTreeModel)tree.getModel();

      //long startTime = System.currentTimeMillis();

      panel.enableMenus(false);

      if(getNumFiles() == 0){
         root.removeAllChildren();
         treeModel.reload();
         panel.setSelectedRecord(null, new TreeRecord());
         panel.setStatus("Status");
         panel.repaint();
      }
      else{
         int fileId = -1;

         ArrayList<Integer> fileIdList = new ArrayList<Integer>();

         if(isBase || root.getUserObject() instanceof String){
            Enumeration children = root.children();
            while(children.hasMoreElements()){
               DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.nextElement();
               Object nodeObject = node.getUserObject();
               if(nodeObject != null){
                  TreeRecord record = (TreeRecord)nodeObject;
                  fileIdList.add(new Integer(record.fileId));
               }
            }

            fileIdList.add(nextFileId++);

            if(isBase){
               initGlobals();

               for(STDFContainer container : stdfContainers)
                  initStdfVariables(container);
            }

            panel.repaint();
         }
         else{
            TreeRecord record = (TreeRecord)root.getUserObject();
            fileId = record.fileId;
         }

         for(STDFContainer container : stdfContainers)
            processStdfRecords(container, true, fileId, fileIdList);
      }

      panel.enableMenus(true);

      if(isBase){
         String recString = totalRecordCount + " record";
         if(totalRecordCount != 1)
            recString += "s";
         panel.setStatus(recString);
      }

      tree.treeDidChange();
      tree.expandCurrentPath();
   }

   public void setStartDepth(int newDepth){
      startDepth = newDepth;
   }

   public void setTreeRoot(DefaultMutableTreeNode node){
      root = node;
   }

   public void saveSTDFContainers(File file) throws IOException{
      STDFWriter writer = new STDFWriter(file);
      for(STDFContainer container : stdfContainers)
         writer.write(container);
      writer.close();
   }
}

// vim:ts=3:et:sw=3
