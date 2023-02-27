package builderb0y.autocodec.constructors;

import java.util.EnumSet;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class EnumSetConstructorTest {

	@Test
	public void test() throws DecodeException {
		AutoConstructor<EnumSet<Color>> constructor = TestCommon.DEFAULT_CODEC.createConstructor(new ReifiedType<>() {});
		assertTrue(constructor instanceof EnumSetConstructor);
		assertTrue(TestCommon.DEFAULT_CODEC.construct(constructor, new JsonObject(), JsonOps.INSTANCE) instanceof EnumSet);

		CoderUnitTester<EnumSet<Color>> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<>() {});
		EnumSet<Color> object = EnumSet.noneOf(Color.class);
		tester.test(object);
		object.add(Color.RED);
		tester.test(object);
		object.add(Color.GREEN);
		tester.test(object);
		object.add(Color.BLUE);
		tester.test(object);
	}

	public static enum Color {
		RED,
		GREEN,
		BLUE;
	}
}