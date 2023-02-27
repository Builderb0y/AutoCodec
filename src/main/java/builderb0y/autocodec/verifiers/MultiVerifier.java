package builderb0y.autocodec.verifiers;

import java.util.Arrays;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

public class MultiVerifier<T_Decoded> implements AutoVerifier<T_Decoded> {

	public final @NotNull AutoVerifier<T_Decoded> @NotNull [] verifiers;

	@SafeVarargs
	public MultiVerifier(@NotNull AutoVerifier<T_Decoded> @NotNull ... verifiers) {
		this.verifiers = (
			Arrays
			.stream(verifiers)
			.flatMap((AutoVerifier<T_Decoded> verifier) -> (
				verifier instanceof MultiVerifier<T_Decoded> multi
				? Arrays.stream(multi.verifiers)
				: Stream.of(verifier)
			))
			.toArray(AutoVerifier.ARRAY_FACTORY.generic())
		);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, T_Decoded> context) throws VerifyException {
		for (AutoVerifier<T_Decoded> verifier : this.verifiers) {
			context.verifyWith(verifier, context.object);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (" + this.verifiers.length + " verifiers)";
	}
}