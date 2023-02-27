package builderb0y.autocodec.annotations;

import org.junit.Test;

import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class RecordLikeTest {

	public static <T> void assertSuccess(ReifiedType<T> type, T object) throws DecodeException {
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, type).test(object);
	}

	public static <T> void assertFailure(ReifiedType<T> type, T object) {
		try {
			new CoderUnitTester<>(TestCommon.DISABLED_CODEC, type).test(object);
			fail();
		}
		catch (Exception expected) {}
	}

	@Test
	public void testNoArg() throws DecodeException {
		assertFailure(new ReifiedType<>() {}, new Empty());
		assertSuccess(new ReifiedType<@RecordLike({}) Empty>() {}, new Empty());
	}

	public static class Empty {

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Empty;
		}
	}

	@Test
	public void testAmbiguous() throws DecodeException {
		assertFailure(new ReifiedType<>() {}, new Ambiguous(3, 4));
		assertSuccess(new ReifiedType<@RecordLike({ "a", "b" }) Ambiguous>() {}, new Ambiguous(3, 4));
	}

	public static class Ambiguous {

		public int a, b;

		public Ambiguous(int a) {
			this.a = a;
			this.b = 42;
		}

		public Ambiguous(int a, int b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj || (
				obj instanceof Ambiguous that &&
				this.a == that.a &&
				this.b == that.b
			);
		}
	}

	@Test
	public void testFactory() throws DecodeException {
		assertFailure(new ReifiedType<>() {}, new UseFactory(1, 2, true));
		assertSuccess(new ReifiedType<@RecordLike(value = { "a", "b" }, name = "create") UseFactory>() {}, new UseFactory(1, 2, true));
		assertSuccess(new ReifiedType<@RecordLike(value = { "a", "b" }, name = "createUseFactory", in = RecordLikeTest.class) UseFactory>() {}, new UseFactory(1, 2, true));
	}

	public static class UseFactory {

		public int a, b;

		public UseFactory(int a, int b) {
			throw new RuntimeException("Wrong constructor!");
		}

		@Hidden
		public UseFactory(int a, int b, boolean ignored) {
			this.a = a;
			this.b = b;
		}

		public static UseFactory create(int a, int b) {
			return new UseFactory(a, b, true);
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj || (
				obj instanceof UseFactory that &&
				this.a == that.a &&
				this.b == that.b
			);
		}
	}

	public static UseFactory createUseFactory(int a, int b) {
		return new UseFactory(a, b, true);
	}
}