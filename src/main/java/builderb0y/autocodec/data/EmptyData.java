package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.util.ObjectArrayFactory;

public class EmptyData extends Data {

	public static final @NotNull ObjectArrayFactory<EmptyData> ARRAY_FACTORY = new ObjectArrayFactory<>(EmptyData.class);

	public static final @NotNull EmptyData INSTANCE = new EmptyData();

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.empty();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Data data && data.isEmpty();
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
	public @NotNull Data deepCopy() {
		return this;
	}
}