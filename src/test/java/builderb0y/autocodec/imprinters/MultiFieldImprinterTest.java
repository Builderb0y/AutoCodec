package builderb0y.autocodec.imprinters;

import java.util.Objects;

import org.junit.Test;

import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;

public class MultiFieldImprinterTest {

	@Test
	public void testFinals() throws DecodeException {
		Base base = new Base();
		base.default_a = "b";
		base.category.default_true = false;
		base.category.default_false = true;
		base.category.category2.default_b = "a";
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Base.class).test(base);
	}

	public static class Base {

		public String default_a = "a";
		public final Category category = new Category();

		@Override
		public boolean equals(Object obj) {
			return this == obj || (
				obj instanceof Base that &&
				Objects.equals(this.default_a, that.default_a) &&
				Objects.equals(this.category, that.category)
			);
		}
	}

	public static class Category {

		public boolean default_true = true;
		public boolean default_false = false;
		public final Category2 category2 = new Category2();

		@Override
		public boolean equals(Object obj) {
			return this == obj || (
				obj instanceof Category that &&
				this.default_true == that.default_true &&
				this.default_false == that.default_false &&
				Objects.equals(this.category2, that.category2)
			);
		}
	}

	public static class Category2 {

		public String default_b = "b";

		@Override
		public boolean equals(Object obj) {
			return this == obj || (
				obj instanceof Category2 that &&
				Objects.equals(this.default_b, that.default_b)
			);
		}
	}
}