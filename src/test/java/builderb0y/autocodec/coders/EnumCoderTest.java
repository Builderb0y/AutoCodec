package builderb0y.autocodec.coders;

import org.junit.Test;

import builderb0y.autocodec.annotations.ForceOrdinal;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.data.DataOps;
import builderb0y.autocodec.data.NumberData;
import builderb0y.autocodec.data.StringData;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class EnumCoderTest {

	@Test
	public void test() {
		assertEquals(new StringData("RED"), TestCommon.DEFAULT_CODEC.encode(TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<PrimaryColor>() {}), PrimaryColor.RED, DataOps.UNCOMPRESSED));
		assertEquals(new NumberData(0),     TestCommon.DEFAULT_CODEC.encode(TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<PrimaryColor>() {}), PrimaryColor.RED, DataOps.COMPRESSED));
		assertEquals(new StringData("RED"), TestCommon.DEFAULT_CODEC.encode(TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<@ForceOrdinal(false) PrimaryColor>() {}), PrimaryColor.RED, DataOps.UNCOMPRESSED));
		assertEquals(new NumberData(0),     TestCommon.DEFAULT_CODEC.encode(TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<@ForceOrdinal(true)  PrimaryColor>() {}), PrimaryColor.RED, DataOps.COMPRESSED));
		assertEquals(new StringData("RED"), TestCommon.DEFAULT_CODEC.encode(TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<@ForceOrdinal(false) PrimaryColor>() {}), PrimaryColor.RED, DataOps.COMPRESSED));
		assertEquals(new NumberData(0),     TestCommon.DEFAULT_CODEC.encode(TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<@ForceOrdinal(true)  PrimaryColor>() {}), PrimaryColor.RED, DataOps.UNCOMPRESSED));
	}

	public static enum PrimaryColor {
		RED,
		GREEN,
		BLUE;
	}
}