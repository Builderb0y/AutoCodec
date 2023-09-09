package builderb0y.autocodec.encoders;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.MultiLine;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/** the AutoEncoder used for String's which are annotated with {@link MultiLine}. */
public class MultiLineStringEncoder extends NamedEncoder<@MultiLine String> {

	public MultiLineStringEncoder(@NotNull ReifiedType<@MultiLine String> type) {
		super(type);
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, @MultiLine String> context) throws EncodeException {
		String input = context.input;
		if (input == null) return context.empty();
		List<T_Encoded> list = input.lines().map(context::createString).collect(Collectors.toList());
		return switch (list.size()) {
			case 0 -> context.createString("");
			case 1 -> list.get(0);
			default -> context.createList(list);
		};
	}

	public static class Factory extends NamedEncoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			return (
				context.type.getRawClass() == String.class &&
				context.type.getAnnotations().has(MultiLine.class)
				? new MultiLineStringEncoder(context.type.uncheckedCast())
				: null
			);
		}
	}
}