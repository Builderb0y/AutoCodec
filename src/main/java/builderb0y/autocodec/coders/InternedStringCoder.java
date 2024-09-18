package builderb0y.autocodec.coders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.Intern;
import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class InternedStringCoder extends NamedCoder<String> {

	public final @NotNull AutoCoder<String> fallback;

	public InternedStringCoder(@NotNull ReifiedType<String> handledType, @NotNull AutoCoder<String> fallback) {
		super(handledType);
		this.fallback = fallback;
	}

	@Override
	@OverrideOnly
	public @Nullable <T_Encoded> String decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		String string = context.decodeWith(this.fallback);
		return string != null ? string.intern() : null;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, String> context) throws EncodeException {
		return context.encodeWith(this.fallback);
	}

	public static class Factory extends NamedCoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings("unchecked")
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			if (context.type.getRawClass() == String.class && context.type.getAnnotations().has(Intern.class)) {
				return new InternedStringCoder(context.type.uncheckedCast(), (AutoCoder<String>)(context.forceCreateFallbackCoder(this)));
			}
			return null;
		}
	}
}