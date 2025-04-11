package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class EmptyData<T_Encoded> extends Data<T_Encoded> {

	public EmptyData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.empty();
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EmptyData;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return "<empty>";
	}

	@Override
	public @NotNull Data<T_Encoded> deepCopy() {
		return this;
	}
}