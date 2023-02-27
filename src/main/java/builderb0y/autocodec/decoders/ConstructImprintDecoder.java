package builderb0y.autocodec.decoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class ConstructImprintDecoder<T_Decoded> extends NamedDecoder<T_Decoded> {

	public final @NotNull AutoConstructor<T_Decoded> constructor;
	public final @NotNull AutoImprinter<T_Decoded> imprinter;

	public ConstructImprintDecoder(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoConstructor<T_Decoded> constructor, @NotNull AutoImprinter<T_Decoded> imprinter) {
		super(type);
		this.constructor = constructor;
		this.imprinter = imprinter;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		T_Decoded object = context.constructWith(this.constructor);
		context.imprintWith(this.imprinter, object);
		return object;
	}

	@Override
	public String toString() {
		return this.toString + ": { constructor: " + this.constructor + ", imprinter: " + this.imprinter + " }";
	}

	public static class Factory extends NamedDecoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public @Nullable <T_HandledType> AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			AutoConstructor<T_HandledType> constructor = context.tryCreateConstructor();
			if (constructor == null) return null;
			//assume the AutoDecoder we're about to return
			//will itself be verified after it is returned.
			AutoImprinter<T_HandledType> imprinter = context.tryCreateImprinter();
			if (imprinter == null) return null;
			return new ConstructImprintDecoder<>(context.type, constructor, imprinter);
		}
	}
}