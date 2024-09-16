package builderb0y.autocodec.encoders;

import java.util.Map;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class MapEncoder<T_Key, T_Value, T_Map extends Map<T_Key, T_Value>> extends NamedEncoder<T_Map> {

	public final @NotNull AutoEncoder<T_Key> keyEncoder;
	public final @NotNull AutoEncoder<T_Value> valueEncoder;

	public MapEncoder(@NotNull ReifiedType<T_Map> type, @NotNull AutoEncoder<T_Key> keyEncoder, @NotNull AutoEncoder<T_Value> valueEncoder) {
		super(type);
		this.  keyEncoder =   keyEncoder;
		this.valueEncoder = valueEncoder;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Map> context) throws EncodeException {
		if (context.object == null) return context.empty();
		return context.createGenericMap(
			context.object.entrySet().stream().map((Map.Entry<T_Key, T_Value> entry) -> Pair.of(
				context.object(entry.getKey  ()).encodeWith(this.  keyEncoder),
				context.object(entry.getValue()).encodeWith(this.valueEncoder)
			))
		);
	}

	public static class Factory extends NamedEncoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?>[] keyValueTypes = context.type.resolveParameters(Map.class);
			if (keyValueTypes != null) {
				AutoEncoder<?>   keyEncoder = context.type(keyValueTypes[0]).forceCreateEncoder();
				AutoEncoder<?> valueEncoder = context.type(keyValueTypes[1]).forceCreateEncoder();
				return new MapEncoder<>(context.type.uncheckedCast(), keyEncoder, valueEncoder);
			}
			return null;
		}
	}
}