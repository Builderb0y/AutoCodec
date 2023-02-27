package builderb0y.autocodec.encoders;

import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.DefaultEmpty;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class DefaultEmptyEncoder<T_Decoded> extends NamedEncoder<T_Decoded> {

	public final @NotNull AutoEncoder<T_Decoded> fallbackEncoder;

	public DefaultEmptyEncoder(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoEncoder<T_Decoded> fallbackEncoder) {
		super(type);
		this.fallbackEncoder = fallbackEncoder;
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		T_Encoded encoded = context.encodeWith(this.fallbackEncoder);
		Optional<? extends Stream<?>> stream = context.ops.getStream(encoded).result();
		if (stream.isPresent() && stream.get().limit(1L).findAny().isEmpty()) {
			encoded = context.empty();
		}
		else {
			stream = context.ops.getMapValues(encoded).result();
			if (stream.isPresent() && stream.get().limit(1L).findAny().isEmpty()) {
				encoded = context.empty();
			}
		}
		return encoded;
	}

	public static class Factory extends NamedEncoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			DefaultEmpty annotation = context.type.getAnnotations().getFirst(DefaultEmpty.class);
			if (annotation != null && !annotation.alwaysEncode()) {
				return new DefaultEmptyEncoder<>(context.type, context.forceCreateFallbackEncoder(this));
			}
			return null;
		}
	}
}