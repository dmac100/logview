package view;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import model.FileNotifications;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class FileList {
	private final Table table;
	private Callback<File> fileSelectedCallback;
	
	private List<File> files = new ArrayList<File>();
	
	private FileNotifications fileNotifications = new FileNotifications();
	
	private Font normalFont;
	private Font boldFont;
	
	public FileList(Composite parent) {
		table = new Table(parent, SWT.NONE);
		
		table.setHeaderVisible(true);
		
		normalFont = table.getFont();
		boldFont = createBoldFont(table.getFont());
		
		final TableColumn column1 = new TableColumn(table, SWT.NONE, 0);
		final TableColumn column2 = new TableColumn(table, SWT.NONE, 1);
		
		column1.setWidth(200);
		column1.setText("Name");
		
		column2.setWidth(100);
		column2.setText("Modified");
		
		table.pack();
		
		column1.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int direction = (table.getSortDirection() == SWT.UP && table.getSortColumn() == column1) ? SWT.DOWN : SWT.UP;
				table.setSortColumn(column1);
				
				table.setSortDirection(direction);
				Collections.sort(files, new Comparator<File>() {
					public int compare(File f1, File f2) {
						return f1.getName().compareTo(f2.getName());
					}
				});
				
				if(direction == SWT.DOWN) {
					Collections.reverse(files);
				}
				
				refreshItems();
			}
		});
		
		column2.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int direction = (table.getSortDirection() == SWT.UP && table.getSortColumn() == column2) ? SWT.DOWN : SWT.UP;
				table.setSortColumn(column2);
				
				table.setSortDirection(direction);
				Collections.sort(files, new Comparator<File>() {
					public int compare(File f1, File f2) {
						return Long.compare(f1.lastModified(), f2.lastModified());
					}
				});
				
				if(direction == SWT.DOWN) {
					Collections.reverse(files);
				}
				
				refreshItems();
			}
		});
		
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				TableItem[] selection = table.getSelection();
				fileSelectedCallback.onCallback((File)selection[0].getData());
			}
		});
		
		table.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				boldFont.dispose();
			}
		});
		
		fileNotifications.setCallback(new Callback<Void>() {
			public void onCallback(Void t) {
				getWidget().getDisplay().asyncExec(new Runnable() {
					public void run() {
						refreshNotifications();
					}
				});
			}
		});
	}
	
	private Font createBoldFont(Font font) {
		FontData[] fontDatas = font.getFontData();
		for(FontData fontData:fontDatas) {
			fontData.setStyle(SWT.BOLD);
		}
		return new Font(Display.getCurrent(), fontDatas);
	}

	public void setFileSelectedCallback(Callback<File> callback) {
		this.fileSelectedCallback = callback;
	}
	
	public void refreshItems() {
		table.setRedraw(false);
		table.removeAll();
		for(File file:files) {
			addFileItem(file);
		}
		table.setRedraw(true);
	}
	
	public void addFile(File file) {
		files.add(file);
		addFileItem(file);
	}
	
	private void addFileItem(File file) {
		TableItem tableItem = new TableItem(table, SWT.NONE);
		tableItem.setData(file);
		tableItem.setText(0, file.getName());
		tableItem.setText(1, formatDate(file.lastModified()));
		refreshNotifications();
	}
	
	public void setModified(File file, boolean selected) {
		for(TableItem tableItem:table.getItems()) {
			if(tableItem.getData().equals(file)) {
				tableItem.setText(1, formatDate(file.lastModified()));
			}
		}
		fileNotifications.notifyModified(file);
	}
	
	private static String formatDate(long date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm");
		return dateFormat.format(date);
	}

	public void fileSelected(File file) {
		fileNotifications.fileSelected(file);
	}
	
	public void clear() {
		table.removeAll();
		files.clear();
		fileNotifications.clear();
	}

	public void refreshNotifications() {
		for(TableItem tableItem:table.getItems()) {
			File file = (File)tableItem.getData();
			
			int color = (fileNotifications.isModified(file) && !fileNotifications.isSelected(file)) ? SWT.COLOR_RED : SWT.COLOR_BLACK;
			Font font = fileNotifications.isRecentlyModified(file) ? boldFont : normalFont;

			for(int column = 0; column < 2; column++) {
				tableItem.setForeground(column, Display.getCurrent().getSystemColor(color));
				tableItem.setFont(column, font);
			}
		}
	}
	
	public Control getWidget() {
		return table;
	}
}
