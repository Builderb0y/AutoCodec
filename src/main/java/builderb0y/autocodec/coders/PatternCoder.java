package builderb0y.autocodec.coders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.PatternFlags;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;

public class PatternCoder extends NamedCoder<Pattern> {

	public final @NotNull AutoCoder<PatternFlags> flagsCoder;

	public PatternCoder(@NotNull AutoCoder<PatternFlags> flagsCoder) {
		super("PatternCoder");
		this.flagsCoder = flagsCoder;
	}

	@Override
	@OverrideOnly
	public @Nullable <T_Encoded> Pattern decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		String patternString = context.tryAsString();
		if (patternString != null) {
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
					patternFlags |= flagContext.decodeWith(this.flagsCoder).flag;
				}
			}
		}
		return Pattern.compile(patternString, patternFlags);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Pattern> context) throws EncodeException {
		if (context.input == null) return context.empty();
		if (context.input.flags() == 0) return context.createString(context.input.pattern());
		Map<T_Encoded, T_Encoded> map = new Object2ObjectArrayMap<>(2);
		map.put(context.createString("pattern"), context.createString(context.input.pattern()));
		if (context.isCompressed()) {
			map.put(context.createString("flags"), context.createInt(context.input.flags()));
		}
		else {
			int flags = context.input.flags();
			List<T_Encoded> list = new ArrayList<>(Integer.bitCount(flags));
			for (PatternFlags flag : PatternFlags.VALUES) {
				if ((flags & flag.flag) != 0) {
					list.add(context.input(flag).encodeWith(this.flagsCoder));
				}
			}
			map.put(context.createString("flags"), context.createList(list));
		}
		return context.createGenericMap(map);
	}

	@Override
	public String toString() {
		return super.toString() + ": { flagsCoder: " + this.flagsCoder + " }";
	}

	public static class Factory extends NamedCoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public @Nullable <T_HandledType> AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			if (context.type.getRawClass() == Pattern.class) {
				return new PatternCoder(context.type(PatternFlags.TYPE).forceCreateCoder());
			}
			return null;
		}
	}
}