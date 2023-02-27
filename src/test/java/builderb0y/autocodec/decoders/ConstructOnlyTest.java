package builderb0y.autocodec.decoders;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.ConstructOnly;
import builderb0y.autocodec.common.JsonBuilder;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class ConstructOnlyTest {

	@Test
	public void test() throws DecodeException {
		JsonElement absentJson = JsonNull.INSTANCE;
		JsonElement presentJson = JsonBuilder.object("a", 1, "b", 2);
		Example defaultExample = new Example();
		Example modifiedExample = new Example();
		modifiedExample.a = 1;
		modifiedExample.b = 2;
		assertEquals(defaultExample, TestCommon.DEFAULT_CODEC.decode(TestCommon.DEFAULT_CODEC.createDecoder(new ReifiedType<@ConstructOnly Example>() {}), absentJson, JsonOps.INSTANCE));
		assertEquals(modifiedExample, TestCommon.DEFAULT_CODEC.decode(TestCommon.DEFAULT_CODEC.createDecoder(new ReifiedType<@ConstructOnly Example>() {}), presentJson, JsonOps.INSTANCE));
		assertEquals(defaultExample, TestCommon.DEFAULT_CODEC.decode(TestCommon.DEFAULT_CODEC.createDecoder(new ReifiedType<@ConstructOnly(onlyWhenNull = false) Example>() {}), absentJson, JsonOps.INSTANCE));
		assertEquals(defaultExample, TestCommon.DEFAULT_CODEC.decode(TestCommon.DEFAULT_CODEC.createDecoder(new ReifiedType<@ConstructOnly(onlyWhenNull = false) Example>() {}), presentJson, JsonOps.INSTANCE));
	}

	public static class Example {

		public int a = 123;
		public int b = 456;

		@Override
		public boolean equals(Object obj) {
			return this == obj || (
				obj instanceof Example that &&
				this.a == that.a &&
				this.b == that.b
			);
		}
	}
}