package builderb0y.autocodec.fixers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

public class NoopFixer<T_Decoded> implements AutoFixer<T_Decoded> {

	public static final NoopFixer<?> INSTANCE = new NoopFixer<>();

	@SuppressWarnings("unchecked")
	public static <T_Decoded> NoopFixer<T_Decoded> instance() {
		return (NoopFixer<T_Decoded>)(INSTANCE);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull DataFixContext<T_Encoded> fix(@NotNull DataFixContext<T_Encoded> context) throws DataFixException {
		return context;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}