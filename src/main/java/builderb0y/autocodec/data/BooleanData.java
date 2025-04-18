package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.util.ObjectArrayFactory;

public class BooleanData extends Data {

	public static final @NotNull ObjectArrayFactory<BooleanData> ARRAY_FACTORY = new ObjectArrayFactory<>(BooleanData.class);

	public boolean value;

	public BooleanData(boolean value) {
		this.value = value;
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded convert(@NotNull DynamicOps<T_Encoded> ops) {
		return ops.createBoolean(this.value);
	}

	@Override
	public boolean equals(Object object) {
		BooleanData bool;
		return object instanceof Data data && (bool = data.tryAsBoolean()) != null && this.value == bool.value;
	}

	@Override
	public int hashCode() {
		return Boolean.hashCode(this.value);
	}

	@Override
	public String toString() {
		return Boolean.toString(this.value);
	}

	@Override
	public @NotNull Data deepCopy() {
		return new BooleanData(this.value);
	}
}