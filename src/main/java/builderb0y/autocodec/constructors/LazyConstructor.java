package builderb0y.autocodec.constructors;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.LazyHandler;

public class LazyConstructor<T> extends LazyHandler<AutoConstructor<T>> implements AutoConstructor<T> {

	public @Nullable AutoConstructor<T> resolution;

	@Override
	public @Nullable AutoConstructor<T> getNullableDelegateHandler() {
		return this.resolution;
	}

	@Override
	public void setDelegateHandler(@NotNull AutoConstructor<T> constructor) {
		this.resolution = constructor;
	}

	@Override
	@OverrideOnly
	@Contract("_ -> new")
	public <T_Encoded> @NotNull T construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
		return context.constructWith(this.getDelegateHandler());
	}
}