package builderb0y.autocodec.common;

import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.EncodeInline;
import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.decoders.DecodeException;

public class InlineTest {

	@Test
	public void testInline() throws DecodeException {
		CoderUnitTester<Point3D> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Point3D.class);
		tester.test(new Point3D(new Point2D(new Point1D(1), 2), 3));
		tester.test(
			new Point3D(new Point2D(new Point1D(1), 2), 3),
			JsonBuilder.object("x", 1, "y", 2, "z", 3),
			JsonOps.INSTANCE
		);
	}

	@Test
	public void testGenericInline() throws DecodeException {
		CoderUnitTester<MultiBox> tester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, MultiBox.class);
		tester.test(new MultiBox(new Box<>(new Point1D(1)), new Box<>(new Point1D(2))));
		tester.test(
			new MultiBox(new Box<>(new Point1D(1)), new Box<>(new Point1D(2))),
			JsonBuilder.object(
				"normal", JsonBuilder.object("value", JsonBuilder.object("x", 1)),
				"inline", JsonBuilder.object("x", 2)
			),
			JsonOps.INSTANCE
		);
	}

	public static record Point1D(int x) {}

	public static record Point2D(@EncodeInline Point1D x, int y) {}

	public static record Point3D(@EncodeInline Point2D xy, int z) {}

	public static record Box<T>(T value) {}

	public static record MultiBox(Box<Point1D> normal, Box<@EncodeInline Point1D> inline) {}
}