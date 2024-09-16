package builderb0y.autocodec.imprinters;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.LazyHandler;

public class LazyImprinter<T> extends LazyHandler<AutoImprinter<T>> implements AutoImprinter<T> {

	public @Nullable AutoImprinter<T> resolution;

	@Override
	public @Nullable AutoImprinter<T> getNullableDelegateHandler() {
		return this.resolution;
	}

	@Override
	public void setDelegateHandler(@NotNull AutoImprinter<T> constructor) {
		this.resolution = constructor;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T> context) throws ImprintException {
		context.imprintWith(this.getDelegateHandler());
	}

	@Override
	public @Nullable Stream<String> getKeys() {
		return this.getDelegateHandler().getKeys();
	}
}