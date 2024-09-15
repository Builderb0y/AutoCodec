package builderb0y.autocodec.encoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.LookupFactory;
import builderb0y.autocodec.encoders.AutoEncoder.EncoderFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class LookupEncoderFactory extends LookupFactory<AutoEncoder<?>> implements EncoderFactory {

	@Override
	@OverrideOnly
	public void setup() {}

	public <T> void addGeneric(@NotNull ReifiedType<T> type, @NotNull AutoEncoder<T> constructor) {
		this.doAddGeneric(type, constructor);
	}

	public <T> void addRaw(@NotNull Class<T> type, @NotNull AutoEncoder<T> constructor) {
		this.doAddRaw(type, constructor);
	}
}