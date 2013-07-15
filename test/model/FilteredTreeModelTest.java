package model;

import static org.junit.Assert.*;

import org.junit.Test;

public class FilteredTreeModelTest {
	@Test
	public void createInitialLines() {
		TreeModel source = new TreeModel();
		source.add("line1");
		source.add("line2");
		
		FilteredTreeModel model = new FilteredTreeModel(source, "line");
		assertEquals(2, model.getTotalRows());
		assertEquals("line1", model.getTreeItemAtRow(0).getText());
		assertEquals("line2", model.getTreeItemAtRow(1).getText());
	}
	
	@Test
	public void filterInitialLines() {
		TreeModel source = new TreeModel();
		source.add("line1");
		source.add("line2");
		
		FilteredTreeModel model = new FilteredTreeModel(source, "line2");
		assertEquals(1, model.getTotalRows());
		assertEquals("line2", model.getTreeItemAtRow(0).getText());
	}
	
	@Test
	public void addNewLine() {
		TreeModel source = new TreeModel();
		
		FilteredTreeModel model = new FilteredTreeModel(source, "line");
		model.add("line1");
		
		assertEquals(1, model.getTotalRows());
		assertEquals("line1", model.getTreeItemAtRow(0).getText());
	}
	
	@Test
	public void filterNewLine() {
		TreeModel source = new TreeModel();
		
		FilteredTreeModel model = new FilteredTreeModel(source, "line2");
		model.add("line1");
		model.add("line2");
		
		assertEquals(1, model.getTotalRows());
		assertEquals("line2", model.getTreeItemAtRow(0).getText());
	}
}
