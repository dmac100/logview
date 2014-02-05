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
	
	public synchronized void notifyModified(final File file) {
		Timer timer = modifiedTimers.get(file);
		if(timer != null) {
			timer.cancel();
		}
		timer = new Timer(true);
		
		modifiedTimers.put(file, timer);
		recentlyModified.add(file);
		modified.add(file);
		
		timer.schedule(new TimerTask() {
			public void run() {
				clearRecentlyModified(file);
			}
		}, 5000);
		
		fireCallback();
	}
	
	private synchronized void clearRecentlyModified(File file) {
		modifiedTimers.remove(file);
		recentlyModified.remove(file);
		callback.onCallback(null);
	}

	public synchronized void fileSelected(File file) {
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

	public synchronized void clear() {
		modified.clear();
		recentlyModified.clear();
		
		for(Timer timer:modifiedTimers.values()) {
			timer.cancel();
		}
		modifiedTimers.clear();
		
		fireCallback();
	}
	
	public synchronized boolean isSelected(File file) {
		return selected == file;
	}

	public synchronized boolean isModified(File file) {
		return modified.contains(file);
	}

	public synchronized boolean isRecentlyModified(File file) {
		return recentlyModified.contains(file);
	}
	
	public synchronized void fireCallback() {
		if(callback != null) {
			callback.onCallback(null);
		}
	}

	public synchronized void setCallback(Callback<Void> callback) {
		this.callback = callback;
	}
}
