package builderb0y.autocodec.decoders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.coders.DefaultCoder;
import builderb0y.autocodec.common.DefaultValue;
import builderb0y.autocodec.common.DefaultValue.NullDefaultValue;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class DefaultDecoder<T_Decoded> extends NamedDecoder<T_Decoded> {

	public final @NotNull AutoDecoder<T_Decoded> decoder;
	public final @NotNull DefaultValue defaultValue;

	public DefaultDecoder(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoDecoder<T_Decoded> decoder,
		@NotNull DefaultValue defaultValue
	) {
		super(type);
		this.decoder = decoder;
		this.defaultValue = defaultValue;
	}

	public static <T_Decoded> DefaultDecoder<T_Decoded> of(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoDecoder<T_Decoded> decoder,
		@NotNull DefaultValue defaultValue
	) {
		return (
			decoder instanceof AutoCoder<T_Decoded> coder
			? new DefaultCoder<>(type, coder, defaultValue)
			: new DefaultDecoder<>(type, decoder, defaultValue)
		);
	}

	@Override
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) context = this.defaultValue.applyToContext(context);
		return context.decodeWith(this.decoder);
	}

	@Override
	public String toString() {
		return this.toString + ": { decoder: " + this.decoder + ", default: " + this.defaultValue + " }";
	}

	public static class Factory extends NamedDecoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			DefaultValue defaultValue = DefaultValue.forType(context.type);
			if (defaultValue != NullDefaultValue.INSTANCE) {
				return DefaultDecoder.of(context.type, context.forceCreateFallbackDecoder(this), defaultValue);
			}
			return null;
		}
	}
}