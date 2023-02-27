package builderb0y.autocodec.common;

import java.lang.reflect.Array;
import java.util.Objects;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.VerifyFloatRange;
import builderb0y.autocodec.annotations.VerifyIntRange;
import builderb0y.autocodec.annotations.VerifySizeRange;
import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.decoders.ArrayDecoder;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.ArrayEncoder;
import builderb0y.autocodec.imprinters.ArrayImprinter;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.imprinters.ImprintException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectOps;
import builderb0y.autocodec.verifiers.VerifyException;

import static org.junit.Assert.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ArrayEncoderDecoderTest {

	@Test
	public void testBasicArrays() throws DecodeException {
		this.testPrimitiveArray(new byte   [] { 1, 2, 3 });
		this.testPrimitiveArray(new short  [] { 1, 2, 3 });
		this.testPrimitiveArray(new int    [] { 1, 2, 3 });
		this.testPrimitiveArray(new long   [] { 1, 2, 3 });
		this.testPrimitiveArray(new float  [] { 1, 2, 3 });
		this.testPrimitiveArray(new double [] { 1, 2, 3 });
		this.testPrimitiveArray(new char   [] { 'a', 'b', 'c' });
		this.testPrimitiveArray(new boolean[] { true, false });
	}

	public void testPrimitiveArray(Object array) throws DecodeException {
		CoderUnitTester tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, array.getClass());
		assertTrue(tester.encoder() instanceof ArrayEncoder<?, ?>);
		assertTrue(tester.decoder() instanceof ArrayDecoder<?, ?>);
		tester.test(array);

		AutoImprinter<Object> imprinter = (AutoImprinter<Object>)(TestCommon.DEFAULT_CODEC.createImprinter(array.getClass()));
		assertTrue(TestCommon.imprinter(imprinter) instanceof ArrayImprinter<?, ?>);
		for (DynamicOps ops : new DynamicOps[] { JsonOps.INSTANCE, JsonOps.COMPRESSED, ObjectOps.INSTANCE, ObjectOps.COMPRESSED }) {
			Object toImprint = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array));
			Object encoded = TestCommon.DEFAULT_CODEC.encode(tester.coder, array, ops);
			TestCommon.DEFAULT_CODEC.imprint(imprinter, toImprint, encoded, ops);
			assertTrue(Objects.deepEquals(array, toImprint));

			toImprint = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array) - 1);
			try {
				TestCommon.DISABLED_CODEC.imprint(imprinter, toImprint, encoded, ops);
				fail();
			}
			catch (ImprintException expected) {}

			toImprint = Array.newInstance(array.getClass().getComponentType(), Array.getLength(array) + 1);
			try {
				TestCommon.DISABLED_CODEC.imprint(imprinter, toImprint, encoded, ops);
				fail();
			}
			catch (ImprintException expected) {}
		}
	}

	@Test
	public void testVerifyingArrays() throws DecodeException {
		this.testVerifyingArray(new byte  [] {  1,  2,  3 }, new ReifiedType<@VerifyIntRange  (min = 1, max = 3) byte  []>() {}, true, false);
		this.testVerifyingArray(new short [] {  1,  2,  3 }, new ReifiedType<@VerifyIntRange  (min = 1, max = 3) short []>() {}, true, false);
		this.testVerifyingArray(new int   [] {  1,  2,  3 }, new ReifiedType<@VerifyIntRange  (min = 1, max = 3) int   []>() {}, true, false);
		this.testVerifyingArray(new long  [] {  1,  2,  3 }, new ReifiedType<@VerifyIntRange  (min = 1, max = 3) long  []>() {}, true, false);
		this.testVerifyingArray(new float [] {  1,  2,  3 }, new ReifiedType<@VerifyFloatRange(min = 1, max = 3) float []>() {}, true, false);
		this.testVerifyingArray(new double[] {  1,  2,  3 }, new ReifiedType<@VerifyFloatRange(min = 1, max = 3) double[]>() {}, true, false);

		this.testVerifyingArray(new byte  [] {  1,  2,  3 }, new ReifiedType<byte   @VerifySizeRange(min = 1, max = 3) []>() {}, true, true);
		this.testVerifyingArray(new short [] {  1,  2,  3 }, new ReifiedType<short  @VerifySizeRange(min = 1, max = 3) []>() {}, true, true);
		this.testVerifyingArray(new int   [] {  1,  2,  3 }, new ReifiedType<int    @VerifySizeRange(min = 1, max = 3) []>() {}, true, true);
		this.testVerifyingArray(new long  [] {  1,  2,  3 }, new ReifiedType<long   @VerifySizeRange(min = 1, max = 3) []>() {}, true, true);
		this.testVerifyingArray(new float [] {  1,  2,  3 }, new ReifiedType<float  @VerifySizeRange(min = 1, max = 3) []>() {}, true, true);
		this.testVerifyingArray(new double[] {  1,  2,  3 }, new ReifiedType<double @VerifySizeRange(min = 1, max = 3) []>() {}, true, true);

		this.testVerifyingArray(new byte  [] { 10, 11, 12 }, new ReifiedType<@VerifyIntRange  (min = 1, max = 3) byte  []>() {}, false, false);
		this.testVerifyingArray(new short [] { 10, 11, 12 }, new ReifiedType<@VerifyIntRange  (min = 1, max = 3) short []>() {}, false, false);
		this.testVerifyingArray(new int   [] { 10, 11, 12 }, new ReifiedType<@VerifyIntRange  (min = 1, max = 3) int   []>() {}, false, false);
		this.testVerifyingArray(new long  [] { 10, 11, 12 }, new ReifiedType<@VerifyIntRange  (min = 1, max = 3) long  []>() {}, false, false);
		this.testVerifyingArray(new float [] { 10, 11, 12 }, new ReifiedType<@VerifyFloatRange(min = 1, max = 3) float []>() {}, false, false);
		this.testVerifyingArray(new double[] { 10, 11, 12 }, new ReifiedType<@VerifyFloatRange(min = 1, max = 3) double[]>() {}, false, false);

		this.testVerifyingArray(new byte  [] { 10, 11, 12 }, new ReifiedType<byte   @VerifySizeRange(min = 10) []>() {}, false, true);
		this.testVerifyingArray(new short [] { 10, 11, 12 }, new ReifiedType<short  @VerifySizeRange(min = 10) []>() {}, false, true);
		this.testVerifyingArray(new int   [] { 10, 11, 12 }, new ReifiedType<int    @VerifySizeRange(min = 10) []>() {}, false, true);
		this.testVerifyingArray(new long  [] { 10, 11, 12 }, new ReifiedType<long   @VerifySizeRange(min = 10) []>() {}, false, true);
		this.testVerifyingArray(new float [] { 10, 11, 12 }, new ReifiedType<float  @VerifySizeRange(min = 10) []>() {}, false, true);
		this.testVerifyingArray(new double[] { 10, 11, 12 }, new ReifiedType<double @VerifySizeRange(min = 10) []>() {}, false, true);
	}

	public void testVerifyingArray(Object array, ReifiedType<?> type, boolean valid, boolean arrayChecked) throws DecodeException {
		CoderUnitTester tester = new CoderUnitTester(valid ? TestCommon.DEFAULT_CODEC : TestCommon.DISABLED_CODEC, type);
		assertTrue(tester.encoder() instanceof ArrayEncoder<?, ?>);
		assertTrue(tester.decoder() instanceof ArrayDecoder<?, ?>);
		if (valid) {
			tester.test(array);
		}
		else try {
			tester.test(array);
		}
		catch (VerifyException expected) {}
	}
}