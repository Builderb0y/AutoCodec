package builderb0y.autocodec.annotations;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.JsonBuilder.JsonObjectBuilder;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.verifiers.VerifyException;

import static org.junit.Assert.*;

public class AliasTest {

	@Test
	public void testAliases() throws DecodeException {
		AutoCoder<AliasHolder> coder = TestCommon.DEFAULT_CODEC.createCoder(AliasHolder.class);
		AliasHolder target = new AliasHolder(42);
		JsonElement json;

		json = new JsonObjectBuilder().add("d", 42).build();
		try {
			TestCommon.DISABLED_CODEC.decode(coder, json, JsonOps.INSTANCE);
			fail();
		}
		catch (VerifyException expected) {}

		json = new JsonObjectBuilder().add("c", 42).build();
		assertEquals(target, TestCommon.DEFAULT_CODEC.decode(coder, json, JsonOps.INSTANCE));

		json = new JsonObjectBuilder().add("b", 42).build();
		assertEquals(target, TestCommon.DEFAULT_CODEC.decode(coder, json, JsonOps.INSTANCE));

		json = new JsonObjectBuilder().add("a", 42).build();
		assertEquals(target, TestCommon.DEFAULT_CODEC.decode(coder, json, JsonOps.INSTANCE));

		assertEquals(json, TestCommon.DEFAULT_CODEC.encode(coder, target, JsonOps.INSTANCE));
	}

	@Test
	public void testPriority() throws DecodeException {
		AutoCoder<AliasHolder> coder = TestCommon.DEFAULT_CODEC.createCoder(AliasHolder.class);
		AliasHolder target = new AliasHolder(1);
		JsonElement json = new JsonObjectBuilder().add("c", 3).add("b", 2).add("a", 1).build();
		assertEquals(target, TestCommon.DEFAULT_CODEC.decode(coder, json, JsonOps.INSTANCE));
	}

	public static record AliasHolder(@Alias({ "b", "c" }) int a) {}

	@Test
	public void testNamedAliases() throws DecodeException {
		AutoCoder<NamedAliasHolder> coder = TestCommon.DEFAULT_CODEC.createCoder(NamedAliasHolder.class);
		NamedAliasHolder target = new NamedAliasHolder(42);
		JsonElement json;

		json = new JsonObjectBuilder().add("d", 42).build();
		try {
			TestCommon.DISABLED_CODEC.decode(coder, json, JsonOps.INSTANCE);
			fail();
		}
		catch (VerifyException expected) {}

		json = new JsonObjectBuilder().add("z", 42).build();
		try {
			TestCommon.DISABLED_CODEC.decode(coder, json, JsonOps.INSTANCE);
			fail();
		}
		catch (VerifyException expected) {}

		json = new JsonObjectBuilder().add("c", 42).build();
		assertEquals(target, TestCommon.DEFAULT_CODEC.decode(coder, json, JsonOps.INSTANCE));

		json = new JsonObjectBuilder().add("b", 42).build();
		assertEquals(target, TestCommon.DEFAULT_CODEC.decode(coder, json, JsonOps.INSTANCE));

		json = new JsonObjectBuilder().add("a", 42).build();
		assertEquals(target, TestCommon.DEFAULT_CODEC.decode(coder, json, JsonOps.INSTANCE));

		assertEquals(json, TestCommon.DEFAULT_CODEC.encode(coder, target, JsonOps.INSTANCE));
	}

	@Test
	public void testNamedPriority() throws DecodeException {
		AutoCoder<AliasHolder> coder = TestCommon.DEFAULT_CODEC.createCoder(AliasHolder.class);
		AliasHolder target = new AliasHolder(1);
		JsonElement json = new JsonObjectBuilder().add("z", 26).add("c", 3).add("b", 2).add("a", 1).build();
		assertEquals(target, TestCommon.DEFAULT_CODEC.decode(coder, json, JsonOps.INSTANCE));
	}

	public static record NamedAliasHolder(@Alias({ "b", "c" }) @UseName("a") int z) {}
}