package builderb0y.autocodec.coders;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseCoder;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class LookupCoderTest {

	@Test
	public void testLookup() throws DecodeException {
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Number.class).test(new Number(1), new JsonPrimitive("one"), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Number.class).test(new Number(2), new JsonPrimitive("two"), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Number.class).test(new Number(3), new JsonPrimitive("three"), JsonOps.INSTANCE);
	}

	@UseCoder(name = "CODER", usage = MemberUsage.FIELD_CONTAINS_HANDLER)
	public static record Number(int value) {

		public static final LookupCoder<String, Number> CODER = new LookupCoder<>(ReifiedType.from(Number.class), PrimitiveCoders.STRING);
		static {
			CODER.add("one", new Number(1));
			CODER.add("two", new Number(2));
			CODER.add("three", new Number(3));
		}
	}
}