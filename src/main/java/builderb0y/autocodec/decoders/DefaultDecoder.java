package builderb0y.autocodec.decoders;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	@Override
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		return this.defaultValue.decode(context, this.decoder);
	}

	@Override
	public @Nullable Stream<String> getKeys() {
		return this.decoder.getKeys();
	}

	@Override
	public String toString() {
		return this.toString + ": { decoder: " + this.decoder + ", default: " + this.defaultValue + " }";
	}

	public static class Factory extends NamedDecoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			DefaultValue defaultValue = DefaultValue.forType(context, context.type, false);
			if (defaultValue != NullDefaultValue.INSTANCE) {
				return new DefaultDecoder<>(context.type, context.forceCreateFallbackDecoder(this), defaultValue);
			}
			return null;
		}
	}
}