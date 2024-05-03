package builderb0y.autocodec.decoders;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.ConstructOnly;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class ConstructOnlyDecoder<T_Decoded> extends NamedDecoder<T_Decoded> {

	public final @NotNull AutoConstructor<T_Decoded> constructor;
	public final @Nullable AutoDecoder<T_Decoded> fallback;

	public ConstructOnlyDecoder(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoConstructor<T_Decoded> constructor,
		@Nullable AutoDecoder<T_Decoded> fallback
	) {
		super(type);
		this.constructor = constructor;
		this.fallback = fallback;
	}

	@Override
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (this.fallback != null && !context.isEmpty()) {
			return context.decodeWith(this.fallback);
		}
		else {
			return context.constructWith(this.constructor);
		}
	}

	@Override
	public @Nullable Stream<String> getKeys() {
		return this.fallback != null ? this.fallback.getKeys() : null;
	}

	public static class Factory extends NamedDecoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ConstructOnly annotation = context.type.getAnnotations().getFirst(ConstructOnly.class);
			if (annotation != null) {
				AutoConstructor<T_HandledType> constructor = context.forceCreateConstructor();
				AutoDecoder<T_HandledType> fallback = annotation.onlyWhenNull() ? context.forceCreateFallbackDecoder(this) : null;
				return new ConstructOnlyDecoder<>(context.type, constructor, fallback);
			}
			return null;
		}
	}
}