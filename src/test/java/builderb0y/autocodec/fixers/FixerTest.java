package builderb0y.autocodec.fixers;

import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseFixer;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.JsonBuilder;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.decoders.DecodeException;

import static org.junit.Assert.*;

public class FixerTest {

	@Test
	public void testDecode() throws DecodeException {
		AutoCoder<Example> coder = TestCommon.DEFAULT_CODEC.createCoder(Example.class);
		TestCommon.DEFAULT_CODEC.decode(coder, JsonBuilder.object("version", 0, "foo", 1), JsonOps.INSTANCE);
		TestCommon.DEFAULT_CODEC.decode(coder, JsonBuilder.object("version", 0, "bar", 1), JsonOps.INSTANCE);
		try {
			TestCommon.DISABLED_CODEC.decode(coder, JsonBuilder.object("version", 0, "baz", 1), JsonOps.INSTANCE);
			fail("allowed baz");
		}
		catch (DecodeException expected) {}

		TestCommon.DEFAULT_CODEC.decode(coder, JsonBuilder.object("version", 1, "type", "foo", "value", 1), JsonOps.INSTANCE);

		TestCommon.DEFAULT_CODEC.decode(coder, JsonBuilder.object("version", 2, "name", "foo", "value", 1), JsonOps.INSTANCE);
	}

	@Test
	public void testEncode() {
		AutoCoder<Example> coder = TestCommon.DEFAULT_CODEC.createCoder(Example.class);
		assertEquals(
			JsonBuilder.object("version", Example.FIXER.version, "name", "foo", "value", 1),
			TestCommon.DEFAULT_CODEC.encode(coder, new Example("foo", 1), JsonOps.INSTANCE)
		);
	}

	@UseFixer(name = "FIXER", in = Example.class, usage = MemberUsage.FIELD_CONTAINS_HANDLER)
	public static record Example(String name, int value) {

		public static final VersionedFixer<Example> FIXER = new VersionedFixer<Example>("Example.FIXER", 2) {

			@Override
			public @NotNull <T_Encoded> DataFixContext<T_Encoded> fixData(@NotNull DataFixContext<T_Encoded> context, int version) throws DataFixException {
				switch (version) {
					default: break;
					case 0: this.fixV0(context);
					case 1: this.fixV1(context);
					case 2: //current.
				}
				return context;
			}

			public <T_Encoded> void fixV0(DataFixContext<T_Encoded> context) throws DataFixException {
				for (String search : new String[] { "foo", "bar" }) {
					Data removed = context.removeMember(search);
					if (!removed.isEmpty()) {
						int value = context.fork(search, removed).forceAsInt();
						context.putString("type", search);
						context.putInt("value", value);
						return;
					}
				}
				throw new DataFixException(() -> "Missing type foo or bar");
			}

			public <T_Encoded> void fixV1(DataFixContext<T_Encoded> context) throws DataFixException {
				context.putMember("name", context.removeMember("type"));
			}
		};
	}
}