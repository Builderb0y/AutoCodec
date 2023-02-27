package builderb0y.autocodec.decoders;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.DefaultEmpty;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class DefaultEmptyDecoderTest {

	@Test
	public void testArrays() throws DecodeException {
		int[] decoded = TestCommon.DEFAULT_CODEC.decode(
			TestCommon.DEFAULT_CODEC.createDecoder(
				new ReifiedType<int @DefaultEmpty []>() {}
			),
			JsonNull.INSTANCE,
			JsonOps.INSTANCE
		);
		assertTrue(decoded != null && decoded.length == 0);

		AutoDecoder<int[]> sharedDecoder = TestCommon.DEFAULT_CODEC.createDecoder(
			new ReifiedType<int @DefaultEmpty(shared = true) []>() {}
		);
		int[] sharedArray1 = TestCommon.DEFAULT_CODEC.decode(sharedDecoder, JsonNull.INSTANCE, JsonOps.INSTANCE);
		int[] sharedArray2 = TestCommon.DEFAULT_CODEC.decode(sharedDecoder, JsonNull.INSTANCE, JsonOps.INSTANCE);
		assertSame(sharedArray1, sharedArray2);
	}

	@Test
	public void testLists() throws DecodeException {
		List<String> decoded = TestCommon.DEFAULT_CODEC.decode(
			TestCommon.DEFAULT_CODEC.createDecoder(
				new ReifiedType<@DefaultEmpty List<String>>() {}
			),
			JsonNull.INSTANCE,
			JsonOps.INSTANCE
		);
		assertTrue(decoded != null && decoded.isEmpty());

		AutoDecoder<List<String>> sharedDecoder = TestCommon.DEFAULT_CODEC.createDecoder(
			new ReifiedType<@DefaultEmpty(shared = true) List<String>>() {}
		);
		List<String> sharedArray1 = TestCommon.DEFAULT_CODEC.decode(sharedDecoder, JsonNull.INSTANCE, JsonOps.INSTANCE);
		List<String> sharedArray2 = TestCommon.DEFAULT_CODEC.decode(sharedDecoder, JsonNull.INSTANCE, JsonOps.INSTANCE);
		assertSame(sharedArray1, sharedArray2);
	}

	@Test
	public void testMaps() throws DecodeException {
		Map<String, String> decoded = TestCommon.DEFAULT_CODEC.decode(
			TestCommon.DEFAULT_CODEC.createDecoder(
				new ReifiedType<@DefaultEmpty Map<String, String>>() {}
			),
			JsonNull.INSTANCE,
			JsonOps.INSTANCE
		);
		assertTrue(decoded != null && decoded.isEmpty());

		AutoDecoder<Map<String, String>> sharedDecoder = TestCommon.DEFAULT_CODEC.createDecoder(
			new ReifiedType<@DefaultEmpty(shared = true) Map<String, String>>() {}
		);
		Map<String, String> sharedArray1 = TestCommon.DEFAULT_CODEC.decode(sharedDecoder, JsonNull.INSTANCE, JsonOps.INSTANCE);
		Map<String, String> sharedArray2 = TestCommon.DEFAULT_CODEC.decode(sharedDecoder, JsonNull.INSTANCE, JsonOps.INSTANCE);
		assertSame(sharedArray1, sharedArray2);
	}
}