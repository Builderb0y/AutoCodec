package builderb0y.autocodec.coders;

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class OptionalCoder<T> extends NamedCoder<Optional<T>> {

	public final @NotNull AutoCoder<T> coder;

	public OptionalCoder(@NotNull ReifiedType<Optional<T>> handledType, @NotNull AutoCoder<T> coder) {
		super(handledType);
		this.coder = coder;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable Optional<T> decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		return Optional.ofNullable(context.decodeWith(this.coder));
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Optional<T>> context) throws EncodeException {
		Optional<T> optional = context.input;
		if (optional == null || optional.isEmpty()) return context.empty();
		return context.input(optional.get()).encodeWith(this.coder);
	}

	public static class Factory extends NamedCoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?> type = context.type.resolveParameter(Optional.class);
			if (type != null) {
				return new OptionalCoder(context.type, context.type(type).forceCreateCoder());
			}
			return null;
		}
	}
}