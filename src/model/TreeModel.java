package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeModel {
	protected class MutableTreeItem implements TreeItem {
		protected String text;
		protected List<MutableTreeItem> children = new ArrayList<>();
		protected boolean expanded = false;
		protected int level;
		protected int row;
		protected int index;
		protected MutableTreeItem next;
		
		public MutableTreeItem(String text, int index, int level) {
			this.text = text;
			this.index = index;
			this.level = level;
		}
		
		@Override
		public String getText() {
			return text;
		}
		
		private List<MutableTreeItem> getChildren() {
			return Collections.unmodifiableList(children);
		}
		
		private TreeItem addChild(String text) {
			MutableTreeItem child = new MutableTreeItem(text, children.size(), level + 1);
			child.row = row + children.size() + 1;
			children.add(child);
			
			if(children.size() > 1) {
				children.get(children.size() - 2).next = children.get(children.size() - 1);
			}
			
			return child;
		}

		@Override
		public int getRow() {
			return row;
		}

		@Override
		public void toggleExpanded() {
			if(children.size() == 0) throw new IllegalStateException("No Children");
			expanded = !expanded;
			
			if(expanded) {
				totalRows += children.size();
			} else {
				totalRows -= children.size();
			}
			
			int currentRow = row;
			for(MutableTreeItem current = getNext(); current != null; current = current.getNext()) {
				current.row = ++currentRow;
			}
		}

		@Override
		public MutableTreeItem getNext() {
			if(expanded && children.size() > 0) {
				return children.get(0);
			}
			
			if(level == 0) {
				if(index + 1 < lines.size()) {
					return lines.get(index + 1);
				}
			} else {
				return next;
			}
			
			return null;
		}

		@Override
		public boolean isExpanded() {
			return expanded;
		}

		@Override
		public boolean hasChildren() {
			return !children.isEmpty();
		}

		@Override
		public int getLevel() {
			return level;
		}
	}
	
	protected List<MutableTreeItem> lines = new ArrayList<>();
	protected int totalRows = 0;
	
	public TreeItem getTreeItemAtRow(int row) {
		for(int i = 0; i < lines.size(); i++) {
			if(lines.get(i).getRow() == row) {
				return lines.get(i);
			}
			if(i == lines.size() - 1 || lines.get(i+1).row > row) {
				for(TreeItem child:lines.get(i).getChildren()) {
					if(child.getRow() == row) {
						return child;
					}
				}
			}
		}
		return null;
	}

	public void addChild(String text) {
		lines.get(lines.size() - 1).addChild(text);
	}

	public int getTotalRows() {
		return totalRows;
	}
	
	public TreeItem add(String text) {
		MutableTreeItem lineItem = new MutableTreeItem(text, lines.size(), 0);
		lineItem.row = totalRows;
		lines.add(lineItem);
		totalRows++;
		
		if(lines.size() > 1) {
			List<MutableTreeItem> children = (List<MutableTreeItem>) lines.get(lines.size() - 2).getChildren();
			if(children.size() > 0) {
				children.get(children.size() - 1).next = lineItem;
			}
		}
		
		return lineItem;
	}

	void printModel() {
		for(MutableTreeItem item = lines.get(0); item != null; item = item.getNext()) {
			System.out.println(item.getRow() + " - " + item.getText());
		}
	}
}