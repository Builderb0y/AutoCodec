package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class StringData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull String value;

	public StringData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = "";
	}

	public StringData(@NotNull DynamicOps<T_Encoded> ops, @NotNull String value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createString(this.value);
	}

	@Override
	public boolean equals(Object object) {
		StringData<?> string;
		return object instanceof Data<?> data && (string = data.tryAsString()) != null && this.value.equals(string.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value;
	}

	@Override
	public @NotNull Data<T_Encoded> deepCopy() {
		return new StringData<>(this.ops, this.value);
	}
}