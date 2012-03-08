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
import javax.swing.event.*;

public class MenuBar extends JMenuBar implements ActionListener{
   private JMenu fileMenu = new JMenu("File");
   private JMenu viewMenu = new JMenu("View");
   private JMenu helpMenu = new JMenu("Help");

   private JMenuItem openItem         = new JMenuItem("Open...");
   private JMenuItem saveAsItem       = new JMenuItem("Save As...");
   private JMenuItem closeAllItem     = new JMenuItem("Close All");
   private JMenuItem reloadItem       = new JMenuItem("Reload Files");
   private JMenuItem prefItem         = new JMenuItem("Preferences");
   private JMenuItem quitItem         = new JMenuItem("Quit");

   private JMenuItem collapseTreeItem = new JMenuItem("Collapse Tree");
   private JMenuItem collapseNodeItem = new JMenuItem("Collapse Current Node");
   private JMenuItem refreshItem      = new JMenuItem("Refresh");

   private JMenuItem datalogItem      = new JMenuItem("View Datalog");
   private JMenuItem findItem         = new JMenuItem("Find Next...");

   private JMenuItem aboutItem        = new JMenuItem("About");

   private Panel panel = null;

   public MenuBar(Panel panel){
      super();

      this.panel = panel;

      setupMenus();

      panel.setMenuBar(this);
   }

   public void actionPerformed(ActionEvent e){
      Object source = e.getSource();
      if(source == openItem)
         panel.openFile();
      else if(source == saveAsItem)
         panel.saveFile();
      else if(source == closeAllItem)
         panel.closeAllAction();
      else if(source == reloadItem)
         panel.processFiles();
      else if(source == prefItem)
         panel.preferencesAction();
      else if(source == quitItem)
         panel.quitAction();
      else if(source == collapseNodeItem)
         panel.collapseCurrentNode();
      else if(source == collapseTreeItem)
         panel.collapseTreeAction();
      else if(source == refreshItem)
         panel.refresh();
      else if(source == datalogItem)
         panel.datalogAction();
      else if(source == findItem)
         panel.findNextAction();
      else if(source == aboutItem)
         panel.aboutAction();
   }

   private void setupMenus(){
      fileMenu.setMnemonic('F');

      viewMenu.setEnabled(false);
      viewMenu.setMnemonic('V');

      helpMenu.setMnemonic('H');

      openItem.setMnemonic('O');
      openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK, false));
      openItem.addActionListener(this);

      saveAsItem.setMnemonic('S');
      saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK, false));
      saveAsItem.addActionListener(this);

      closeAllItem.setEnabled(false);
      closeAllItem.setMnemonic('C');
      closeAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_MASK, false));
      closeAllItem.addActionListener(this);

      reloadItem.setMnemonic('R');
      reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK, false));
      reloadItem.addActionListener(this);

      prefItem.setMnemonic('P');
      prefItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK, false));
      prefItem.addActionListener(this);

      quitItem.setMnemonic('Q');
      quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK, false));
      quitItem.addActionListener(this);

      fileMenu.add(openItem);
      fileMenu.add(saveAsItem);
      fileMenu.add(closeAllItem);
      fileMenu.addSeparator();
      fileMenu.add(reloadItem);
      fileMenu.addSeparator();
      fileMenu.add(prefItem);
      fileMenu.addSeparator();
      fileMenu.add(quitItem);

      collapseTreeItem.setMnemonic('C');
      collapseTreeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK, false));
      collapseTreeItem.addActionListener(this);

      collapseNodeItem.setMnemonic('O');
      collapseNodeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_MASK, false));
      collapseNodeItem.addActionListener(this);

      refreshItem.setMnemonic('R');
      refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false));
      refreshItem.addActionListener(this);

      datalogItem.setMnemonic('D');
      datalogItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_MASK, false));
      datalogItem.addActionListener(this);

      findItem.setMnemonic('F');
      findItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false));
      findItem.addActionListener(this);

      viewMenu.add(datalogItem);
      viewMenu.addSeparator();
      viewMenu.add(collapseTreeItem);
      viewMenu.add(collapseNodeItem);
      viewMenu.addSeparator();
      viewMenu.add(refreshItem);
      viewMenu.addSeparator();
      viewMenu.add(findItem);

      aboutItem.setMnemonic('A');
      aboutItem.addActionListener(this);

      helpMenu.add(aboutItem);

      add(fileMenu);
      add(viewMenu);
      add(helpMenu);
   }

   public void enableMenus(boolean enable){
      viewMenu.setEnabled(enable);
      closeAllItem.setEnabled(enable);
   }

   public JMenuItem getQuitMenuItem(){
      return quitItem;
   }
}
