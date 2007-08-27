/*
 * org.openmicroscopy.shoola.agents.measurement.view.ObjectInspector 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.measurement.view;


//Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

//Third-party libraries
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.measurement.util.AttributeField;
import org.openmicroscopy.shoola.agents.measurement.util.FigureTable;
import org.openmicroscopy.shoola.agents.measurement.util.FigureTableModel;
import org.openmicroscopy.shoola.agents.measurement.util.ROITableCellRenderer;
import org.openmicroscopy.shoola.agents.measurement.util.ValueType;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;
import org.openmicroscopy.shoola.util.ui.drawingtools.attributes.DrawingAttributes;


/** 
 * UI Component displaying various drawing information about a 
 * Region of Interest.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class ObjectInspector 
	extends JPanel
{

	/** Collection of column names. */
	private static List<String>			columnNames;
	
	/** The name of the panel. */
	private static final String			NAME = "Inspector";
	
	/** The table hosting the various fields. */
	private FigureTable					fieldTable;

	/** Reference to the control. */
	private MeasurementViewerControl	controller;
	
	/** Reference to the model. */
	private MeasurementViewerModel		model;

	static {
		columnNames = new ArrayList<String>(2);
		columnNames.add("Field");
		columnNames.add("Value");
	}
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		List<AttributeField> l = new ArrayList<AttributeField>();
		l.add(new AttributeField(AttributeKeys.TEXT, "Text", true));
		l.add(new AttributeField(DrawingAttributes.SHOWTEXT, "Show Text", 
				false));
		l.add(new AttributeField(MeasurementAttributes.SHOWMEASUREMENT, 
					"Show Measurements", 
				false)); 

		l.add(new AttributeField(AttributeKeys.STROKE_WIDTH, "Line Width", 
				true, strokeRange(), ValueType.ENUM));
		l.add(new AttributeField(AttributeKeys.FONT_SIZE, "Font Size", 
				true, fontRange(), ValueType.ENUM));
		l.add(new AttributeField(AttributeKeys.TEXT_COLOR, "Font Colour", 
				false));
		l.add(new AttributeField(AttributeKeys.FILL_COLOR, "Fill Colour", 
				false));
		l.add(new AttributeField(AttributeKeys.STROKE_COLOR, "Line Colour", 
				false));
		l.add(new AttributeField(MeasurementAttributes.MEASUREMENTTEXT_COLOUR, 
				"Measurement Text Colour", false));
		
		//create the table
		fieldTable = new FigureTable(new FigureTableModel(l, columnNames));
		fieldTable.getTableHeader().setReorderingAllowed(false);
		fieldTable.setRowHeight(25);
		fieldTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fieldTable.setCellSelectionEnabled(true);
		fieldTable.setColumnSelectionAllowed(true);
		
		fieldTable.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) {
				
				if (e.getClickCount() == 1) {
					int col = fieldTable.getSelectedColumn();
					int row = fieldTable.getSelectedRow();
					Object value = fieldTable.getValueAt(row, col);
					if (value instanceof Boolean) toggleValue();
					
				} else if (e.getClickCount() > 1) {
					e.consume();
					int col = fieldTable.getSelectedColumn();
					int row = fieldTable.getSelectedRow();
					Object value = fieldTable.getValueAt(row, col);
					if (value instanceof Color)
						controller.showColorPicker((Color) value);
					if (value instanceof Boolean)
						toggleValue();
				}
			}
		});

	}
	
	/**
	 * Set the range of values the stroke attribute may take. 
	 * @return see above.
	 */
	ArrayList<Double> strokeRange()
	{
		ArrayList<Double> sRange = new ArrayList<Double>();
		sRange.add(new Double(1));
		sRange.add(new Double(2));
		sRange.add(new Double(3));
		sRange.add(new Double(4));
		return sRange;
	}
	
	/**
	 * Set the range of values the font attribute may take. 
	 * @return see above.
	 */
	ArrayList<Double> fontRange()
	{
		ArrayList<Double> fRange = new ArrayList<Double>();
		fRange.add(new Double(8));
		fRange.add(new Double(9));
		fRange.add(new Double(10));
		fRange.add(new Double(12));
		fRange.add(new Double(14));
		return fRange;
	}
	
	/** Toggles the value of the boolean under the current selection. */
	private void toggleValue()
	{
		int col = fieldTable.getSelectedColumn();
		int row = fieldTable.getSelectedRow();
		Boolean value = (Boolean)fieldTable.getModel().getValueAt(row, col);
		boolean newValue = !(value.booleanValue());
		fieldTable.getModel().setValueAt(new Boolean(newValue), row, col);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout());
		add(new JScrollPane(fieldTable), BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the Control. Mustn't be <code>null</code>.
	 * @param model		 Reference to the Model. Mustn't be <code>null</code>.
	 */
	ObjectInspector(MeasurementViewerControl controller, 
					MeasurementViewerModel model)
	{
		if (controller == null)
			throw new IllegalArgumentException("No control.");
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.controller = controller;
		this.model = model;
		initComponents();
		buildGUI();
	}

	/**
	 * Returns the name of the component.
	 * 
	 * @return See above.
	 */
	String getComponentName() { return NAME; }
	
	/**
	 * Returns the icon of the component.
	 * 
	 * @return See above.
	 */
	Icon getComponentIcon()
	{
		IconManager icons = IconManager.getInstance();
		return icons.getIcon(IconManager.INSPECTOR);
	}
	
	/**
	 * Sets the passed color to the currently selected cell.
	 * 
	 * @param c The color to set.
	 */
	void setCellColor(Color c)
	{
		int col = fieldTable.getSelectedColumn();
		int row = fieldTable.getSelectedRow();
		fieldTable.getModel().setValueAt(c, row, col);
	}

	/**
	 * Sets the data.
	 * 
	 * @param figure The data to set.
	 */
	void setModelData(Figure figure)
	{
		FigureTableModel tableModel = (FigureTableModel) fieldTable.getModel();
		tableModel.setData(figure);
		fieldTable.setModel(tableModel);
		fieldTable.repaint();
	}
	
	/**
	 * Sets the new figure retrieved from the passed collection.
	 * 
	 * @param l The collection to handle.
	 */
	void setSelectedFigures(Collection l)
	{
		FigureTableModel tableModel = (FigureTableModel) fieldTable.getModel();
		Iterator i = l.iterator();
		//Register error and notify user.
		ROI roi;
		ROIShape shape;
		try {
			while (i.hasNext()) {
				roi = (ROI) i.next();
				shape = roi.getShape(model.getCurrentView());
				tableModel.setData(shape.getFigure());
				fieldTable.setModel(tableModel);
				fieldTable.repaint();
			}
		} catch (Exception e) {
			MeasurementAgent.getRegistry().getLogger().info(this, 
													"Figures selection"+e);;
		}
		
	}
	
}
