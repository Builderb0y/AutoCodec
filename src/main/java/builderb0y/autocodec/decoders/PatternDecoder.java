package builderb0y.autocodec.decoders;

import java.util.regex.Pattern;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.PatternFlags;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;

public class PatternDecoder extends NamedDecoder<Pattern> {

	public final @NotNull AutoDecoder<PatternFlags> flagsDecoder;

	public PatternDecoder(@NotNull AutoDecoder<PatternFlags> flagsDecoder) {
		super("PatternDecoder: { flagsDecoder: " + flagsDecoder + " }");
		this.flagsDecoder = flagsDecoder;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable Pattern decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		String patternString;
		if ((patternString = context.tryAsString()) != null) {
			return Pattern.compile(patternString);
		}
		patternString = context.getMember("pattern").forceAsString();
		int patternFlags = 0;
		DecodeContext<T_Encoded> flags = context.getMember("flags");
		if (!flags.isEmpty()) {
			if (context.isCompressed()) {
				patternFlags = flags.forceAsNumber().intValue();
			}
			else {
				for (DecodeContext<T_Encoded> flagContext : flags.forceAsList(true)) {
					patternFlags |= flagContext.decodeWith(this.flagsDecoder).flag;
				}
			}
		}
		return Pattern.compile(patternString, patternFlags);
	}

	public static class Factory extends NamedDecoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			Class<?> rawClass = context.type.getLowerBoundOrSelf().getRawClass();
			if (rawClass == Pattern.class) {
				AutoDecoder<PatternFlags> flagsDecoder = context.type(PatternFlags.TYPE).forceCreateDecoder();
				return new PatternDecoder(flagsDecoder);
			}
			return null;
		}
	}
}