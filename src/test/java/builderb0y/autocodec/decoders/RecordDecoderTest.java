package builderb0y.autocodec.decoders;

import org.junit.Test;

import builderb0y.autocodec.annotations.AddPseudoField;
import builderb0y.autocodec.annotations.Hidden;
import builderb0y.autocodec.annotations.VerifyNullable;
import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.encoders.MultiFieldEncoder;

import static org.junit.Assert.*;

public class RecordDecoderTest {

	@Test
	public void testEmptyRecord() throws DecodeException {
		CoderUnitTester<EmptyRecord> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, EmptyRecord.class);
		assertTrue(tester.encoder() instanceof MultiFieldEncoder<?>);
		assertTrue(tester.decoder() instanceof RecordDecoder<?>);
		tester.test(new EmptyRecord());
	}

	public static record EmptyRecord() {

		public static final EmptyRecord STATIC = new EmptyRecord();
	}

	@Test
	public void testPoint() throws DecodeException {
		CoderUnitTester<PointXY> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, PointXY.class);
		assertTrue(tester.encoder() instanceof MultiFieldEncoder<?>);
		assertTrue(tester.decoder() instanceof RecordDecoder<?>);
		tester.test(new PointXY(3, 4));
	}

	public static record PointXY(int x, int y) {

		public static final PointXY STATIC = new PointXY(0, 0);
	}

	@Test
	public void testNested() throws DecodeException {
		CoderUnitTester<Nested> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Nested.class);
		for (int maxDepth = 0; maxDepth <= 5; maxDepth++) {
			Nested nested = new Nested(null, 0);
			for (int depth = 1; depth <= maxDepth; depth++) {
				nested = new Nested(nested, depth);
			}
			tester.test(nested);
		}
	}

	public static record Nested(@VerifyNullable Nested parent, int value) {

		public static final Nested STATIC = new Nested(null, 0);
	}

	@Test
	public void testEncodedPoints() throws DecodeException {
		try {
			TestCommon.DISABLED_CODEC.createDecoder(BadEncodedPoint.class);
			fail();
		}
		catch (FactoryException expected) {}

		CoderUnitTester<GoodEncodedPoint> good = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, GoodEncodedPoint.class);
		good.test(new GoodEncodedPoint(3, 4));

		CoderUnitTester<PseudoEncodedPoint> pseudo = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, PseudoEncodedPoint.class);
		pseudo.test(new PseudoEncodedPoint(3, 4, 0L));
	}

	public static record BadEncodedPoint(int x, int y, @Hidden long encoded) {}

	public static record GoodEncodedPoint(int x, int y, @Hidden long encoded) {

		public GoodEncodedPoint(int x, int y) {
			this(x, y, (((long)(y)) << 32) | (((long)(x)) & 0xFFFF_FFFFL));
		}
	}

	@AddPseudoField(name = "encoded", getter = "getEncoded", setter = "setEncoded")
	public static record PseudoEncodedPoint(int x, int y, @Hidden LongBox encoded) {

		public PseudoEncodedPoint(int x, int y, long encoded) {
			this(x, y, new LongBox(encoded));
		}

		public long getEncoded() {
			return this.encoded.value;
		}

		public void setEncoded(long value) {
			this.encoded.value = value;
		}
	}

	public static class LongBox {

		public long value;

		public LongBox(long value) {
			this.value = value;
		}

		public LongBox() {}

		@Override
		public int hashCode() {
			return Long.hashCode(this.value);
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj || (obj instanceof LongBox that && this.value == that.value);
		}

		@Override
		public String toString() {
			return "LongBox: { value: " + this.value + " }";
		}
	}
}