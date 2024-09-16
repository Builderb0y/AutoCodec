package builderb0y.autocodec.coders;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.LazyHandler;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;

public class LazyCoder<T> extends LazyHandler<AutoCoder<T>> implements AutoCoder<T> {

	public @Nullable AutoCoder<T> resolution;

	@Override
	public @Nullable AutoCoder<T> getNullableDelegateHandler() {
		return this.resolution;
	}

	@Override
	public void setDelegateHandler(@NotNull AutoCoder<T> coder) {
		this.resolution = coder;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		return context.decodeWith(this.getDelegateHandler());
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