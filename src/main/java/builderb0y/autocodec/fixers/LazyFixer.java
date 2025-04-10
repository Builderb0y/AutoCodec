package builderb0y.autocodec.fixers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.LazyHandler;

public class LazyFixer<T> extends LazyHandler<AutoFixer<T>> implements AutoFixer<T> {

	public @Nullable AutoFixer<T> resolution;

	@Override
	public @Nullable AutoFixer<T> getNullableDelegateHandler() {
		return this.resolution;
	}

	@Override
	public void setDelegateHandler(@NotNull AutoFixer<T> fixer) {
		this.resolution = fixer;
	}

	@Override
	@OverrideOnly
	public @NotNull <T_Encoded> DataFixContext<T_Encoded> fix(@NotNull DataFixContext<T_Encoded> context) throws DataFixException {
		return context.fixWith(this.getDelegateHandler());
	}
}