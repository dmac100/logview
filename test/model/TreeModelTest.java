package model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TreeModelTest {
	TreeModel model = new TreeModel();
	
	@Test
	public void addItem() {
		TreeItem line1 = model.add("Line1");
		
		assertEquals("Line1", line1.getText());
	}
	
	@Test
	public void rowNumbering() {
		model.add("Line1");
		model.add("Line2");
		model.add("Line3");
		
		assertEquals(0, model.getTreeItemAtRow(0).getRow());
		assertEquals(1, model.getTreeItemAtRow(1).getRow());
		assertEquals(2, model.getTreeItemAtRow(2).getRow());
	}
	
	@Test
	public void rowNumbering_unexpandedChildren() {
		model.add("Line1");
		model.add("Line2");
		model.addChild("Child1");
		model.addChild("Child2");
		
		TreeItem line3 = model.add("Line3");
		
		assertEquals(2, line3.getRow());
	}
	
	@Test
	public void rowNumbering_expandedChildren() {
		model.add("Line1");
		TreeItem line2 = model.add("Line2");
		model.addChild("Child1");
		model.addChild("Child2");
		line2.toggleExpanded();
		
		TreeItem line4 = model.add("Line3");
		
		assertEquals(4, line4.getRow());
	}
	
	@Test
	public void rowNumbering_expandChildren() {
		TreeItem line1 = model.add("Line1");
		model.addChild("Child1");
		model.addChild("Child2");
		TreeItem line2 = model.add("Line2");
		
		line1.toggleExpanded();
		assertEquals(3, line2.getRow());
		
		line1.toggleExpanded();
		assertEquals(1, line2.getRow());
	}
	
	@Test
	public void childRowNumbering() {
		TreeItem line1 = model.add("Line1");
		model.addChild("Child1");
		model.addChild("Child2");
		TreeItem line2 = model.add("Line2");
		model.addChild("Child3");
		model.addChild("Child4");
		
		line1.toggleExpanded();
		assertEquals(4, model.getTotalRows());
		assertEquals("Line1", model.getTreeItemAtRow(0).getText());
		assertEquals("Child1", model.getTreeItemAtRow(1).getText());
		assertEquals("Child2", model.getTreeItemAtRow(2).getText());
		assertEquals("Line2", model.getTreeItemAtRow(3).getText());
		
		line1.toggleExpanded();
		assertEquals(2, model.getTotalRows());
		assertEquals("Line1", model.getTreeItemAtRow(0).getText());
		assertEquals("Line2", model.getTreeItemAtRow(1).getText());
		
		line2.toggleExpanded();
		assertEquals(4, model.getTotalRows());
		assertEquals("Line1", model.getTreeItemAtRow(0).getText());
		assertEquals("Line2", model.getTreeItemAtRow(1).getText());
		assertEquals("Child3", model.getTreeItemAtRow(2).getText());
		assertEquals("Child4", model.getTreeItemAtRow(3).getText());
	}
	
	@Test
	public void iterateItems() {
		TreeItem line1 = model.add("Line1");
		model.addChild("Child1");
		model.addChild("Child2");
		TreeItem line2 = model.add("Line2");
		TreeItem line3 = model.add("Line3");
		model.addChild("Child4");
		model.addChild("Child5");
		
		assertEquals("Line3", line2.getNext().getText());
		assertNull(line2.getNext().getNext());
		
		line3.toggleExpanded();
		assertEquals("Line3", line2.getNext().getText());
		assertEquals("Child4", line2.getNext().getNext().getText());
		assertEquals("Child5", line2.getNext().getNext().getNext().getText());
		assertNull(line2.getNext().getNext().getNext().getNext());
	}
}