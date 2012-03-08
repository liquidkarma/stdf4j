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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import java.util.ArrayList;
import java.util.Enumeration;

import com.tragicphantom.stdf.Record;

public class RecordTree extends JTree implements TreeSelectionListener,
                                                 TreeWillExpandListener
{
   private DefaultMutableTreeNode  root         = new DefaultMutableTreeNode("Data");
   private DefaultTreeModel        treeModel    = new DefaultTreeModel(root);
   private DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();

   private Color focusBackgroundSelectionColor = cellRenderer.getBackgroundSelectionColor();
   private Color blurBackgroundSelectionColor  = Color.LIGHT_GRAY; //cellRenderer.getBackgroundNonSelectionColor();

   private Color focusTextSelectionColor = cellRenderer.getTextSelectionColor();
   private Color blurTextSelectionColor  = cellRenderer.getTextNonSelectionColor();

   private ArrayList<RecordSelectionListener> listeners = new ArrayList<RecordSelectionListener>();

   public static final String LOAD_CONSTANT = "_load_";

   private Panel panel = null;

   private boolean focus = true;

   public RecordTree(Panel panel){
      super();

      this.panel = panel;

      init();
   }

   private void init(){
      setModel(treeModel);
      setRootVisible(false);
      getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      setCellRenderer(cellRenderer);

      addTreeSelectionListener(this);
      addTreeWillExpandListener(this);
   }

   public void treeWillExpand(TreeExpansionEvent e){
      loadPath(e.getPath());
   }

   public void treeWillCollapse(TreeExpansionEvent e){
   }

   public void loadPath(TreePath path){
      //System.err.println("Tree expanding: " + path);

      //call setSelectionPath twice because first call may have to expand to path
      //leaving selection at parent node for path requested
      setSelectionPath(path);
      setSelectionPath(path);

      DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();

      if(node != null){
         boolean needLoad = false;
         for(Enumeration i = node.children(); i.hasMoreElements(); ){
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)i.nextElement();
            if(child.toString().equals(LOAD_CONSTANT)){
               child.removeFromParent();
               needLoad = true;
            }
         }

         if(needLoad){
            TreeRecord record = (TreeRecord)node.getUserObject();
            panel.processPartial(this, node, record);
         }
      }
   }

   public void valueChanged(TreeSelectionEvent e){
      if(FileLoader.getNumFiles() == 0)
         broadcastSelectedRecord(new TreeRecord());
      else{
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
         if(node == null || node == root)
            broadcastSelectedRecord(new TreeRecord());
         else
            broadcastSelectedRecord((TreeRecord)node.getUserObject());
      }
   }

   public TreeModel getModel(){
      return treeModel;
   }

   public DefaultMutableTreeNode getRoot(){
      return root;
   }

   public DefaultMutableTreeNode getTreeSelectedNode(){
      return (DefaultMutableTreeNode)getLastSelectedPathComponent();
   }

   public int getCurrentFileIndex(){
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
      if(node != null && node != root){
         Object nodeObject = node.getUserObject();
         if(nodeObject != null){
            TreeRecord record = (TreeRecord)nodeObject;
            //System.err.println("id => " + record.id);
            return record.fileId;
         }
      }

      // find children of root
      for(Enumeration i = root.children(); i.hasMoreElements(); )
         node = (DefaultMutableTreeNode)i.nextElement();

      if(node != null){
         Object nodeObject = node.getUserObject();
         if(nodeObject != null){
            TreeRecord record = (TreeRecord)nodeObject;
            //System.err.println("id => " + record.id);
            return record.fileId;
         }
      }

      return -1;
   }

   private void collapseTree(DefaultMutableTreeNode node){
      Enumeration children = node.children();
      while(children.hasMoreElements())
         collapseTree((DefaultMutableTreeNode)children.nextElement());

      collapsePath(new TreePath(node.getPath()));
   }

   public void collapseTree(){
      Enumeration children = root.children();
      while(children.hasMoreElements())
         collapseTree((DefaultMutableTreeNode)children.nextElement());
   }

   public void collapseCurrentNode(){
      TreePath path = getSelectionPath();
      if(path != null){
         path = path.getParentPath();
         if(!path.equals(new TreePath(root.getPath()))){
            collapsePath(path);
            scrollToPath(path);
         }
      }
   }

   private void scrollToNode(DefaultMutableTreeNode node){
      scrollToPath(new TreePath(node.getPath()));
   }

   private void scrollToPath(TreePath path){
      makeVisible(path);
      setSelectionPath(path);
      scrollPathToVisible(path);
   }

   public void scrollToFirstNode(){
      Enumeration children = root.children();
      if(children.hasMoreElements())
         scrollToNode((DefaultMutableTreeNode)children.nextElement());
   }

   public void scrollToRecord(TreeRecord record){
      scrollToRecordId(record.id);
   }

   private void scrollToRecordId(long id){
      try{
         setExpandsSelectedPaths(true);
      }
      catch(Exception e){
      }

      ArrayList children = new ArrayList();

      for(Enumeration i = root.children(); i.hasMoreElements();)
         children.add(i.nextElement());

      int numChildren = children.size();
      boolean found = false;

      while(numChildren > 0){
         ArrayList newChildren = new ArrayList();

         for(int i = 0; i < numChildren; i++){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.get(i);
            TreeRecord record = (TreeRecord)node.getUserObject();
            if(record.id == id){
               scrollToNode(node);
               found = true;
               break;
            }

            for(Enumeration child = node.children(); child.hasMoreElements();)
               newChildren.add(child.nextElement());
         }

         if(found)
            break;

         children    = newChildren;
         numChildren = children.size();
      }

      if(!found)
         JOptionPane.showMessageDialog(this, "Requested unit id was not found", "STDFView Error", JOptionPane.ERROR_MESSAGE);
   }

   public void expandCurrentPath(){
      TreePath path = new TreePath(root.getPath());
      expandPath(path);
   }

   public void addRecordSelectionListener(RecordSelectionListener listener){
      listeners.add(listener);
   }

   private void broadcastSelectedRecord(TreeRecord record){
      for(RecordSelectionListener listener : listeners)
         listener.setSelectedRecord(this, record);
   }

   public void clear(){
      root.removeAllChildren();
      treeModel.reload();
   }

   public void setFocus(boolean _focus){
      if(focus != _focus){
         focus = _focus;
         if(focus){
            cellRenderer.setBackgroundSelectionColor(focusBackgroundSelectionColor);
            cellRenderer.setTextSelectionColor(focusTextSelectionColor);
         }
         else{
            cellRenderer.setBackgroundSelectionColor(blurBackgroundSelectionColor);
            cellRenderer.setTextSelectionColor(blurTextSelectionColor);
         }
         repaint();
      }
   }
}
