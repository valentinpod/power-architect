package regress.ca.sqlpower.architect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;

public class SQLObjectTest extends TestCase {

	SQLObject target;
	protected boolean allowsChildren;
	
	class SQLObjectImpl extends SQLObject {
		SQLObjectImpl() {
			children = new ArrayList();
		}
		SQLObject parent = null;

		@Override
		public String getName() {
			throw new RuntimeException("test abstract stub");
		}
		@Override
		public SQLObject getParent() {
			return parent;
		}
		@Override
		protected void setParent(SQLObject parent) {
			this.parent = parent;
		}
		@Override
		protected void populate() throws ArchitectException {
			// System.err.println("Abstract test stub populate() invoked");
		}
		@Override
		public String getShortDisplayName() {
			throw new RuntimeException("test abstract stub");
		}
		@Override
		public boolean allowsChildren() {
			//throw new RuntimeException("test abstract stub");
			return allowsChildren;	 // Used by setChildren().
		}
		
		// manually call fireDbObjecChanged, so it can be tested.
		public void fakeObjectChanged(String string,Object oldValue, Object newValue) {
			
			fireDbObjectChanged(string,oldValue,newValue);
		}
		
		// manually call fireDbStructureChanged, so it can be tested.
		public void fakeStructureChanged(String string) {
			fireDbStructureChanged(string);
		}
	};
	
	public void setUp() {
		target = new SQLObjectImpl();
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.isPopulated()'
	 */
	public final void testIsPopulated() {
		assertFalse(target.isPopulated());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setPopulated(boolean)'
	 */
	public final void testSetPopulated() {
		target.setPopulated(true);
		assertTrue(target.isPopulated());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.setChildren(List)'
	 * Note that setChildren copies elements, does not assign the list, and
	 * getChildren returns an unmodifiable copy of the current list.
	 */
	public final void testAllChildHandlingMethods() throws ArchitectException {
		assertEquals(0, target.getChildCount());

		SQLObject x = new SQLObjectImpl();
		target.addChild(x);
		assertEquals(1, target.getChildCount());
		assertEquals(x, target.getChild(0));
		
		SQLObject y = new SQLObjectImpl();
		
		// Test addChild(int, SQLObject)
		target.addChild(0, y);
		assertEquals(y, target.getChild(0));
		assertEquals(x, target.getChild(1));
		
		target.removeChild(1);
		List<SQLObject> list2 = new LinkedList<SQLObject>();
		list2.add(y);
		assertEquals(list2, target.getChildren());
		
		target.removeChild(y);
		assertEquals(Collections.EMPTY_LIST, target.getChildren());
	}

	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.removeDependencies()'
	 */
	public final void testRemoveDependencies() {
		// At present the SQLObject version of this method does nothing, so there is no test here.
	}

	class TestListener implements SQLObjectListener {

		boolean childInserted;
		boolean childRemoved;
		boolean objectChanged;
		boolean structureChanged;
		
		public void dbChildrenInserted(SQLObjectEvent e) {
			System.out.println("Listener.dbChildrenInserted()");
			setChildInserted(true);
		}

		public void dbChildrenRemoved(SQLObjectEvent e) {
			System.out.println("Listener.dbChildrenRemoved()");
			setChildRemoved(true);
		}

		public void dbObjectChanged(SQLObjectEvent e) {
			System.out.println("Listener.dbObjectChanged()");
			setObjectChanged(true);
		}

		public void dbStructureChanged(SQLObjectEvent e) {
			System.out.println("Listener.dbStructureChanged()");
			setStructureChanged(true);
		}

		public boolean isChildInserted() {
			return childInserted;
		}

		public void setChildInserted(boolean childInserted) {
			this.childInserted = childInserted;
		}

		public boolean isChildRemoved() {
			return childRemoved;
		}

		public void setChildRemoved(boolean childRemoved) {
			this.childRemoved = childRemoved;
		}

		public boolean isObjectChanged() {
			return objectChanged;
		}

		public void setObjectChanged(boolean objectChanged) {
			this.objectChanged = objectChanged;
		}

		public boolean isStructureChanged() {
			return structureChanged;
		}

		public void setStructureChanged(boolean structureChanged) {
			this.structureChanged = structureChanged;
		}
		
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.SQLObject.addSQLObjectListener(SQLObjectListener)'
	 */
	public final void testSQLObjectListenerHandling() throws ArchitectException {
		SQLObjectListener t = new TestListener();
		TestListener tt = (TestListener)t;
		
		target.addSQLObjectListener(t);
		allowsChildren = true;
		
		tt.setChildInserted(false);
		final SQLObjectImpl objectImpl = new SQLObjectImpl();
		target.addChild(objectImpl);
		assertTrue(tt.isChildInserted());
		
		tt.setObjectChanged(false);
		((SQLObjectImpl)target).fakeObjectChanged("fred","old value","new value");
		assertTrue(tt.isObjectChanged());
		
		tt.setObjectChanged(false);
		((SQLObjectImpl)target).fakeObjectChanged("fred","old value","old value");
		assertFalse(tt.isObjectChanged());
		
		tt.setStructureChanged(false);
		((SQLObjectImpl)target).fakeStructureChanged("george");
		assertTrue(tt.isStructureChanged());
		
		// MUST BE LAST!!
		target.removeSQLObjectListener(t);
		assertEquals(Collections.EMPTY_LIST, target.getSQLObjectListeners());

	}
}
