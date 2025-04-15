package builderb0y.autocodec.encoders;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.EmptyData;
import builderb0y.autocodec.data.MapData;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class MapEncoder<T_Key, T_Value, T_Map extends Map<T_Key, T_Value>> extends NamedEncoder<T_Map> {

	public final @NotNull AutoCoder<T_Key> keyEncoder;
	public final @NotNull AutoCoder<T_Value> valueEncoder;

	public MapEncoder(
		@NotNull ReifiedType<T_Map> type,
		@NotNull AutoCoder<T_Key> keyEncoder,
		@NotNull AutoCoder<T_Value> valueEncoder
	) {
		super(type);
		this.  keyEncoder =   keyEncoder;
		this.valueEncoder = valueEncoder;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull Data encode(@NotNull EncodeContext<T_Encoded, T_Map> context) throws EncodeException {
		T_Map object = context.object;
		if (object == null) return EmptyData.INSTANCE;
		return MapData.collect(
			object.entrySet().stream(),
			(Map.Entry<T_Key, T_Value> entry) -> context.object(entry.getKey  ()).encodeWith(this.  keyEncoder),
			(Map.Entry<T_Key, T_Value> entry) -> context.object(entry.getValue()).encodeWith(this.valueEncoder)
		);
	}

	public static class Factory extends NamedEncoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?>[] keyValueTypes = context.type.resolveParameters(Map.class);
			if (keyValueTypes != null) {
				AutoCoder<?>   keyEncoder = context.type(keyValueTypes[0]).forceCreateCoder();
				AutoCoder<?> valueEncoder = context.type(keyValueTypes[1]).forceCreateCoder();
				return new MapEncoder<>(context.type.uncheckedCast(), keyEncoder, valueEncoder);
			}
			return null;
		}
	}
}