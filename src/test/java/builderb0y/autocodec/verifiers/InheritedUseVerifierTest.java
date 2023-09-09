package builderb0y.autocodec.verifiers;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseVerifier;
import builderb0y.autocodec.common.TestCommon;

import static org.junit.Assert.*;

public class InheritedUseVerifierTest {

	@Test
	public void test() throws VerifyException {
		AutoVerifier<A> verifier1 = TestCommon.DEFAULT_CODEC.createVerifier(A.class);
		this.checkValid  (verifier1, new A(true ));
		this.checkInvalid(verifier1, new A(false));

		AutoVerifier<B> verifier2 = TestCommon.DEFAULT_CODEC.createVerifier(B.class);
		this.checkValid  (verifier2, new B(true,  true ));
		this.checkInvalid(verifier2, new B(true,  false));
		this.checkInvalid(verifier2, new B(false, true ));
		this.checkInvalid(verifier2, new B(false, false));
	}

	public <T> void checkValid(AutoVerifier<T> verifier, T instance) throws VerifyException {
		TestCommon.DEFAULT_CODEC.verify(verifier, instance, JsonNull.INSTANCE, JsonOps.INSTANCE);
	}

	public <T> void checkInvalid(AutoVerifier<T> verifier, T instance) {
		try {
			TestCommon.DISABLED_CODEC.verify(verifier, instance, JsonNull.INSTANCE, JsonOps.INSTANCE);
			fail();
		}
		catch (VerifyException expected) {}
	}

	@UseVerifier(name = "verify", usage = MemberUsage.METHOD_IS_HANDLER)
	public static class A {

		public boolean valid;

		public A(boolean valid) {
			this.valid = valid;
		}

		public static <T_Encoded> void verify(VerifyContext<T_Encoded, A> context) throws VerifyException {
			if (context.object != null && !context.object.valid) {
				throw new VerifyException(() -> "not valid");
			}
		}
	}

	@UseVerifier(name = "verify2", usage = MemberUsage.METHOD_IS_HANDLER)
	public static class B extends A {

		public boolean valid2;

		public B(boolean valid, boolean valid2) {
			super(valid);
			this.valid2 = valid2;
		}

		public static <T_Encoded> void verify2(VerifyContext<T_Encoded, B> context) throws VerifyException {
			if (context.object != null && !context.object.valid2) {
				throw new VerifyException(() -> "not valid 2");
			}
		}
	}
}