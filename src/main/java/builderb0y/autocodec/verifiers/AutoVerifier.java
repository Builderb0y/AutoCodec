package builderb0y.autocodec.verifiers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.AutoHandler;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

public interface AutoVerifier<T_Decoded> extends AutoHandler {

	public static final @NotNull ObjectArrayFactory<AutoVerifier<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoVerifier.class).generic();

	/**
	verifies that the output of a {@link AutoDecoder} called previously is "valid".
	if the object is not valid, throws a {@link VerifyException}.
	the definition of valid depends on the specific
	subclass of {@link AutoVerifier} in question.
	some subclasses may assert that the object is non-null,
	or that the object (if it's an array or list) is non-empty,
	or if it's a number they may assert that it's not 0 or negative,
	etc... most subclasses can be enabled and fine-tuned with annotations.
	see the documentation on specific subclasses (or associated annotations)
	for more info on how to configure them.

	this method is annotated with {@link OverrideOnly}
	because it performs no logging on its own.
	use {@link DecodeContext#verifyWith(AutoVerifier, Object)}
	to construct and log what is being constructed.
	*/
	@OverrideOnly
	public abstract <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, T_Decoded> context) throws VerifyException;

	public static abstract class NamedVerifier<T_Decoded> extends NamedHandler<T_Decoded> implements AutoVerifier<T_Decoded> {

		public NamedVerifier(@NotNull ReifiedType<T_Decoded> type) {
			super(type);
		}

		public NamedVerifier(@NotNull String toString) {
			super(toString);
		}
	}

	public static interface VerifierFactory extends AutoFactory<AutoVerifier<?>> {

		/**
		returns an AutoVerifier which can verify instances of T_HandledType.
		or null if this factory does not know how to verify instances of T_HandledType
		or decides that T_HandledType does not need to be verified.
		throws {@link FactoryException} if this factory knows how to verify
		instances of T_HandledType, and decides that verification is applicable,
		but some other problem occurs which prevents it from doing so.

		this method used to enforce that it returns AutoVerifier<T_HandledType>,
		but the problem with that is it usually just resulted
		in a lot of unchecked casts for implementors.
		java's generics system just wasn't designed for these kinds of things.

		this method is annotated with {@link OverrideOnly}
		because it performs no logging on its own.
		use {@link FactoryContext#tryCreateVerifier(VerifierFactory)}
		to create a verifier using this factory and log it at the same time.
		*/
		@Override
		@OverrideOnly
		public abstract <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException;
	}

	public static abstract class NamedVerifierFactory extends NamedFactory<AutoVerifier<?>> implements VerifierFactory {

		public NamedVerifierFactory() {}

		public NamedVerifierFactory(@NotNull String toString) {
			super(toString);
		}
	}
}