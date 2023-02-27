package builderb0y.autocodec.common;

import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.UseName;
import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class UseNameTest {

	@Test
	public void test() throws DecodeException {
		CoderUnitTester<Box<String>> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@UseName("string") String>>() {});
		tester.test(new Box<>("foo"), JsonBuilder.object("string", "foo"), JsonOps.INSTANCE);
	}

	public static record Box<T>(T value) {}
}