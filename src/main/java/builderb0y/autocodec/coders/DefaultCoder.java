package builderb0y.autocodec.coders;

import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.DefaultSpec;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class DefaultCoder<T_Decoded> extends NamedCoder<T_Decoded> {

	public final @NotNull AutoCoder<T_Decoded> fallback;
	public final @NotNull DefaultSpec spec;

	public DefaultCoder(
		@NotNull ReifiedType<T_Decoded> handledType,
		@NotNull AutoCoder<T_Decoded> fallback,
		@NotNull DefaultSpec spec
	) {
		super(handledType);
		this.fallback = fallback;
		this.spec = spec;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		try {
			if (context.isEmpty()) switch (this.spec.mode()) {
				case ENCODED -> context = context.input(this.spec.getEncodedDefaultValue(context));
				case DECODED -> { return this.spec.getDecodedDefaultValue(context); }
			}
			return context.decodeWith(this.fallback);
		}
		catch (DecodeException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new DecodeException(exception);
		}
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		try {
			if (!this.spec.alwaysEncode()) {
				switch (this.spec.mode()) {
					case ENCODED -> {
						T_Encoded encoded = context.encodeWith(this.fallback);
						if (Objects.equals(this.spec.getEncodedDefaultValue(context), encoded)) {
							encoded = context.empty();
						}
						return encoded;
					}
					case DECODED -> {
						if (Objects.equals(this.spec.getDecodedDefaultValue(context), context.object)) {
							return context.empty();
						}
					}
				}
			}
			return context.encodeWith(this.fallback);
		}
		catch (EncodeException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new EncodeException(exception);
		}
	}

	@Override
	public @Nullable Stream<@NotNull String> getKeys() {
		return this.fallback.getKeys();
	}

	public static class Factory extends NamedCoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			DefaultSpec spec = DefaultSpec.from(context);
			if (spec != null) {
				return new DefaultCoder<>(context.type, context.forceCreateFallbackCoder(this), spec);
			}
			return null;
		}
	}
}