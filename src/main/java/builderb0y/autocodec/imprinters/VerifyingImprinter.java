package builderb0y.autocodec.imprinters;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.imprinters.AutoImprinter.NamedImprinter;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.VerifyException;

public class VerifyingImprinter<T_Decoded> extends NamedImprinter<T_Decoded> {

	public final @NotNull AutoImprinter<T_Decoded> imprinter;
	public final @NotNull AutoVerifier<T_Decoded> verifier;

	public VerifyingImprinter(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoImprinter<T_Decoded> imprinter, @NotNull AutoVerifier<T_Decoded> verifier) {
		super(type);
		this.imprinter = imprinter;
		this.verifier  = verifier;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Decoded> context) throws ImprintException {
		context.imprintWith(this.imprinter, context.object);
		try {
			context.verifyWith(this.verifier, context.object);
		}
		catch (VerifyException exception) {
			throw new ImprintException(exception);
		}
	}

	@Override
	public String toString() {
		return super.toString() + ": { imprinter: " + this.imprinter + ", verifier: " + this.verifier + " }";
	}
}