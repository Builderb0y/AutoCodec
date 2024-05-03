package builderb0y.autocodec.coders;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.common.JsonBuilder;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.util.DFUVersions;

import static org.junit.Assert.*;

public class MapCodecTest {

	@Test
	public void test() {
		Data data = new Data(42, "meaning of life");
		JsonObject expected = JsonBuilder.object("foo", 42, "bar", "meaning of life");
		JsonElement actual = DFUVersions.getResult(TestCommon.DEFAULT_CODEC.createDFUMapCodec(Data.class).encode(data, JsonOps.INSTANCE, JsonOps.INSTANCE.mapBuilder()).build(JsonNull.INSTANCE));
		assertEquals(expected, actual);
	}

	public static record Data(int foo, String bar) {}
}