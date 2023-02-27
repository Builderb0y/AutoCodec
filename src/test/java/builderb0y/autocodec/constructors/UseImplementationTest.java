package builderb0y.autocodec.constructors;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.UseImplementation;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class UseImplementationTest {

	@Test
	public void test() throws ConstructException {
		AutoConstructor<X<Object, String>> constructor = TestCommon.DEFAULT_CODEC.createConstructor(
			new ReifiedType<@UseImplementation(Y.class) X<Object, String>>() {}
		);
		assertEquals("builderb0y.autocodec.constructors.UseImplementationTest$Y<java.lang.String, ? extends java.lang.Object>::new", constructor.toString());
		assertTrue(TestCommon.DEFAULT_CODEC.construct(constructor, JsonNull.INSTANCE, JsonOps.INSTANCE) instanceof Y<String, ?>);
	}

	public static class X<A, B> {}

	public static class Y<B, C> extends X<Object, B> {}
}