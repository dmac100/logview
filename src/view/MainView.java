package view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tayler.Tailer;
import tayler.TailerListenerAdapter;

public class MainView {
	private static final Logger log = LoggerFactory.getLogger(MainView.class);
	
	private Map<File, FileView> fileViews = new HashMap<File, FileView>();
	
	private Shell shell;
	private FileList fileList;
	private CTabFolder tabs;
	
	private String filter = "";
	
	public MainView(Shell shell) {
		this.shell = shell;

		addFileDropTarget(shell);
		
		createMenubar(shell);
		
		SashForm sash = new SashForm(shell, SWT.NONE);
		this.fileList = new FileList(sash);
		
		Composite right = new Composite(sash, SWT.NONE);
		this.tabs = new CTabFolder(right, SWT.NONE);
		
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		right.setLayout(gridLayout);
		tabs.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createBottomBar(right);
		
		fileList.setFileSelectedCallback(new Callback<File>() {
			public void onCallback(File selected) {
				fileList.clearModified(selected);
				
				for(CTabItem existing:tabs.getItems()) {
					if(existing.getData().equals(selected)) {
						tabs.setSelection(existing);
						return;
					}
				}
				
				final CTabItem tabItem = new CTabItem(tabs, SWT.NONE);
				tabItem.setText(selected.getName());
				tabItem.setData(selected);
				tabs.setSelection(tabItem);
				tabItem.setShowClose(true);
				tabs.setSimple(false);
				tabs.setTabHeight(22);
				
				final FileView fileView = fileViews.get(selected);
				
				if(fileView.isEmpty()) {
					try {
						fileView.addLines(FileUtils.readLines(selected));
					} catch(IOException e) {
						log.error("Error reading file: " + selected, e);
					}
					fileView.setTail(true);
				}
				
				Display.getCurrent().asyncExec(new Runnable() {
					public void run() {
						fileView.scrollToEnd();
					}
				});
				tabItem.setControl(fileView.getWidget());
			}
		});
		
		tabs.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				File file = (File)event.item.getData();
				fileList.clearModified(file);
			}
		});
		
		tabs.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if(event.stateMask == SWT.CONTROL && event.keyCode == 'w') {
					CTabItem selected = tabs.getSelection();
					if(selected != null) {
						selected.dispose();
					}
				}
			}
		});
		
		sash.setWeights(new int[] { 30, 70 });
	}
	
	private void addFileDropTarget(Composite parent) {
		final Transfer fileTransfer = FileTransfer.getInstance();
		
		DropTarget target = new DropTarget(parent, DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(new Transfer[] { fileTransfer });
		target.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if(event.detail == DND.DROP_DEFAULT) {
					if((event.operations & DND.DROP_COPY) > 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}
			
			public void drop(DropTargetEvent event) {
				if(fileTransfer.isSupportedType(event.currentDataType)) {
					List<File> files = new ArrayList<File>();
					for(String file:(String[])event.data) {
						files.add(new File(file));
					}
					setFiles(files);
				}
			}
		});
	}

	private void createBottomBar(Composite parent) {
		Composite bar = new Composite(parent, SWT.NONE);
		
		bar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginBottom = 5;
		gridLayout.marginWidth = 5;
		bar.setLayout(gridLayout);
		
		Label label = new Label(bar, SWT.NONE);
		label.setText("Filter:");
		
		final Text text = new Text(bar, SWT.NONE);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		text.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent event) {
				filter = text.getText();
				
				for(FileView view:fileViews.values()) {
					view.setFilter(filter);
				}
			}
		});
	}

	private void createMenubar(final Shell shell) {
		Menu menu = new Menu(shell, SWT.BAR);
		
		MenuItem fileMenuItem = new MenuItem(menu, SWT.MENU);
		fileMenuItem.setText("File");
		Menu fileMenu = new Menu(fileMenuItem);
		fileMenuItem.setMenu(fileMenu);
		
		MenuItem openItem = new MenuItem(fileMenu, SWT.NONE);
		openItem.setText("Open...");
		openItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				List<File> files = selectFiles();
				if(!files.isEmpty()) {
					setFiles(files);
				}
			}
		});
		
		new MenuItem(fileMenu, SWT.SEPARATOR);
		
		MenuItem exitItem = new MenuItem(fileMenu, SWT.NONE);
		exitItem.setText("Exit");
		exitItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.dispose();
			}
		});
		
		shell.setMenuBar(menu);
	}
	
	private List<File> selectFiles() {
		FileDialog dialog = new FileDialog(shell, SWT.MULTI);
		dialog.setText("Open");
		dialog.open();
		
		List<File> files = new ArrayList<File>();
		for(String filename:dialog.getFileNames()) {
			files.add(new File(dialog.getFilterPath(), filename));
		}
		
		return files;
	}

	private void displayException(Exception e) {
		MessageBox messageBox = new MessageBox(shell);
		messageBox.setText("Error");
		messageBox.setMessage(e.getMessage() == null ? e.toString() : e.getMessage());
		e.printStackTrace();
		
		messageBox.open();
	}
	
	private void tail(final FileView fileView, final File file) {
		class TailerListener extends TailerListenerAdapter {
			private final Queue<String> queue = new ConcurrentLinkedQueue<String>();
			
			public void handle(final String line) {
				queue.add(line);
			}
			
			public void flushQueue() {
				if(!queue.isEmpty()) {
					handle(new ArrayList<String>(queue));
					queue.clear();
				}
			}
			
			private void handle(final List<String> lines) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						CTabItem selectedTab = tabs.getSelection();
						boolean selected = (selectedTab != null && selectedTab.getData() == file);
						fileList.setModified(file, selected);
						
						fileView.addLines(lines);
					}
				});
			}
			
			public void fileRotated() {
				fileView.clearLines();
			}
		}
		
		final TailerListener tailer = new TailerListener();
		
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				tailer.flushQueue();
			}
		}, 0, 100);
		
		
		Thread thread = new Thread(new Tailer(file, tailer, 100, true));
		thread.setDaemon(true);
		thread.start();
		
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				fileView.scrollToEnd();
			}
		});
	}
	
	private void clearFiles() {
		fileList.clear();
		
		for(FileView fileView:fileViews.values()) {
			fileView.dispose();
		}
		
		for(CTabItem tabItem:tabs.getItems()) {
			tabItem.dispose();
		}
		
		fileViews.clear();	
	}
	
	public void setFiles(List<File> files) {
		clearFiles();
		
		for(File file:files) {
			addFile(file);
		}
	}
	
	public void addFile(final File file) {
		if(!file.isFile()) return;
		
		fileList.addFile(file);
		
		FileView fileView = new FileView(tabs);
		fileView.setFilter(filter);
		
		fileView.getWidget().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if(event.stateMask == SWT.CONTROL && event.keyCode == 'w') {
					for(CTabItem tabItem:tabs.getItems()) {
						if(tabItem.getData().equals(file)) {
							tabItem.dispose();
						}
					}
				}
			}
		});
		
		fileViews.put(file, fileView);
		
		tail(fileView, file);
	}
}