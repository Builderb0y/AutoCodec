package builderb0y.autocodec.decoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.LookupFactory;
import builderb0y.autocodec.decoders.AutoDecoder.DecoderFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class LookupDecoderFactory extends LookupFactory<AutoDecoder<?>> implements DecoderFactory {

	@Override
	@OverrideOnly
	public void setup() {}

	public <T_Decoded> void addGeneric(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoDecoder<T_Decoded> constructor) {
		this.doAddGeneric(type, constructor);
	}

	public <T_Decoded> void addRaw(@NotNull Class<T_Decoded> type, @NotNull AutoDecoder<T_Decoded> constructor) {
		this.doAddRaw(type, constructor);
	}
}