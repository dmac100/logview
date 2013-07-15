package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilteredTreeModel extends TreeModel {
	private String filter;
	private String lastText;
	
	private List<String> lastChildren = new ArrayList<String>();

	public FilteredTreeModel(TreeModel source, String filter) {
		this.filter = filter;
		
		for(MutableTreeItem line:source.lines) {
			add(line.getText());
			for(MutableTreeItem child:line.children) {
				addChild(child.getText());
			}
		}
	}
	
	public void addChild(String text) {
		if(lastText == null) {
			super.addChild(text);
		} else {
			if(text.toLowerCase().contains(filter.toLowerCase())) {
				super.add(lastText);
				for(String child:lastChildren) {
					super.addChild(child);
				}
				super.addChild(text);
				lastText = null;
			}
		}
	}
	
	public TreeItem add(String text) {
		if(text.toLowerCase().contains(filter.toLowerCase())) {
			lastText = null;
			return super.add(text);
		} else {
			lastText = text;
			lastChildren = new ArrayList<String>();
			return null;
		}
	}
	
	public String getFilter() {
		return filter;
	}
}