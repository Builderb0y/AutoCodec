package builderb0y.autocodec.decoders;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.LazyHandler;

public class LazyDecoder<T> extends LazyHandler<AutoDecoder<T>> implements AutoDecoder<T> {

	public @Nullable AutoDecoder<T> resolution;

	@Override
	public @Nullable AutoDecoder<T> getNullableDelegateHandler() {
		return this.resolution;
	}

	@Override
	public void setDelegateHandler(@NotNull AutoDecoder<T> decoder) {
		this.resolution = decoder;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		return context.decodeWith(this.getDelegateHandler());
	}

	@Override
	public @Nullable Stream<@NotNull String> getKeys() {
		return this.getDelegateHandler().getKeys();
	}
}