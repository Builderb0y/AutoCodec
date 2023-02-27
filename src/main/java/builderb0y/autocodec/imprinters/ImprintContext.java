package builderb0y.autocodec.imprinters;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class ImprintContext<T_Encoded, T_Decoded> extends DecodeContext<T_Encoded> {

	public static final @NotNull ObjectArrayFactory<ImprintContext<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(ImprintContext.class).generic();

	public final @NotNull T_Decoded object;

	public ImprintContext(@NotNull DecodeContext<T_Encoded> context, @NotNull T_Decoded object) {
		super(context);
		this.object = object;
	}

	public <T_NewDecoded> @NotNull ImprintContext<T_Encoded, T_NewDecoded> object(@NotNull T_NewDecoded object) {
		return new ImprintContext<>(this, object);
	}

	public void imprintWith(@NotNull AutoImprinter<T_Decoded> imprinter) throws ImprintException {
		this.logger().imprint(imprinter, this);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { path: " + this.pathToString() + ", input: " + this.input + ", ops: " + this.ops + ", object: " + this.object + " }";
	}
}