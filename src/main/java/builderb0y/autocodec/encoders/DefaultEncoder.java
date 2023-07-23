package builderb0y.autocodec.encoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.DefaultValue;
import builderb0y.autocodec.common.DefaultValue.NullDefaultValue;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class DefaultEncoder<T_Decoded> extends NamedEncoder<T_Decoded> {

	public final @NotNull AutoEncoder<T_Decoded> encoder;
	public final @NotNull DefaultValue defaultValue;

	public DefaultEncoder(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoEncoder<T_Decoded> encoder,
		@NotNull DefaultValue defaultValue
	) {
		super(type);
		this.encoder = encoder;
		this.defaultValue = defaultValue;
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		return this.defaultValue.encode(context, this.encoder);
	}

	@Override
	public String toString() {
		return this.toString + ": { encoder: " + this.encoder + ", default: " + this.defaultValue + " }";
	}

	public static class Factory extends NamedEncoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			DefaultValue defaultValue = DefaultValue.forType(context, context.type, true);
			if (defaultValue != NullDefaultValue.INSTANCE) {
				return new DefaultEncoder<>(context.type, context.forceCreateFallbackEncoder(this), defaultValue);
			}
			return null;
		}
	}
}