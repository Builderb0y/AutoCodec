package builderb0y.autocodec.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class TypeFormatterTest {

	@Test
	public void testAnonymous() {
		assertEquals("TypeFormatterTest$EmptyClass@TypeFormatterTest$1", TypeFormatter.getSimpleClassName(new EmptyClass() {}.getClass()));
		assertEquals("TypeFormatterTest$EmptyInterface@TypeFormatterTest$2", TypeFormatter.getSimpleClassName(new EmptyInterface() {}.getClass()));
	}

	public static class EmptyClass {}

	public static interface EmptyInterface {}
}