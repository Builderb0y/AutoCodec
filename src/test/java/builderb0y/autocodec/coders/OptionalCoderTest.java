package builderb0y.autocodec.coders;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.Test;

import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.data.DataOps;
import builderb0y.autocodec.data.EmptyData;
import builderb0y.autocodec.data.NumberData;
import builderb0y.autocodec.data.StringData;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class OptionalCoderTest {

	@Test
	public void testObjects() throws DecodeException {
		AutoCoder<Optional<String>> coder = TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<Optional<String>>() {});
		assertEquals(Optional.of("hi"), TestCommon.DEFAULT_CODEC.decode(coder, new StringData("hi"), DataOps.UNCOMPRESSED));
		assertEquals(Optional.empty(), TestCommon.DEFAULT_CODEC.decode(coder, EmptyData.INSTANCE, DataOps.UNCOMPRESSED));
		assertEquals(new StringData("hi"), TestCommon.DEFAULT_CODEC.encode(coder, Optional.of("hi"), DataOps.UNCOMPRESSED));
		assertEquals(EmptyData.INSTANCE, TestCommon.DEFAULT_CODEC.encode(coder, Optional.empty(), DataOps.UNCOMPRESSED));
	}

	@Test
	public void testInts() throws DecodeException {
		AutoCoder<OptionalInt> coder = TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<OptionalInt>() {});
		assertEquals(OptionalInt.of(42), TestCommon.DEFAULT_CODEC.decode(coder, new NumberData(42), DataOps.UNCOMPRESSED));
		assertEquals(OptionalInt.empty(), TestCommon.DEFAULT_CODEC.decode(coder, EmptyData.INSTANCE, DataOps.UNCOMPRESSED));
		assertEquals(new NumberData(42), TestCommon.DEFAULT_CODEC.encode(coder, OptionalInt.of(42), DataOps.UNCOMPRESSED));
		assertEquals(EmptyData.INSTANCE, TestCommon.DEFAULT_CODEC.encode(coder, OptionalInt.empty(), DataOps.UNCOMPRESSED));
	}

	@Test
	public void testLongs() throws DecodeException {
		AutoCoder<OptionalLong> coder = TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<OptionalLong>() {});
		assertEquals(OptionalLong.of(42L), TestCommon.DEFAULT_CODEC.decode(coder, new NumberData(42L), DataOps.UNCOMPRESSED));
		assertEquals(OptionalLong.empty(), TestCommon.DEFAULT_CODEC.decode(coder, EmptyData.INSTANCE, DataOps.UNCOMPRESSED));
		assertEquals(new NumberData(42L), TestCommon.DEFAULT_CODEC.encode(coder, OptionalLong.of(42L), DataOps.UNCOMPRESSED));
		assertEquals(EmptyData.INSTANCE, TestCommon.DEFAULT_CODEC.encode(coder, OptionalLong.empty(), DataOps.UNCOMPRESSED));
	}

	@Test
	public void testDoubles() throws DecodeException {
		AutoCoder<OptionalDouble> coder = TestCommon.DEFAULT_CODEC.createCoder(new ReifiedType<OptionalDouble>() {});
		assertEquals(OptionalDouble.of(42.0D), TestCommon.DEFAULT_CODEC.decode(coder, new NumberData(42.0D), DataOps.UNCOMPRESSED));
		assertEquals(OptionalDouble.empty(), TestCommon.DEFAULT_CODEC.decode(coder, EmptyData.INSTANCE, DataOps.UNCOMPRESSED));
		assertEquals(new NumberData(42.0D), TestCommon.DEFAULT_CODEC.encode(coder, OptionalDouble.of(42.0D), DataOps.UNCOMPRESSED));
		assertEquals(EmptyData.INSTANCE, TestCommon.DEFAULT_CODEC.encode(coder, OptionalDouble.empty(), DataOps.UNCOMPRESSED));
	}
}