package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class BooleanData<T_Encoded> extends Data<T_Encoded> {

	public boolean value;

	public BooleanData(DynamicOps<T_Encoded> ops, boolean value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createBoolean(this.value);
	}

	@Override
	public boolean equals(Object object) {
		BooleanData<?> bool;
		return object instanceof Data<?> data && (bool = data.tryAsBoolean()) != null && this.value == bool.value;
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
	public @NotNull Data<T_Encoded> deepCopy() {
		return new BooleanData<>(this.ops, this.value);
	}
}