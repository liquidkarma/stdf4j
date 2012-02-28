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
package com.tragicphantom.stdf.tools.viewer;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.Set;
import java.util.Enumeration;
import java.util.ArrayList;

import com.tragicphantom.stdf.RecordData;

public class FindThread extends Thread{
   private FindDialog pDialog = null;

   private RecordTree [] trees = null;

   private Pattern expr1 = null;
   private Pattern expr2 = null;

   private boolean showTraversal     = false;
   private boolean showNoDataMessage = true;

   private boolean isRunning = false;

   public FindThread(FindDialog dialog, RecordTree [] _trees, boolean _showTraversal, String value){
      super();
      init(dialog, _trees, _showTraversal);

      try{
         expr1 = Pattern.compile(value);
         expr2 = null;
      }
      catch(PatternSyntaxException e){
         expr1 = expr2 = null;
         JOptionPane.showMessageDialog(null, e.getMessage(), "Invalid Regular Expression", JOptionPane.ERROR_MESSAGE);
      }
   }

   public FindThread(FindDialog dialog, RecordTree [] _trees, boolean _showTraversal, String name, String value){
      super();
      init(dialog, _trees, _showTraversal);

      try{
         expr1 = Pattern.compile(name);
         expr2 = Pattern.compile(value);
      }
      catch(PatternSyntaxException e){
         expr1 = expr2 = null;
         JOptionPane.showMessageDialog(null, e.getMessage(), "Invalid Regular Expression", JOptionPane.ERROR_MESSAGE);
      }
   }

   private void init(FindDialog dialog, RecordTree [] _trees, boolean _showTraversal){
      pDialog       = dialog;
      trees         = _trees;
      showTraversal = _showTraversal;
   }

   private TreeRecord getValueRecord(RecordTree tree,
                                     DefaultMutableTreeNode node){
      TreeRecord record = (TreeRecord)node.getUserObject();
      String nodeValue = record.typeName;

      //System.err.println("Checking node: " + nodeValue);

      if(showTraversal)
         tree.scrollToRecord(record);

      if(nodeValue == RecordTree.LOAD_CONSTANT){
         DefaultMutableTreeNode pNode = (DefaultMutableTreeNode)node.getParent();
         tree.loadPath(new TreePath(pNode.getPath()));

         return searchNodes(tree, pNode.children());
      }
      else{
         if(expr2 == null){
            if(expr1.matcher(nodeValue).find())
               return record;
         }
         else{
            if(record.getStdfRecord() != null){
               RecordData data;
               try{
                  data = record.getStdfRecord().getData();
               }
               catch(Exception e){
                  throw new RuntimeException(e);
               }

               Set<String> fieldNames = data.getFields().keySet();
               for(String fieldName : fieldNames){
                  if(expr1.matcher(fieldName).find()){
                     if(expr2 == null ||
                        expr2.matcher(data.getField(fieldName).toString()).find())
                        return record;
                  }
               }
            }
         }

         return null;
      }
   }

   private void addEnumeration(ArrayList v, Enumeration e){
      while(e.hasMoreElements())
         v.add(e.nextElement());
   }

   private TreeRecord searchNodes(RecordTree tree, Enumeration enumeration){
      ArrayList nodes = new ArrayList();
      addEnumeration(nodes, enumeration);
      return searchNodes(tree, nodes);
   }

   private TreeRecord searchNodes(RecordTree tree, ArrayList nodes){
      TreeRecord record = null;
      int       size   = nodes.size();

      for(int i = 0; i < size && isRunning && record == null; i++){
         DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)nodes.get(i);
         record = getValueRecord(tree, currentNode);
         if(record == null && isRunning){
            addEnumeration(nodes, currentNode.children());
            size = nodes.size();
         }
      }

      return record;
   }

   private TreeRecord checkSiblings(RecordTree tree, DefaultMutableTreeNode node){
      TreeRecord record = null;

      if(node != null){
         node = node.getNextSibling();
         while(record == null && node != null && isRunning){
            record = getValueRecord(tree, node);
            if(record == null){
               record = searchNodes(tree, node.children());
               if(record == null)
                  node = node.getNextSibling();
            }
         }
      }

      return record;
   }

   private boolean findExpression(){
      TreeRecord record = null;

      for(RecordTree tree : trees){
         DefaultMutableTreeNode node = tree.getTreeSelectedNode();
         boolean startedFromTop = false;

         if(node == null){
            node = tree.getRoot();
            startedFromTop = true;
         }

         if(isRunning)
            record = searchNodes(tree, node.children());

         if(record == null && isRunning){
            record = checkSiblings(tree, node);

            if(record == null && isRunning){
               record = checkSiblings(tree, (DefaultMutableTreeNode)node.getParent());

               if(record != null && isRunning && !startedFromTop){
                  int option = JOptionPane.showConfirmDialog(null, "No data found... Continue from top?", "Continue?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                  if(option == JOptionPane.YES_OPTION)
                     record = searchNodes(tree, tree.getRoot().children());
                  else
                     showNoDataMessage = false;
               }
            }
         }

         if(record != null){
            tree.scrollToRecord(record);
            break;
         }
         else if(!isRunning)
            break;
      }

      return record != null;
   }

   public void run(){
      if(expr1 != null){
         pDialog.setState(isRunning = true);

         if(!findExpression() && showNoDataMessage)
            JOptionPane.showMessageDialog(null, "No Data Found", "Results", JOptionPane.ERROR_MESSAGE);

         pDialog.setState(isRunning = false);
      }
   }

   public void kill(){
      isRunning = false;
   }
}
