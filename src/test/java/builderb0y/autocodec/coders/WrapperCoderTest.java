package builderb0y.autocodec.coders;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.VerifyNullable;
import builderb0y.autocodec.annotations.Wrapper;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class WrapperCoderTest {

	@Test
	public void test() throws DecodeException {
		JsonElement encoded = new JsonPrimitive("I'm a box!");
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<String>>() {}).test(new Box<>("I'm a box!"), encoded, JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<HashBox<String>>() {}).test(new HashBox<>("I'm a box!"), encoded, JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<HashBox2<String>>() {}).test(new HashBox2<>("I'm a box!"), encoded, JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<@VerifyNullable Box<String>>() {}).test(null, JsonNull.INSTANCE, JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<ClassBox<String>>() {}).test(new ClassBox<>("I'm a box!"), encoded, JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<@VerifyNullable ClassBox<String>>() {}).test(null, JsonNull.INSTANCE, JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<@Wrapper(wrapNull = true) Box<String>>() {}).test(new Box<>(null), JsonNull.INSTANCE, JsonOps.INSTANCE);
	}

	@Wrapper
	public static record Box<T>(T value) {}

	@Wrapper
	public static record HashBox<T>(T value, int hash) {

		public HashBox(T value) {
			this(value, value.hashCode());
		}
	}

	@Wrapper("value")
	public static record HashBox2<T>(T value, int hash) {

		public HashBox2(T value) {
			this(value, value.hashCode());
		}

		public HashBox2(int hash) {
			this(null, hash);
		}
	}

	@Wrapper
	public static class ClassBox<T> {

		public T value;

		public ClassBox(T value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj || (
				obj instanceof ClassBox<?> that &&
				Objects.equals(this.value, that.value)
			);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.value);
		}
	}
}