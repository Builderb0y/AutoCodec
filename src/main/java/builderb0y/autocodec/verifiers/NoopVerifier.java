package builderb0y.autocodec.verifiers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

public class NoopVerifier<T> implements AutoVerifier<T> {

	public static final NoopVerifier<?> INSTANCE = new NoopVerifier<>();

	@Override
	@OverrideOnly
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, T> context) throws VerifyException {
		//nothing to do here.
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}