package builderb0y.autocodec.decoders;

import java.util.Objects;

import org.junit.Test;

import builderb0y.autocodec.annotations.AddPseudoField;
import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseConstructor;
import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.constructors.ConstructContext;
import builderb0y.autocodec.encoders.MultiFieldEncoder;

import static org.junit.Assert.*;

public class ConstructImprintTest {


	@Test
	public void testAllTheThings() throws DecodeException {
		CoderUnitTester<Sub> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Sub.class);
		assertTrue(tester.encoder() instanceof MultiFieldEncoder);
		assertTrue(tester.decoder() instanceof ConstructImprintDecoder);
		Sub sub = new Sub();
		sub.x = 42;
		sub.value = "meaning of life";
		tester.test(sub);
	}

	@AddPseudoField(name = "value", getter = "getValue", setter = "setValue")
	public static interface I {

		public abstract String getValue();

		public abstract void setValue(String value);
	}

	public static class Base {

		public int x;
	}

	@UseConstructor(name = "create", usage = MemberUsage.METHOD_IS_HANDLER)
	public static class Sub extends Base implements I {

		public transient String value;

		public static <T_Encoded> Sub create(ConstructContext<T_Encoded> context) {
			//System.out.println("Called factory method");
			return new Sub();
		}

		@Override
		public String getValue() {
			//System.out.println("Called getter");
			return this.value;
		}

		@Override
		public void setValue(String value) {
			//System.out.println("Called setter");
			this.value = value;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.value) + this.x;
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj || (
				obj instanceof Sub that &&
				this.x == that.x &&
				Objects.equals(this.value, that.value)
			);
		}
	}
}