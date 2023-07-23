package builderb0y.autocodec.coders;

import java.util.Objects;
import java.util.function.BiPredicate;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectOps;

import static org.junit.Assert.*;

public class CoderUnitTester<T_Decoded> {

	public final AutoCodec autoCodec;
	public final AutoCoder<T_Decoded> coder;
	public BiPredicate<T_Decoded, T_Decoded> equality = Objects::deepEquals;

	public CoderUnitTester(AutoCodec autoCodec, ReifiedType<T_Decoded> type) {
		this.autoCodec = autoCodec;
		this.coder = autoCodec.createCoder(type);
	}

	public CoderUnitTester(AutoCodec autoCodec, Class<T_Decoded> clazz) {
		this(autoCodec, ReifiedType.from(clazz));
	}

	public AutoEncoder<T_Decoded> encoder() {
		return TestCommon.encoder(this.coder);
	}

	public AutoDecoder<T_Decoded> decoder() {
		return TestCommon.decoder(this.coder);
	}

	public void test(T_Decoded object) throws DecodeException {
		this.test(object,   JsonOps.INSTANCE);
		this.test(object,   JsonOps.COMPRESSED);
		this.test(object, ObjectOps.INSTANCE);
		this.test(object, ObjectOps.COMPRESSED);
	}

	public <T_Encoded> void test(T_Decoded object, DynamicOps<T_Encoded> ops) throws DecodeException {
		T_Encoded element = this.autoCodec.encode(this.coder, object, ops);
		T_Decoded decoded = this.autoCodec.decode(this.coder, element, ops);
		assertTrue(this.equality.test(object, decoded));
	}

	public <T_Encoded> void test(T_Decoded decoded, T_Encoded encoded, DynamicOps<T_Encoded> ops) throws EncodeException, DecodeException {
		T_Encoded newEncoded = this.autoCodec.encode(this.coder, decoded, ops);
		assertEquals(encoded, newEncoded);
		T_Decoded newDecoded = this.autoCodec.decode(this.coder, encoded, ops);
		assertEquals(decoded, newDecoded);
	}

	public <T_Encoded> void testEncoded(T_Encoded input, DynamicOps<T_Encoded> ops) throws EncodeException, DecodeException {
		T_Decoded decoded = this.autoCodec.decode(this.coder, input, ops);
		T_Encoded encoded = this.autoCodec.encode(this.coder, decoded, ops);
		assertEquals(input, encoded);
	}
}