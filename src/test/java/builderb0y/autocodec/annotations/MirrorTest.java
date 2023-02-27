package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.VerifyException;

import static org.junit.Assert.*;

public class MirrorTest {

	@Test
	public void testBasic() throws DecodeException {
		AutoDecoder<List<String>> decoder = TestCommon.DEFAULT_CODEC.createDecoder(
			new ReifiedType<@A List<String>>() {}
		);
		List<String> decoded = TestCommon.DEFAULT_CODEC.decode(decoder, new JsonPrimitive("test"), JsonOps.INSTANCE);
		assertTrue(decoded instanceof LinkedList<?>);
		assertEquals("test", decoded.get(0));
		try {
			TestCommon.DISABLED_CODEC.decode(decoder, JsonNull.INSTANCE, JsonOps.INSTANCE);
			fail();
		}
		catch (VerifyException expected) {}
	}

	@Test
	public void testNested() throws DecodeException {
		AutoDecoder<List<String>> decoder = TestCommon.DEFAULT_CODEC.createDecoder(
			new ReifiedType<@B List<String>>() {}
		);
		List<String> decoded = TestCommon.DEFAULT_CODEC.decode(decoder, new JsonPrimitive("test"), JsonOps.INSTANCE);
		assertTrue(decoded instanceof LinkedList<?>);
		assertEquals("test", decoded.get(0));
		try {
			TestCommon.DISABLED_CODEC.decode(decoder, JsonNull.INSTANCE, JsonOps.INSTANCE);
			fail();
		}
		catch (VerifyException expected) {}
	}

	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	@SingletonArray
	@VerifyNullable
	@UseImplementation(LinkedList.class)
	@Mirror({ UseImplementation.class, SingletonArray.class })
	public static @interface A {}

	@Target(ElementType.TYPE_USE)
	@Retention(RetentionPolicy.RUNTIME)
	@UseImplementation(ArrayList.class) //should NOT be mirrored.
	@A
	@Mirror(A.class)
	public static @interface B {}
}