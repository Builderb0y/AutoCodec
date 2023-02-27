package builderb0y.autocodec.reflection;

import java.lang.annotation.*;

import org.junit.Test;

import static org.junit.Assert.*;

public class AnnotationContainerTest {

	@Test
	public void testDeclaredAndInherited() {
		AnnotationContainer declared = AnnotationContainer.fromDeclared(ClassWithRepeatedAnnotations.class);
		assertEquals(4, declared.getAll().length);
		assertEquals(RepeatableContainer.class, declared.getAll()[0].annotationType());
		for (int i = 0; i < 3; i++) {
			assertEquals(RepeatableAnnotation.class, declared.getAll()[i + 1].annotationType());
			assertEquals(String.valueOf((char)(i + 'a')), ((RepeatableAnnotation)(declared.getAll()[i + 1])).value());
		}
		assertTrue(declared.equalsOrdered(AnnotationContainer.from(ClassWithRepeatedAnnotations.class)));
		assertTrue(declared.equalsOrdered(AnnotationContainer.from(ClassWithInheritedAnnotations.class)));
		assertTrue(AnnotationContainer.from(ClassWithRepeatedAnnotations.class).equalsOrdered(declared));
		assertTrue(AnnotationContainer.from(ClassWithInheritedAnnotations.class).equalsOrdered(declared));
		assertEquals(0, AnnotationContainer.fromDeclared(ClassWithInheritedAnnotations.class).getAll().length);
	}

	@Inherited
	@Repeatable(RepeatableContainer.class)
	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface RepeatableAnnotation {

		public abstract String value();
	}

	@Inherited
	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface RepeatableContainer {

		public abstract RepeatableAnnotation[] value();
	}

	@RepeatableAnnotation("a")
	@RepeatableAnnotation("b")
	@RepeatableAnnotation("c")
	public static class ClassWithRepeatedAnnotations {}

	public static class ClassWithInheritedAnnotations extends ClassWithRepeatedAnnotations {}
}