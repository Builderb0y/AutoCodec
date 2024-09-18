package builderb0y.autocodec.encoders;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.DefaultEmpty;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class DefaultEmptyEncoderTest {

	@Test
	public void testArrays() {
		JsonElement encoded = TestCommon.DEFAULT_CODEC.encode(
			TestCommon.DEFAULT_CODEC.createCoder(
				new ReifiedType<int @DefaultEmpty []>() {}
			),
			new int[0],
			JsonOps.INSTANCE
		);
		assertSame(JsonNull.INSTANCE, encoded);
	}

	@Test
	public void testLists() {
		JsonElement encoded = TestCommon.DEFAULT_CODEC.encode(
			TestCommon.DEFAULT_CODEC.createCoder(
				new ReifiedType<@DefaultEmpty List<String>>() {}
			),
			Collections.emptyList(),
			JsonOps.INSTANCE
		);
		assertSame(JsonNull.INSTANCE, encoded);
	}

	@Test
	public void testMaps() {
		JsonElement encoded = TestCommon.DEFAULT_CODEC.encode(
			TestCommon.DEFAULT_CODEC.createCoder(
				new ReifiedType<@DefaultEmpty Map<String, String>>() {}
			),
			Collections.emptyMap(),
			JsonOps.INSTANCE
		);
		assertSame(JsonNull.INSTANCE, encoded);
	}
}