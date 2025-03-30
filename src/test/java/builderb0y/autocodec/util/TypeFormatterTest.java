package builderb0y.autocodec.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;

import static org.junit.Assert.*;

public class TypeFormatterTest {

	@Test
	public void testAnonymous() {
		assertEquals("TypeFormatterTest$EmptyClass@TypeFormatterTest$1", TypeFormatter.getSimpleClassName(new EmptyClass() {}.getClass()));
		assertEquals("TypeFormatterTest$EmptyInterface@TypeFormatterTest$2", TypeFormatter.getSimpleClassName(new EmptyInterface() {}.getClass()));
	}

	@Test
	public void testAnnotation() {
		assertEquals(
			"@TypeFormatterTest$Example(byteValue=(byte)0x00, shortValue=0, intValue=0, longValue=0L, floatValue=0.0f, doubleValue=0.0, charValue='a', booleanValue=false, stringValue=\"should.not.truncate\", classValue=TypeFormatterTest$Example.class)",
			new TypeFormatter(256).append(Example.class.getDeclaredAnnotation(Example.class)).toString()
		);
		assertEquals(
			"@TypeFormatterTest$ArrayExample(byteValue={(byte)0x00, (byte)0x01}, shortValue={0, 1}, intValue={0, 1}, longValue={0L, 1L}, floatValue={0.0f, 1.0f}, doubleValue={0.0, 1.0}, charValue={'a', 'b'}, booleanValue={false, true}, stringValue={\"should.not.truncate\", \"or.this.either\"}, classValue={TypeFormatterTest$Example.class, TypeFormatterTest$ArrayExample.class})",
			new TypeFormatter(512).append(ArrayExample.class.getDeclaredAnnotation(ArrayExample.class)).toString()
		);
		assertEquals(
			"@TypeFormatterTest$NestedExample({@TypeFormatterTest$NestedExample2({}), @TypeFormatterTest$NestedExample2({@TypeFormatterTest$NestedExample3()}), @TypeFormatterTest$NestedExample2({@TypeFormatterTest$NestedExample3(), @TypeFormatterTest$NestedExample3()})})",
			new TypeFormatter(256).append(NestedExample.class.getDeclaredAnnotation(NestedExample.class)).toString()
		);
	}

	public static class EmptyClass {}

	public static interface EmptyInterface {}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Example(
		byteValue = (byte)(0),
		shortValue = (short)(0),
		intValue = 0,
		longValue = 0L,
		floatValue = 0.0F,
		doubleValue = 0.0D,
		charValue = 'a',
		booleanValue = false,
		stringValue = "should.not.truncate",
		classValue = Example.class
	)
	public static @interface Example {

		public abstract byte byteValue();

		public abstract short shortValue();

		public abstract int intValue();

		public abstract long longValue();

		public abstract float floatValue();

		public abstract double doubleValue();

		public abstract char charValue();

		public abstract boolean booleanValue();

		public abstract String stringValue();

		public abstract Class<?> classValue();
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@ArrayExample(
		byteValue = { (byte)(0), (byte)(1) },
		shortValue = { (short)(0), (short)(1) },
		intValue = { 0, 1 },
		longValue = { 0L, 1L },
		floatValue = { 0.0F, 1.0F },
		doubleValue = { 0.0D, 1.0D },
		charValue = { 'a', 'b' },
		booleanValue = { false, true },
		stringValue = { "should.not.truncate", "or.this.either" },
		classValue = { Example.class, ArrayExample.class }
	)
	public static @interface ArrayExample {

		public abstract byte[] byteValue();

		public abstract short[] shortValue();

		public abstract int[] intValue();

		public abstract long[] longValue();

		public abstract float[] floatValue();

		public abstract double[] doubleValue();

		public abstract char[] charValue();

		public abstract boolean[] booleanValue();

		public abstract String[] stringValue();

		public abstract Class<?>[] classValue();
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@NestedExample({
		@NestedExample2({}),
		@NestedExample2({ @NestedExample3 }),
		@NestedExample2({ @NestedExample3, @NestedExample3 }),
	})
	public static @interface NestedExample {

		public abstract NestedExample2[] value();
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface NestedExample2 {

		public abstract NestedExample3[] value();
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface NestedExample3 {}
}