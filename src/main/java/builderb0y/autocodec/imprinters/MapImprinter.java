package builderb0y.autocodec.imprinters;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.imprinters.AutoImprinter.NamedImprinter;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class MapImprinter<T_Key, T_Value, T_Map extends Map<T_Key, T_Value>> extends NamedImprinter<T_Map> {

	public final @NotNull AutoDecoder<T_Key> keyDecoder;
	public final @NotNull AutoDecoder<T_Value> valueDecoder;

	public MapImprinter(@NotNull ReifiedType<T_Map> type, @NotNull AutoDecoder<T_Key> keyDecoder, @NotNull AutoDecoder<T_Value> valueDecoder) {
		super(type);
		this.keyDecoder = keyDecoder;
		this.valueDecoder = valueDecoder;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Map> context) throws ImprintException {
		try {
			for (Map.Entry<DecodeContext<T_Encoded>, DecodeContext<T_Encoded>> entry : context.forceAsContextMap().entrySet()) {
				T_Key key = entry.getKey().decodeWith(this.keyDecoder);
				T_Value value = entry.getValue().decodeWith(this.valueDecoder);
				if (key != null && value != null) context.object.put(key, value);
			}
		}
		catch (ImprintException exception) {
			throw exception;
		}
		catch (DecodeException exception) {
			throw new ImprintException(exception);
		}
	}

	public static class Factory extends NamedImprinterFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoImprinter<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ReifiedType<?>[] keyValueTypes = context.type.getUpperBoundOrSelf().resolveParameters(Map.class);
			if (keyValueTypes != null) {
				AutoDecoder<?>   keyDecoder = context.type(keyValueTypes[0]).forceCreateDecoder();
				AutoDecoder<?> valueDecoder = context.type(keyValueTypes[1]).forceCreateDecoder();
				return new MapImprinter<>(context.type.uncheckedCast(), keyDecoder, valueDecoder);
			}
			return null;
		}
	}
}