package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class EmptyData extends Data {

	public static final EmptyData INSTANCE = new EmptyData();

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull DynamicOps<T_Encoded> ops) {
		return ops.empty();
	}

	@Override
	public <T_Encoded> boolean isEmpty(@NotNull DynamicOps<T_Encoded> ops) {
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
}