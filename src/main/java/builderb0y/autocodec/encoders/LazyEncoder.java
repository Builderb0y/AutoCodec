package builderb0y.autocodec.encoders;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.LazyHandler;

public class LazyEncoder<T> extends LazyHandler<AutoEncoder<T>> implements AutoEncoder<T> {

	public @Nullable AutoEncoder<T> resolution;

	@Override
	public @Nullable AutoEncoder<T> getNullableDelegateHandler() {
		return this.resolution;
	}

	@Override
	public void setDelegateHandler(@NotNull AutoEncoder<T> constructor) {
		this.resolution = constructor;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T> context) throws EncodeException {
		return context.encodeWith(this.getDelegateHandler());
	}

	@Override
	public @Nullable Stream<String> getKeys() {
		return this.getDelegateHandler().getKeys();
	}
}