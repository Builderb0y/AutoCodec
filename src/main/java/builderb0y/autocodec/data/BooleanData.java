package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	public @Nullable Boolean tryAsBoolean() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BooleanData<?> that && this.value == that.value;
	}

	@Override
	public int hashCode() {
		return Boolean.hashCode(this.value);
	}

	@Override
	public String toString() {
		return Boolean.toString(this.value);
	}
}