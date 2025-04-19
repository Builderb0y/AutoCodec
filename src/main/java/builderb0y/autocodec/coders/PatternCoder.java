package builderb0y.autocodec.coders;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.PatternFlags;
import builderb0y.autocodec.data.*;
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
		if (context.data.isEmpty()) return null;
		StringData patternString = context.data.tryAsString();
		if (patternString != null) {
			return Pattern.compile(patternString.value);
		}
		patternString = context.forceGetMember("pattern").forceAsString();
		int patternFlags = 0;
		DecodeContext<T_Encoded> flags = context.forceGetMember("flags");
		if (!flags.isEmpty()) {
			if (context.isCompressed()) {
				patternFlags = flags.forceAsInt();
			}
			else {
				ListData list = flags.tryAsList();
				if (list != null) {
					for (int index = 0, size = list.value.size(); index < size; index++) {
						patternFlags |= context.fork(index, list.value.get(index)).decodeWith(this.flagsCoder).flag;
					}
				}
				else {
					patternFlags = flags.decodeWith(this.flagsCoder).flag;
				}
			}
		}
		return Pattern.compile(patternString.value, patternFlags);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull Data encode(@NotNull EncodeContext<T_Encoded, Pattern> context) throws EncodeException {
		if (context.object == null) return EmptyData.INSTANCE;
		if (context.object.flags() == 0) return new StringData(context.object.pattern());
		MapData map = new MapData();
		map.putString("pattern", context.object.pattern());
		if (context.isCompressed()) {
			map.putInt("flags", context.object.flags());
		}
		else {
			int flags = context.object.flags();
			List<Data> list = new ArrayList<>(Integer.bitCount(flags));
			for (PatternFlags flag : PatternFlags.VALUES) {
				if ((flags & flag.flag) != 0) {
					list.add(context.object(flag).encodeWith(this.flagsCoder));
				}
			}
			map.putList("flags", list);
		}
		return map;
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