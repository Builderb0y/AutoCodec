package builderb0y.autocodec.verifiers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

public class NoopVerifier<T_Decoded> implements AutoVerifier<T_Decoded> {

	public static final NoopVerifier<?> INSTANCE = new NoopVerifier<>();

	@SuppressWarnings("unchecked")
	public static <T_Decoded> NoopVerifier<T_Decoded> instance() {
		return (NoopVerifier<T_Decoded>)(INSTANCE);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, T_Decoded> context) throws VerifyException {
		//nothing to do here.
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}