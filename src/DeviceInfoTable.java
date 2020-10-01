import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DeviceInfoTable extends JFrame implements ActionListener {
	
	// Initialize UI components.
	private JPanel buttons;
	private JButton add, edit, delete;
	private JScrollPane scrollPane;
	private JTable deviceInfo;
	private static DefaultTableModel deviceInfoModel;
	private String column[] = new String[] {"ID", "NAME", "DATE INSTALLED", "COST", 
			"REPORT INTERVAL", "REMARKS"};
	
	/**
	 * The main frame where you can add, edit, delete, and see the data of 
	 * device_info table.
	 */
	public DeviceInfoTable() {
		// Set up frame.
		super("GPS Device Info");
		setSize(1080, 520);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Set up table.
		deviceInfoModel = new DefaultTableModel() {
			// Set table to be non-editable.
			public boolean isCellEditable(int row, int column) {
				return false;
		    }
		};
		deviceInfoModel.setColumnIdentifiers(column);
		deviceInfo = new JTable();
		deviceInfo.setModel(deviceInfoModel);
		deviceInfo.setSelectionModel(new ForcedListSelectionModel());
		scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(deviceInfo);
		add(BorderLayout.NORTH, scrollPane);
		
		// Set up buttons.
		buttons = new JPanel();
		add = new JButton("Add");
		edit = new JButton("Edit");
		delete = new JButton("Delete");
		add.addActionListener(this);
		edit.addActionListener(this);
		delete.addActionListener(this);
		buttons.add(add);
		buttons.add(edit);
		buttons.add(delete);
		add(BorderLayout.SOUTH, buttons);
		
		// Make the frame visible.
		setVisible(true);
		
		// Update table contents after UI finishes setting up.
		update_table();
	}
	
	/**
	 * Listener of the performed actions.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// Does actions depending on the source of action.
		switch (e.getActionCommand()) {
			// Does the following when "ADD" button is pressed.
			case "Add":
				// Switch to the "Add Device Info" window.
				this.dispose();
				new DeviceInfoForm("Add", null);
				break;
			// Does the following when "EDIT" button is pressed.
			case "Edit":
				// Try to get the selected row in the table.
				try {
					Vector rowData = deviceInfoModel.getDataVector()
							.elementAt(deviceInfo.getSelectedRow());
					// Switch to the "Edit Device Info" window.
					this.dispose();
					new DeviceInfoForm("Edit", rowData);
				}
				// Error if user has no selected row.
				catch (Exception err) {
					JOptionPane.showMessageDialog(this, "Please select row to update!");
				}
				
				break;
			// Does the following when "DELETE" button is pressed.
			case "Delete":
				// Show dialog for confirmation of deletion.
				int reply = JOptionPane.showConfirmDialog(null, 
						"Are you sure you want to delete selected row?", 
						"Confirm", JOptionPane.YES_NO_OPTION);
				
				// User confirmed the deletion.
				if (reply == JOptionPane.YES_OPTION) {
					// Get the id of selected row.
					int id = Integer.valueOf(deviceInfoModel.getDataVector()
							.elementAt(deviceInfo.getSelectedRow()).get(0).toString());
					
					// Call controller class to delete chosen row.
				    DeviceInfoController.delete_deviceInfo(id);
				    
				    // Reset and update the table.
				    deviceInfoModel.setRowCount(0);
				    update_table();
				}
				
				break;
		}
	}
	
	/**
	 * This function updates the table's contents.
	 */
	private static void update_table() {
		// Call controller class to return table data.
		ArrayList<DeviceInfo> deviceInfo = DeviceInfoController.get_deviceInfo();
		
		// Process table rows if returned data is not null.
		if (deviceInfo != null) {
			for (DeviceInfo data : deviceInfo) {
				Object rowData[] = { data.getId(), data.getName(),
						data.getDateInstalled(), data.getCost(),
						data.getReportInterval(), data.getRemarks() };
				deviceInfoModel.addRow(rowData);
			}
		}
		
		// Call table model to add the new rows.
		deviceInfoModel.fireTableDataChanged();
	}
	
	// Table selection model to force that only 1 row is selected.
	private class ForcedListSelectionModel extends DefaultListSelectionModel {

	    public ForcedListSelectionModel() {
	        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    }

	    @Override
	    public void clearSelection() {
	    	// Empty
	    }

	    @Override
	    public void removeSelectionInterval(int index0, int index1) {
	    	// Empty
	    }

	}
	
	public static void main(String[] args) {
		// Initialize .ini configuration then create connection pool.
		DbConnection.initialize_db();
		
		// Initialize main frame.
		new DeviceInfoTable();
	}
}
