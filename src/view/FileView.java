package view;

import java.util.List;

import model.FilteredTreeModel;
import model.TreeItem;
import model.TreeModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;

public class FileView {
	private final Canvas canvas;
	
	private TreeModel treeModel = new TreeModel();
	private FilteredTreeModel filteredModel = new FilteredTreeModel(new TreeModel(), "");
	private TreeModel activeModel = treeModel;
	
	private int verticalOffset = 0;
	private boolean tail = false;
	
	final int lineHeight = 17;
	
	private int maxWidth = 1000;
	
	public FileView(Composite parent) {
		this.canvas = new Canvas(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_BACKGROUND);
		
		canvas.addMouseWheelListener(new MouseWheelListener() {
			public void mouseScrolled(MouseEvent event) {
				ScrollBar bar = canvas.getVerticalBar();
				bar.setSelection(bar.getSelection() - event.count * 50);
			}
		});
		
		canvas.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent event) {
				setBounds();
				canvas.redraw();
			}
		});
		
		canvas.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent event) {
				int line = (canvas.getVerticalBar().getSelection() + event.y) / lineHeight;
				
				TreeItem treeItem = activeModel.getTreeItemAtRow(line);
				if(treeItem != null) {
					if(event.x + canvas.getHorizontalBar().getSelection() < 20) {
						if(treeItem.hasChildren()) {
							treeItem.toggleExpanded();
						}
						setBounds();
						canvas.redraw();
					}
				}
			}
		});
		
		canvas.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				canvas.redraw();
			}
		});
		
		canvas.getVerticalBar().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				int diff = verticalOffset - canvas.getVerticalBar().getSelection();
			
				canvas.scroll(0, diff, 0, 0, canvas.getBounds().width, canvas.getBounds().height, true);
				
				verticalOffset = canvas.getVerticalBar().getSelection();
			}
		});
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				Image buffer = new Image(Display.getCurrent(), canvas.getBounds());
				GC gc = new GC(buffer);
				gc.setAntialias(SWT.ON);
				
				gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
				
				int top = verticalOffset;
				int bottom = verticalOffset + canvas.getClientArea().height;
				
				Transform transform = new Transform(Display.getCurrent());
				transform.translate(-canvas.getHorizontalBar().getSelection(), 0);
				gc.setTransform(transform);
				
				int offsetY = -(top % lineHeight);
				
				for(
					TreeItem treeItem = activeModel.getTreeItemAtRow(top / lineHeight);
					treeItem != null && treeItem.getRow() * lineHeight <= bottom;
					treeItem = treeItem.getNext()
				) {
					int indent = 0;
					if(treeItem.hasChildren() || treeItem.getLevel() > 0) {
						gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
						indent = 10;
					} else {
						gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
					}
					gc.drawText(treeItem.getText(), 20 + indent, offsetY + 2);
					
					gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
					
					if(treeItem.hasChildren()) {
						Rectangle expander = new Rectangle(3, offsetY + 4, 4, 8);
						
						if(treeItem.isExpanded()) {
							gc.drawLine(expander.x, expander.y + 2, expander.x + 10, expander.y + 2);
							gc.drawLine(expander.x, expander.y + 2, expander.x + 5, expander.y + 7);
							gc.drawLine(expander.x + 10, expander.y + 2, expander.x + 5, expander.y + 7);
						} else {
							gc.drawLine(expander.x + 2, expander.y, expander.x + 2, expander.y + 10);
							gc.drawLine(expander.x + 2, expander.y, expander.x + 7, expander.y + 5);
							gc.drawLine(expander.x + 2, expander.y + 10, expander.x + 7, expander.y + 5);
						}
					}
					
					offsetY += lineHeight;
				}
				
				event.gc.drawImage(buffer, 0, 0);
				buffer.dispose();
				transform.dispose();
			}
		});
	}
	
	public void addLines(List<String> lines) {
		final ScrollBar bar = canvas.getVerticalBar();
		final boolean atBottom = bar.getSelection() + canvas.getClientArea().height > activeModel.getTotalRows() * lineHeight - 20;
		
		for(String line:lines) {
			if(line.matches("\\s*")) {
				continue;
			}
	
			// Increase width for horizontal scrolling if we get a long line.
			GC gc = new GC(canvas);
			int width = gc.stringExtent(line).x;
			if(width > maxWidth) {
				maxWidth = width;
				setBounds();
			}
			gc.dispose();
			
			if(line.matches("\\s.*") && treeModel.getTotalRows() > 0) {
				treeModel.addChild(line);
				filteredModel.addChild(line);
			} else {
				treeModel.add(line);
				filteredModel.add(line);
			}
			
			setBounds();
			
		}
		
		if(atBottom && tail) {
			scrollToEnd();
		}
	}
	
	private void setBounds() {
		canvas.getVerticalBar().setIncrement(50);
		canvas.getHorizontalBar().setIncrement(50);
		canvas.getVerticalBar().setPageIncrement(400);
		canvas.getHorizontalBar().setPageIncrement(400);
		
		canvas.getHorizontalBar().setMaximum(maxWidth + 20 - canvas.getClientArea().width);
		canvas.getVerticalBar().setMaximum(lineHeight * activeModel.getTotalRows() - canvas.getClientArea().height + 20);
		
		verticalOffset = canvas.getVerticalBar().getSelection();
	}
	
	public void setTail(boolean tail) {
		this.tail = true;
	}
	
	public void setFilter(String filter) {
		if(filter.isEmpty()) {
			activeModel = treeModel;
		} else {
			filteredModel = new FilteredTreeModel(treeModel, filter);
			activeModel = filteredModel;
		}
		setBounds();
		canvas.redraw();
	}
	
	public void scrollToEnd() {
		canvas.getVerticalBar().setSelection(lineHeight * activeModel.getTotalRows() - canvas.getClientArea().height + 20);
		verticalOffset = canvas.getVerticalBar().getSelection();
		canvas.redraw();
	}

	public void clearLines() {
		treeModel = new TreeModel();
		filteredModel = new FilteredTreeModel(treeModel, filteredModel.getFilter());
		setBounds();
		canvas.redraw();
	}
	
	public Control getWidget() {
		return canvas;
	}

	public boolean isEmpty() {
		return (treeModel.getTotalRows() >= 0);
	}

	public void dispose() {
		canvas.dispose();
	}
}