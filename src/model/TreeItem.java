package model;

public interface TreeItem {
	int getRow();
	boolean hasChildren();
	String getText();
	void toggleExpanded();
	TreeItem getNext();
	boolean isExpanded();
	int getLevel();
}
