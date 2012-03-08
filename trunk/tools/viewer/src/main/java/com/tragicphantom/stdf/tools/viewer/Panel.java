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
import javax.swing.table.*;
import javax.swing.border.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

import java.util.Properties;

public class Panel extends JPanel implements SettingsProvider,
                                             PreferenceListener,
                                             RecordSelectionListener
{
   private static String VERSION = "0.0.0";

   public static final int DEFAULT_WIDTH  = 600;
   public static final int DEFAULT_HEIGHT = 700;

   private Window                pWindow     = null;
   private GraphicsConfiguration pWindowGC   = null;
   private Frame                 dialogOwner = null;

   private static int windowX = 0;
   private static int windowY = 0;

   private static int width  = DEFAULT_WIDTH;
   private static int height = DEFAULT_HEIGHT;

   private static int dividerLocation = 300;

   private MenuBar menuBar = null;

   private FileLoader loader = new FileLoader(this);

   private RecordTree  recordTree       = new RecordTree(this);
   private JScrollPane recordTreeScroll = new JScrollPane(recordTree);

   private DataTable    dataTable  = new DataTable();
   private JScrollPane  dataScroll = new JScrollPane(dataTable);

   private JPanel       dataPanel  = new JPanel();
   private TitledBorder dataBorder = null;

   private JFileChooser chooser = null;

   private FindDialog findDialog = null;

   private JPanel     centerPanel = new JPanel();
   private JSplitPane splitPane   = new JSplitPane();
   private JLabel     statusLabel = new JLabel();

   private Preferences preferences = new Preferences();

   private boolean expandToFirstUnit = DefaultSettings.EXPAND_FIRST_UNIT;

   private long selectedUnitId = -1;

   public Panel(){
      try{
         readVersion();
         initGui();
         setupFileChooser();

         recordTree.addRecordSelectionListener(this); // I should always be last

         Settings.addSettingsProvider(this);
         Settings.addSettingsProvider(dataTable);
         Settings.addSettingsProvider(loader);

         preferences.load();
         preferences.addPreferenceListener(dataTable);
         preferences.addPreferenceListener(loader);

         //we should be last listener so that it can process files if necessary
         preferences.addPreferenceListener(this);
      }
      catch(Exception e){
         e.printStackTrace();
      }
   }

   public void getSettings(){
      if(pWindow != null){
         width  = pWindow.getWidth();
         height = pWindow.getHeight();
         Rectangle bounds = pWindowGC.getBounds();
         Point location = pWindow.getLocation();
         windowX = (int)location.getX() - bounds.x;
         windowY = (int)location.getY() - bounds.y;
      }

      Settings.put("MainWindowWidth", new Integer(width));
      Settings.put("MainWindowHeight", new Integer(height));
      Settings.put("MainWindowX", new Integer(windowX));
      Settings.put("MainWindowY", new Integer(windowY));

      dividerLocation = splitPane.getDividerLocation();
      Settings.put("MainWindowDivider", new Integer(dividerLocation));

      Settings.put("expandToFirstUnit", new Boolean(expandToFirstUnit));
   }

   public void setSettings(){
      width = ((Integer)Settings.get("MainWindowWidth", width)).intValue();
      height = ((Integer)Settings.get("MainWindowHeight", height)).intValue();

      windowX = ((Integer)Settings.get("MainWindowX", windowX)).intValue();
      windowY = ((Integer)Settings.get("MainWindowY", windowY)).intValue();

      initWindow();

      dividerLocation = ((Integer)Settings.get("MainWindowDivider", dividerLocation)).intValue();
      splitPane.setDividerLocation(dividerLocation);

      expandToFirstUnit = ((Boolean)Settings.get("expandToFirstUnit", expandToFirstUnit)).booleanValue();
   }

   private void initWindow(){
      if(pWindow != null){
         pWindow.setSize(new Dimension(width, height));
         Rectangle bounds = pWindowGC.getBounds();
         pWindow.setLocation(windowX + bounds.x, windowY + bounds.y);
      }
   }

   public void preferencesChanged(Preferences preferences){
      expandToFirstUnit = preferences.getExpandToFirstUnit();
      processFiles();
   }

   public void addNotify(){
      Object parent = getTopLevelAncestor();

      if(parent != null && parent instanceof Window){
         dialogOwner = (Frame)parent;
         String title = ((JFrame)parent).getTitle();
         if(title.startsWith(Main.DEFAULT_TITLE)){
            pWindow = (Window)parent;
            pWindowGC = pWindow.getGraphicsConfiguration();
            pWindow.addWindowListener(new WindowAdapter(){
               public void windowClosing(WindowEvent e){
                  houseClean();
               }
            });
            initWindow();
         }
      }

      super.addNotify();
   }

   public void setArgs(String[] args){
      if(args.length > 0){
         ((Component)this).setCursor(new Cursor(Cursor.WAIT_CURSOR));

         setStatus("Loading...");

         for(String name : args){
            if(!loader.addFile(name)){
               setStatus("Error!");
               JOptionPane.showMessageDialog(this, "Invalid File!", "Error Reading File", JOptionPane.ERROR_MESSAGE);
            }
         }

         ((Component)this).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

         processFiles();
      }
   }

   private void setupFileChooser(){
      if(System.getProperties().getProperty("os.name").startsWith("Windows"))
         chooser = new JFileChooser(new WindowsAltFileSystemView());
      else
         chooser = new JFileChooser(System.getProperty("user.dir"));

      chooser.addChoosableFileFilter(new StdfFileFilter());
      //chooser.setAcceptAllFileFilterUsed(false);
   }

   private void setupSplitPanes(){
      splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

      splitPane.add(recordTreeScroll, JSplitPane.TOP);
      splitPane.add(dataPanel       , JSplitPane.BOTTOM);

      splitPane.setDividerLocation(dividerLocation);

      centerPanel.setLayout(new BorderLayout());
      centerPanel.add(splitPane, BorderLayout.CENTER);
   }

   private void readVersion(){
      try{
         BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/VERSION")));
         VERSION = reader.readLine();
         reader.close();
      }
      catch(Exception e){
         e.printStackTrace();
      }
   }

   private void initGui() throws Exception{
      setLayout(new BorderLayout());

      dataBorder = new TitledBorder(BorderFactory.createLineBorder(new Color(153, 153, 153), 2), "Data");
      dataPanel.setBorder(dataBorder);
      dataPanel.setLayout(new BorderLayout());
      dataPanel.add(dataScroll, BorderLayout.CENTER);

      setStatus("Status");

      setupSplitPanes();

      add(centerPanel, BorderLayout.CENTER);
      add(statusLabel, BorderLayout.SOUTH);
   }

   public void setSelectedRecord(RecordTree tree, TreeRecord record){
      if(record.id < 0){
         dataBorder.setTitle("Data");
         dataTable.setSelectedRecord(tree, record);
         selectedUnitId = -1;
      }
      else{
         dataBorder.setTitle(record.typeName);
         dataTable.setSelectedRecord(tree, record);

         recordTree.setFocus(true);
         selectedUnitId = -1;
      }

      dataPanel.repaint();
   }

   public void preferencesAction(){
      preferences.show();
   }

   public void quitAction(){
      houseClean();
      System.exit(0);
   }

   public void openFile(){
      try{
         int returnVal = chooser.showOpenDialog(this);

         if(returnVal == JFileChooser.APPROVE_OPTION){
            String name = chooser.getSelectedFile().getPath();
            ((Component)this).setCursor(new Cursor(Cursor.WAIT_CURSOR));

            loader.closeFiles();

            setStatus("Loading...");

            boolean loaded = loader.addFile(name);

            ((Component)this).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            if(!loaded){
               setStatus("Error!");
               JOptionPane.showMessageDialog(this, "Invalid File!", "Error Reading File", JOptionPane.ERROR_MESSAGE);
            }

            processFiles();

            if(!loaded){
               enableMenus(false);
               setStatus("Status");
            }
         }
      }
      catch(Exception e){
      }
   }

   public void addFile(){
      try{
         int returnVal = chooser.showOpenDialog(this);

         if(returnVal == JFileChooser.APPROVE_OPTION){
            String name = chooser.getSelectedFile().getPath();
            ((Component)this).setCursor(new Cursor(Cursor.WAIT_CURSOR));
            setStatus("Loading...");
            boolean loaded = loader.addFile(name);
            ((Component)this).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            if(!loaded){
               setStatus("Error!");
               JOptionPane.showMessageDialog(this, "Invalid File!", "Error Reading File", JOptionPane.ERROR_MESSAGE);
            }
            else
               processFiles();
         }
      }
      catch(Exception e){
      }
   }

   private File getSaveFile(){
      if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
         return chooser.getSelectedFile();
      else
         return null;
   }

   private boolean verifyOverwrite(){
      return JOptionPane.showConfirmDialog(this, "Overwrite existing file?",
                                           "File Exists", JOptionPane.YES_NO_OPTION)
             == JOptionPane.YES_OPTION;
   }

   public void saveFile(){
      File file = getSaveFile();
      while(file != null && file.exists() && !verifyOverwrite())
         file = getSaveFile();

      if(file != null){
         try{
            loader.saveSTDFContainers(file);
         }
         catch(Exception e){
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Writing File", JOptionPane.ERROR_MESSAGE);
         }
      }
   }

   public void closeAllAction(){
      loader.closeFiles();
      processFiles();
      enableMenus(false);
      setStatus("Status");
   }

   public void processFiles(){
      if(loader.getNumFiles() > 0){
         setStatus("Processing...");
         setCursor(new Cursor(Cursor.WAIT_CURSOR));

         try{
            loader.setStartDepth(1);
            loader.run(recordTree, recordTree.getRoot(), true);

            if(expandToFirstUnit){
               find("^Unit|PIR");
               refresh();
            }
         }
         catch(Exception e){
            setStatus("Error!");
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error Reading File", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
         }

         setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

         if(pWindow != null){
            if(loader.getNumFiles() == 1)
               ((JFrame)pWindow).setTitle(Main.DEFAULT_TITLE + " - " + loader.getFileName(0));
            else
               ((JFrame)pWindow).setTitle(Main.DEFAULT_TITLE);
         }
      }
      else{
         recordTree.clear();
         selectedUnitId = -1;

         if(pWindow != null)
            ((JFrame)pWindow).setTitle(Main.DEFAULT_TITLE);
      }
   }

   public void processPartial(RecordTree tree, DefaultMutableTreeNode node, TreeRecord record){
      try{
         setCursor(new Cursor(Cursor.WAIT_CURSOR));
         loader.run(tree, node, false);
         setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
      catch(Exception e){
         e.printStackTrace();
      }
   }

   public void datalogAction(){
      new DatalogDialog(dialogOwner).show();
   }

   public void collapseTreeAction(){
      recordTree.collapseTree();
      selectedUnitId = -1;
   }

   public void collapseCurrentNode(){
      recordTree.collapseCurrentNode();
      selectedUnitId = -1;
   }

   public void refresh(){
      recordTree.repaint();
   }

   protected void finalize(){
      houseClean();
   }

   public void houseClean(){
      loader.closeFiles();
      processFiles();
      preferences.save();
   }

   public void aboutAction(){
      Runtime rt = Runtime.getRuntime();

      StringBuffer sb = new StringBuffer();

      sb.append("STDF Viewer v").append(VERSION)
        .append("\n\nTotal Memory: ").append(formatMemValue(rt.totalMemory()))
        .append("\nFree Memory: ").append(formatMemValue(rt.freeMemory()))
        .append("\nMax Memory: ").append(formatMemValue(rt.maxMemory()));

      JOptionPane.showMessageDialog(this, sb.toString(), "STDF Viewer", JOptionPane.PLAIN_MESSAGE);
   }

   private String formatMemValue(long value){
      String type  = "B";

      if(value > 1024){
         value /= 1024;
         if(value > 1024){
            value /= 1024;
            if(value > 1024){
               value /= 1024;
               type   = "G";
            }
            else
               type = "M";
         }
         else
            type = "K";
      }

      return Long.toString(value) + type;
   }

   public void setStatus(String statString){
      statusLabel.setText(statString);
   }

   private void find(String record){
      if(findDialog == null)
         findDialog = new FindDialog(dialogOwner, new RecordTree[]{ recordTree });
      findDialog.findRecord(record);
   }

   public void findNextAction(){
      if(findDialog == null)
         findDialog = new FindDialog(dialogOwner, new RecordTree[]{ recordTree });
      findDialog.show();
   }

   public void setMenuBar(MenuBar menuBar){
      this.menuBar = menuBar;
   }

   public void enableMenus(boolean set){
      if(menuBar != null)
         menuBar.enableMenus(set);
   }
}
