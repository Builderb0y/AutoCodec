package builderb0y.autocodec.data;

import java.util.SplittableRandom;

import org.junit.Test;

import static org.junit.Assert.*;

public class NumberDataTest {

	@Test
	public void test() {
		SplittableRandom random = new SplittableRandom(12345L);
		NumberData data = new NumberData();
		for (int trial = 0; trial < 1_000_000; trial++) {
			byte b = (byte)(random.nextInt());
			data.set(b);
			assertEquals((byte)(b), data.byteValue());
			assertEquals((short)(b), data.shortValue());
			assertEquals((int)(b), data.intValue());
			assertEquals((long)(b), data.longValue());
			assertEquals((float)(b), data.floatValue(), 0.0F);
			assertEquals((double)(b), data.doubleValue(), 0.0D);

			short s = (short)(random.nextInt());
			data.set(s);
			assertEquals((byte)(s), data.byteValue());
			assertEquals((short)(s), data.shortValue());
			assertEquals((int)(s), data.intValue());
			assertEquals((long)(s), data.longValue());
			assertEquals((float)(s), data.floatValue(), 0.0F);
			assertEquals((double)(s), data.doubleValue(), 0.0D);

			int i = random.nextInt();
			data.set(i);
			assertEquals((byte)(i), data.byteValue());
			assertEquals((short)(i), data.shortValue());
			assertEquals((int)(i), data.intValue());
			assertEquals((long)(i), data.longValue());
			assertEquals((float)(i), data.floatValue(), 0.0F);
			assertEquals((double)(i), data.doubleValue(), 0.0D);

			long l = random.nextLong();
			data.set(l);
			assertEquals((byte)(l), data.byteValue());
			assertEquals((short)(l), data.shortValue());
			assertEquals((int)(l), data.intValue());
			assertEquals((long)(l), data.longValue());
			assertEquals((float)(l), data.floatValue(), 0.0F);
			assertEquals((double)(l), data.doubleValue(), 0.0D);

			float f = Float.intBitsToFloat(random.nextInt());
			data.set(f);
			assertEquals((byte)(f), data.byteValue());
			assertEquals((short)(f), data.shortValue());
			assertEquals((int)(f), data.intValue());
			assertEquals((long)(f), data.longValue());
			assertEquals((float)(f), data.floatValue(), 0.0F);
			assertEquals((double)(f), data.doubleValue(), 0.0D);

			double d = Double.longBitsToDouble(random.nextLong());
			data.set(d);
			assertEquals((byte)(d), data.byteValue());
			assertEquals((short)(d), data.shortValue());
			assertEquals((int)(d), data.intValue());
			assertEquals((long)(d), data.longValue());
			assertEquals((float)(d), data.floatValue(), 0.0F);
			assertEquals((double)(d), data.doubleValue(), 0.0D);
		}
	}
}