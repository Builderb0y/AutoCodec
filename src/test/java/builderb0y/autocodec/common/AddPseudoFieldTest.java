package builderb0y.autocodec.common;

import java.lang.annotation.AnnotationFormatError;

import org.junit.Test;

import builderb0y.autocodec.annotations.AddPseudoField;
import builderb0y.autocodec.reflection.PseudoField;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.PseudoFieldView;

import static org.junit.Assert.*;

public class AddPseudoFieldTest {

	@Test
	public void test() {
		this.assertValid(GetterOnly.class);
		this.assertValid(DefaultGetterOnly.class);
		this.assertInvalid(MissingGetterOnly.class);
		this.assertInvalid(MissingDefaultGetterOnly.class);
		this.assertValid(Setter.class);
		this.assertValid(DefaultSetter.class);
		this.assertInvalid(MissingSetter.class);
		this.assertValid(MissingDefaultSetter.class);
		this.assertInvalid(MismatchedValueTypes.class);
		this.assertValid(RedHerringDefaultSetter.class);

		FieldLikeMemberView<Sub, ?>[] fields = TestCommon.DEFAULT_CODEC.reflect(Sub.class).getFields(false);
		assertEquals("Should only report one field", 1, fields.length);
		FieldLikeMemberView<Sub, ?> value = fields[0];
		assertEquals("value", value.getName());
		if (!(value instanceof PseudoFieldView<Sub, ?>)) fail("Not pseudo: " + value);
		assertEquals(String.class, value.getType().getRawClass());

		//just check that getType() doesn't NPE.
		TestCommon.DEFAULT_CODEC.reflect(NotSuper.class).getFields(false)[0].getType();
	}

	public void assertValid(Class<?> clazz) {
		PseudoField.getPseudoFieldsNoClone(clazz);
	}

	public void assertInvalid(Class<?> clazz) {
		try {
			PseudoField.getPseudoFieldsNoClone(clazz);
			fail();
		}
		catch (AnnotationFormatError expected) {}
	}

	@AddPseudoField(name = "value", getter = "getValue")
	public static class GetterOnly {
		public Object getValue() { return null; }
	}

	@AddPseudoField("value")
	public static class DefaultGetterOnly {
		public Object value() { return null; }
	}

	@AddPseudoField(name = "value", getter = "getValue")
	public static class MissingGetterOnly {}

	@AddPseudoField("value")
	public static class MissingDefaultGetterOnly {}

	@AddPseudoField(name = "value", getter = "getValue", setter = "setValue")
	public static class Setter {
		public Object getValue() { return null; }
		public void setValue(Object value) {}
	}

	@AddPseudoField("value")
	public static class DefaultSetter {
		public Object value() { return null; }
		public void value(Object value) {}
	}

	@AddPseudoField(name = "value", getter = "getValue", setter = "setValue")
	public static class MissingSetter {
		public Object getValue() { return null; }
	}

	@AddPseudoField("value")
	public static class MissingDefaultSetter {
		public Object value() { return null; }
	}

	@AddPseudoField(name = "value", getter = "getValue", setter = "setValue")
	public static class MismatchedValueTypes {
		public Integer getValue() { return null; }
		public void setValue(String value) {}
	}

	@AddPseudoField("value")
	public static class RedHerringDefaultSetter {
		public Integer value() { return null; }
		public void value(String value) {}
	}

	public static class Super<T> {

		public T secret;

		public T value() {
			return this.secret;
		}
	}

	@AddPseudoField("value")
	public static class Sub extends Super<String> {}

	@AddPseudoField("value")
	public static class NotSuper {

		public int value() {
			return 1;
		}
	}
}