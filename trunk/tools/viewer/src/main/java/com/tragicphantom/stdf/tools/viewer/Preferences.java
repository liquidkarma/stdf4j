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

import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

public class Preferences extends JFrame implements ActionListener, ItemListener{
   private boolean dirty = false;

   private JButton okButton     = new JButton("OK");
   private JButton cancelButton = new JButton("Cancel");
   private JButton applyButton  = new JButton("Apply");

   private JCheckBox allFieldsButton = new JCheckBox("Show All Fields");
   private JCheckBox expandButton    = new JCheckBox("Expand to First Unit on Open");
   private JCheckBox showGroups      = new JCheckBox("Group related record types");

   private Vector<PreferenceListener> preferenceListeners = new Vector<PreferenceListener>();

   public Preferences(){
      setTitle("Preferences");
      setSize(new Dimension(300, 200));

      JPanel panel = new JPanel();
      okButton.addActionListener(this);
      panel.add(okButton);
      cancelButton.addActionListener(this);
      panel.add(cancelButton);
      applyButton.addActionListener(this);
      applyButton.setEnabled(false);
      panel.add(applyButton);
      getContentPane().add(panel, BorderLayout.SOUTH);

      JTabbedPane tabbedPane = new JTabbedPane();

      JCheckBox [] preferenceItems = new JCheckBox[]{
         allFieldsButton,
         expandButton,
         showGroups
      };

      panel = new JPanel();
      panel.setLayout(new GridLayout(preferenceItems.length, 1));
      panel.setBorder(BorderFactory.createTitledBorder("View"));
      for(JCheckBox item : preferenceItems)
         addCheckBox(item, panel);
      tabbedPane.addTab("General", panel);

      getContentPane().add(tabbedPane, BorderLayout.CENTER);
   }

   private void addCheckBox(JCheckBox checkBox, JPanel panel){
      checkBox.addItemListener(this);
      panel.add(checkBox);
   }

   public void actionPerformed(ActionEvent e){
      Object source = e.getSource();
      if(source.equals(okButton)){
         applySettings();
         hide();
      }
      else if(source.equals(cancelButton)){
         resetSettings();
         hide();
      }
      else if(source.equals(applyButton))
         applySettings();
   }

   public void itemStateChanged(ItemEvent e){
      Object source = e.getItemSelectable();
      if(source == allFieldsButton
            || source == expandButton
            || source == showGroups)
         setDirty(true);
   }

   private void setDirty(boolean value){
      dirty = value;

      applyButton.setEnabled(dirty);
   }

   private void applySettings(){
      broadcastPreferencesChanged();

      setDirty(false);
   }

   private void broadcastPreferencesChanged(){
      for(PreferenceListener listener : preferenceListeners)
         listener.preferencesChanged(this);
   }

   public void addPreferenceListener(PreferenceListener listener){
      preferenceListeners.addElement(listener);
   }

   private void resetSettings(){
      allFieldsButton.setSelected(((Boolean)Settings.get("allFields", DefaultSettings.SHOW_ALL_FIELDS)).booleanValue());
      expandButton.setSelected(((Boolean)Settings.get("expandToFirstUnit", DefaultSettings.EXPAND_FIRST_UNIT)).booleanValue());
      showGroups.setSelected(((Boolean)Settings.get("showGroups", DefaultSettings.GROUP_RECORDS)).booleanValue());

      setDirty(false);
   }

   public boolean getAllFields(){
      return allFieldsButton.isSelected();
   }

   public boolean getExpandToFirstUnit(){
      return expandButton.isSelected();
   }

   public boolean getShowGroups(){
      return showGroups.isSelected();
   }

   public void save(){
      Settings.save();
   }

   public void load(){
      Settings.restore();
      resetSettings();
   }
}
