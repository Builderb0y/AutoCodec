package builderb0y.autocodec.decoders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.Intern;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class InternedStringDecoder extends NamedDecoder<@Intern String> {

	public final AutoDecoder<String> stringDecoder;

	public InternedStringDecoder(@NotNull ReifiedType<@Intern String> type, AutoDecoder<String> stringDecoder) {
		super(type);
		this.stringDecoder = stringDecoder;
	}

	@Override
	public <T_Encoded> @Nullable String decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		String string = context.decodeWith(this.stringDecoder);
		return string != null ? string.intern() : null;
	}

	public static class Factory implements DecoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			if (context.type.getRawClass() == String.class && context.type.getAnnotations().has(Intern.class)) {
				return new InternedStringDecoder(
					context.type.uncheckedCast(),
					//fallback decoder allows us to stack with @MultiLine.
					context.type(context.type.<String>uncheckedCast()).forceCreateFallbackDecoder(this)
				);
			}
			return null;
		}
	}
}