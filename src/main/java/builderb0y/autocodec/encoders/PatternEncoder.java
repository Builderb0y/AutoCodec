package builderb0y.autocodec.encoders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.PatternFlags;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;

public class PatternEncoder extends NamedEncoder<Pattern> {

	public final @NotNull AutoEncoder<PatternFlags> flagsEncoder;

	public PatternEncoder(@NotNull AutoEncoder<PatternFlags> flagsEncoder) {
		super("PatternEncoder: { flagsEncoder: " + flagsEncoder + " }");
		this.flagsEncoder = flagsEncoder;
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
					list.add(context.input(flag).encodeWith(this.flagsEncoder));
				}
			}
			map.put(context.createString("flags"), context.createList(list));
		}
		return context.createGenericMap(map);
	}

	public static class Factory extends NamedEncoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			Class<?> rawClass = context.type.getUpperBoundOrSelf().getRawClass();
			if (rawClass == Pattern.class) {
				AutoEncoder<PatternFlags> flagsEncoder = context.type(PatternFlags.TYPE).forceCreateEncoder();
				return new PatternEncoder(flagsEncoder);
			}
			return null;
		}
	}
}