package builderb0y.autocodec.verifiers;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.VerifySorted;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;

import static org.junit.Assert.*;

public class SorterVerifierTest {

	@Test
	public void test() throws VerifyException {
		AutoVerifier<B> verifier = TestCommon.DEFAULT_CODEC.createVerifier(B.class);
		this.checkValid(verifier, new B(1, 2, 3, 4, 5));
		this.checkValid(verifier, new B(2, 1, 3, 5, 4));
		this.checkInvalid(verifier, new B(5, 4, 3, 2, 1));
		this.checkInvalid(verifier, new B(1, 3, 2, 4, 5));
		this.checkInvalid(verifier, new B(3, 1, 2, 4, 5));
		this.checkInvalid(verifier, new B(1, 2, 4, 3, 5));
		this.checkInvalid(verifier, new B(1, 2, 4, 5, 3));
	}

	public <T> void checkValid(AutoVerifier<T> verifier, T instance) throws VerifyException {
		TestCommon.DEFAULT_CODEC.verify(verifier, instance, JsonNull.INSTANCE, JsonOps.INSTANCE);
	}

	public <T> void checkInvalid(AutoVerifier<T> verifier, T instance) {
		try {
			TestCommon.DISABLED_CODEC.verify(verifier, instance, JsonNull.INSTANCE, JsonOps.INSTANCE);
			fail();
		}
		catch (VerifyException expected) {}
	}

	public static class A {

		public int one, five;

		public A(int one, int five) {
			this.one = one;
			this.five = five;
		}
	}

	public static class B extends A {

		public int two;
		public @VerifySorted(greaterThan = { "one", "two" }, lessThan = { "four", "five" }) int three;
		public int four;

		public B(int one, int two, int three, int four, int five) {
			super(one, five);
			this.two = two;
			this.three = three;
			this.four = four;
		}
	}

	@Test
	public void testMalformedAnnotations() {
		this.testMalformedAnnotation(BadName.class);
		this.testMalformedAnnotation(BadType1.class);
		this.testMalformedAnnotation(BadType2.class);
	}

	public void testMalformedAnnotation(Class<?> clazz) {
		try {
			TestCommon.DISABLED_CODEC.createVerifier(clazz);
			fail();
		}
		catch (FactoryException expected) {}
	}

	public static class BadName {

		public @VerifySorted(lessThan = "missing") int present;
	}

	public static class BadType1 {

		public @VerifySorted(lessThan = "floatValue") int intValue;
		public float floatValue;
	}

	public static class BadType2 {

		public @VerifySorted Object value;
	}

	@Test
	public void testNulls() throws VerifyException {
		AutoVerifier<NullBox> verifier = TestCommon.DEFAULT_CODEC.createVerifier(NullBox.class);
		this.checkValid(verifier, new NullBox(null, null));
		this.checkValid(verifier, new NullBox(null, 2));
		this.checkValid(verifier, new NullBox(1, null));
	}

	public static record NullBox(Integer a, @VerifySorted(greaterThan = "a") Integer b) {}

	@Test
	public void testInheritance() throws VerifyException {
		AutoVerifier<Inheriting> verifier = TestCommon.DEFAULT_CODEC.createVerifier(Inheriting.class);
		this.checkValid(verifier, new Inheriting(1, 2));
		this.checkInvalid(verifier, new Inheriting(2, 1));
	}

	public static class Inherited {

		public int a;
		public @VerifySorted(greaterThan = "a") int b;

		public Inherited(int a, int b) {
			this.a = a;
			this.b = b;
		}
	}

	public static class Inheriting extends Inherited {

		public Inheriting(int a, int b) {
			super(a, b);
		}
	}
}