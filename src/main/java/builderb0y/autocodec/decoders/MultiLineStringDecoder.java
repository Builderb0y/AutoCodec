package builderb0y.autocodec.decoders;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.MultiLine;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.AutoCodecUtil;

/** the AutoDecoder used for String's which are annotated with {@link MultiLine}. */
public class MultiLineStringDecoder extends NamedDecoder<@MultiLine String> {

	public MultiLineStringDecoder(@NotNull ReifiedType<@MultiLine String> type) {
		super(type);
	}

	@Override
	public <T_Encoded> @Nullable @MultiLine String decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		String string = context.tryAsString();
		if (string != null) return string;
		List<DecodeContext<T_Encoded>> list = context.tryAsList(false);
		if (list != null) {
			return list.stream().map((DecodeContext<T_Encoded> elementContext) -> {
				try {
					return elementContext.forceAsString();
				}
				catch (DecodeException exception) {
					throw AutoCodecUtil.rethrow(exception);
				}
			}).collect(Collectors.joining(System.lineSeparator()));
		}
		else {
			throw context.notA("String or list of String's");
		}
	}

	public static class Factory extends NamedDecoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			return (
				context.type.getRawClass() == String.class &&
				context.type.getAnnotations().has(MultiLine.class)
				? new MultiLineStringDecoder(context.type.uncheckedCast())
				: null
			);
		}
	}
}