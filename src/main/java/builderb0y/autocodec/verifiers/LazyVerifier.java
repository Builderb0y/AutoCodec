package builderb0y.autocodec.verifiers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.LazyHandler;

public class LazyVerifier<T> extends LazyHandler<AutoVerifier<T>> implements AutoVerifier<T> {

	public @Nullable AutoVerifier<T> resolution;

	@Override
	public @Nullable AutoVerifier<T> getNullableDelegateHandler() {
		return this.resolution;
	}

	@Override
	public void setDelegateHandler(@NotNull AutoVerifier<T> constructor) {
		this.resolution = constructor;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, T> context) throws VerifyException {
		context.verifyWith(this.getDelegateHandler());
	}
}