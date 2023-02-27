package builderb0y.autocodec.constructors;

import java.util.EnumMap;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectOps;

import static org.junit.Assert.*;

public class EnumMapConstructorTest {

	@Test
	public void test() throws DecodeException {
		AutoConstructor<EnumMap<Color, String>> constructor = TestCommon.DEFAULT_CODEC.createConstructor(new ReifiedType<>() {});
		assertTrue(constructor instanceof EnumMapConstructor);
		assertTrue(TestCommon.DEFAULT_CODEC.construct(constructor, new JsonObject(), JsonOps.INSTANCE) instanceof EnumMap);
		CoderUnitTester<EnumMap<Color, String>> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<>() {}) {

			@Override
			public void test(EnumMap<Color, String> object) throws DecodeException {
				this.test(object, JsonOps.INSTANCE);
				//this.test(object, JsonOps.COMPRESSED);
				this.test(object, ObjectOps.INSTANCE);
				//this.test(object, ObjectOps.COMPRESSED);
			}
		};
		EnumMap<Color, String> object = new EnumMap<>(Color.class);
		tester.test(object);
		object.put(Color.RED, "red");
		tester.test(object);
		object.put(Color.GREEN, "green");
		tester.test(object);
		object.put(Color.BLUE, "blue");
		tester.test(object);
	}

	public static enum Color {
		RED,
		GREEN,
		BLUE;
	}
}