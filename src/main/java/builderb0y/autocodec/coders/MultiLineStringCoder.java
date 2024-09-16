package builderb0y.autocodec.coders;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.MultiLine;
import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.AutoCodecUtil;

public class MultiLineStringCoder extends NamedCoder<@MultiLine String> {

	public final @NotNull String lineSeparator;
	public final @NotNull AutoCoder<String> fallback;

	public MultiLineStringCoder(@NotNull ReifiedType<@MultiLine String> handledType, @NotNull String lineSeparator, @NotNull AutoCoder<String> fallback) {
		super(handledType);
		this.lineSeparator = lineSeparator;
		this.fallback = fallback;
	}

	@Override
	public <T_Encoded> @Nullable @MultiLine String decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		Stream<DecodeContext<T_Encoded>> stream = context.tryAsStream(false);
		if (stream != null) {
			return stream.map((DecodeContext<T_Encoded> elementContext) -> {
				try {
					return elementContext.decodeWith(this.fallback);
				}
				catch (DecodeException exception) {
					throw AutoCodecUtil.rethrow(exception);
				}
			})
			.collect(Collectors.joining(this.lineSeparator));
		}
		else {
			return context.decodeWith(this.fallback);
		}
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, @MultiLine String> context) throws EncodeException {
		String string = context.object;
		if (string == null) return context.empty();
		int index = string.indexOf(this.lineSeparator);
		if (index < 0) return context.createString(string);
		Stream.Builder<String> lines = Stream.builder();
		lines.accept(string.substring(0, index));
		while (true) {
			int nextIndex = string.indexOf(this.lineSeparator, index + this.lineSeparator.length());
			if (nextIndex >= 0) {
				lines.accept(string.substring(index + this.lineSeparator.length(), nextIndex));
				index = nextIndex;
			}
			else {
				lines.accept(string.substring(index + 1));
				break;
			}
		}
		return context.createList(lines.build().map((String line) -> context.object(line).encodeWith(this.fallback)));
	}

	public static class Factory extends NamedCoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings("unchecked")
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			if (context.type.getRawClass() == String.class) {
				MultiLine annotation = context.type.getAnnotations().getFirst(MultiLine.class);
				if (annotation != null) {
					return new MultiLineStringCoder(
						context.type.uncheckedCast(),
						annotation.value(),
						(AutoCoder<String>)(context.forceCreateFallbackCoder(this))
					);
				}
			}
			return null;
		}
	}
}