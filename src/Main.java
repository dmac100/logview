import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import view.MainView;

public class Main {
	public static void main(String[] args) throws IOException {
		List<File> files = new ArrayList<File>();
		for(String arg:args) {
			files.add(new File(arg));
		}
		
		final Display display = new Display();
		
		final Shell shell = new Shell(display);
		shell.setText("LogView");
		shell.setLayout(new FillLayout());
		
		MainView mainView = new MainView(shell);
		
		Collections.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.compare(f2.lastModified(), f1.lastModified());
			}
		});
		
		for(File file:files) {
			mainView.addFile(file);
		}
		
		/*
		for(File file:new File("/home/david/opt/tomcat/logs/").listFiles()) {
			mainView.addFile(file);
		}
		*/
	
		shell.setSize(1100, 800);
		shell.open();
		
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		display.dispose();
	}
}
