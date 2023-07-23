package builderb0y.autocodec.annotations;

import org.junit.Test;

import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class VerifyNullableInstanceTest {

	@Test
	public void test() {
		VerifyNullable official = new ReifiedType<@VerifyNullable Object>() {}.getAnnotations().getFirst(VerifyNullable.class);
		VerifyNullable artificial = VerifyNullable.INSTANCE;
		assertEquals(official, artificial);
		assertEquals(official.toString(), artificial.toString());
		assertEquals(official.hashCode(), artificial.hashCode());
	}
}