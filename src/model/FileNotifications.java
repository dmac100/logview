package model;

import java.io.File;
import java.util.*;

import org.eclipse.swt.widgets.Display;

import view.Callback;

public class FileNotifications {
	private Set<File> modified = new HashSet<File>();
	private Set<File> recentlyModified = new HashSet<File>();
	private Map<File, Timer> modifiedTimers = new HashMap<File, Timer>();
	private File selected;
	
	private Callback<Void> callback;
	
	public void notifyModified(final File file) {
		Timer timer = modifiedTimers.get(file);
		if(timer != null) {
			timer.cancel();
		}
		timer = new Timer(true);
		
		modifiedTimers.put(file, timer);
		recentlyModified.add(file);
		modified.add(file);
		
		final Display display = Display.getCurrent();
		timer.schedule(new TimerTask() {
			public void run() {
				display.asyncExec(new Runnable() {
					public void run() {
						modifiedTimers.remove(file);
						recentlyModified.remove(file);
						callback.onCallback(null);
					}
				});
			}
		}, 5000);
		
		fireCallback();
	}

	public void fileSelected(File file) {
		selected = file;
		modified.remove(file);
		recentlyModified.remove(file);
		
		Timer timer = modifiedTimers.get(file);
		if(timer != null) {
			timer.cancel();
			modifiedTimers.remove(file);
		}
		
		fireCallback();
	}

	public void clear() {
		modified.clear();
		recentlyModified.clear();
		
		for(Timer timer:modifiedTimers.values()) {
			timer.cancel();
		}
		modifiedTimers.clear();
		
		fireCallback();
	}
	
	public boolean isSelected(File file) {
		return selected == file;
	}

	public boolean isModified(File file) {
		return modified.contains(file);
	}

	public boolean isRecentlyModified(File file) {
		return recentlyModified.contains(file);
	}
	
	public void fireCallback() {
		if(callback != null) {
			callback.onCallback(null);
		}
	}

	public void setCallback(Callback<Void> callback) {
		this.callback = callback;
	}
}
