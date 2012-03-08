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

import java.awt.*;
import java.awt.event.*;

public class FindDialog extends JDialog implements ActionListener, FocusListener{
   private RecordTree [] trees = null;

   private JButton findButton   = new JButton("Find Next");
   private JButton cancelButton = new JButton("Cancel");

   private JTextField recordText = new JTextField();
   private JTextField fieldText  = new JTextField();
   private JTextField valueText  = new JTextField();

   private JRadioButton recordRadio = new JRadioButton("Find Record:");
   private JRadioButton fieldRadio  = new JRadioButton("Find Field:");
   private ButtonGroup  buttonGroup = new ButtonGroup();

   private JCheckBox showFindCheckBox = new JCheckBox("Show Tree Traversal");

   private FindThread findThread = null;

   public FindDialog(Frame dialogOwner, RecordTree [] _trees){
      super(dialogOwner, "Find", false);

      trees = _trees;

      setSize(new Dimension(400, 150));

      recordText.setPreferredSize(new Dimension(160, 23));
      recordText.addActionListener(this);
      recordText.addFocusListener(this);

      fieldText.setPreferredSize(new Dimension(100, 23));
      fieldText.addActionListener(this);
      fieldText.addFocusListener(this);

      valueText.setPreferredSize(new Dimension(100, 23));
      valueText.addActionListener(this);
      valueText.addFocusListener(this);

      JPanel panel = new JPanel();
      panel.setLayout(new GridLayout(3, 1));

      recordRadio.setSelected(true);
      buttonGroup.add(fieldRadio);
      buttonGroup.add(recordRadio);

      JPanel subPanel = new JPanel();
      subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      subPanel.add(recordRadio);
      subPanel.add(recordText, null);
      panel.add(subPanel);

      subPanel = new JPanel();
      subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      subPanel.add(fieldRadio);
      subPanel.add(fieldText);
      subPanel.add(new JLabel("Value:"));
      subPanel.add(valueText);
      panel.add(subPanel);

      //subPanel = new JPanel();
      //subPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      //showFindCheckBox.setSelected(true);
      //subPanel.add(showFindCheckBox);
      //panel.add(subPanel);

      getContentPane().add(panel, BorderLayout.CENTER);

      panel = new JPanel();
      findButton.addActionListener(this);
      panel.add(findButton, null);
      cancelButton.addActionListener(this);
      panel.add(cancelButton, null);
      getContentPane().add(panel, BorderLayout.SOUTH);
   }

   public void show(){
      super.show();
      recordText.requestFocusInWindow();
   }

   public void actionPerformed(ActionEvent e){
      Object source = e.getSource();
      if(source == cancelButton){
         if(findThread != null){
            findThread.kill();
/*
            try{
               findThread.join();
            }
            catch(Exception ex){
               ex.printStackTrace();
            }
*/
            findThread = null;
         }
         setVisible(false);
      }
      else if(source == findButton){
         if(recordRadio.isSelected())
            findRecord(recordText.getText());
         else //fieldRadio.isSelected()
            findField(fieldText.getText(), valueText.getText());
      }
      else if(source == recordText || source == fieldText || source == valueText)
         findButton.doClick();
   }

   public void focusGained(FocusEvent e){
      Object source = e.getSource();
      if(source == recordText)
         recordRadio.setSelected(true);
      else if(source == fieldText || source == valueText)
         fieldRadio.setSelected(true);
   }

   public void focusLost(FocusEvent e){
   }

   public void findRecord(String value){
      boolean showTraversal = false; //showFindCheckBox.isSelected();
      findThread = new FindThread(this, trees, showTraversal, value);
      findThread.start();
   }

   public void findField(String name, String value){
      boolean showTraversal = false; //showFindCheckBox.isSelected();
      findThread = new FindThread(this, trees, showTraversal, name, value);
      findThread.start();
   }

   public void setState(boolean isRunning){
      if(isRunning){
         findButton.setEnabled(false);
         ((Component)this).setCursor(new Cursor(Cursor.WAIT_CURSOR));
      }
      else{
         findButton.setEnabled(true);
         ((Component)this).setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
   }
}
