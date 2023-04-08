package builderb0y.autocodec.verifiers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.VerifyNullable;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;

public class NotNullVerifier<T> implements AutoVerifier<T> {

	public static final NotNullVerifier<?> INSTANCE = new NotNullVerifier<>();

	@Override
	@OverrideOnly
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, T> context) throws VerifyException {
		if (context.object == null) {
			throw new VerifyException(() -> context.pathToStringBuilder().append(" cannot be null.").toString());
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

	public static class Factory extends NamedVerifierFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			if (!context.type.getAnnotations().has(VerifyNullable.class)) {
				return NotNullVerifier.INSTANCE;
			}
			return null;
		}
	}
}