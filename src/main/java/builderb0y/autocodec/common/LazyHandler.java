package builderb0y.autocodec.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LazyHandler<T_Handler> {

	@SuppressWarnings("unchecked")
	public final @NotNull T_Handler getThisHandler() {
		return (T_Handler)(this);
	}

	public abstract @Nullable T_Handler getNullableDelegateHandler();

	public @NotNull T_Handler getDelegateHandler() {
		T_Handler handler = this.getNullableDelegateHandler();
		if (handler != null) return handler;
		else throw new IllegalStateException();
	}

	public abstract void setDelegateHandler(@NotNull T_Handler handler);

	@Override
	public String toString() {
		T_Handler handler = this.getNullableDelegateHandler();
		if (handler != null) return handler.toString();
		else return "Unresolved " + this.getClass().getSimpleName();
	}
}