package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class StringData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull CharSequence value;

	public StringData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = "";
	}

	public StringData(@NotNull DynamicOps<T_Encoded> ops, @NotNull CharSequence value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createString(this.value.toString());
	}

	@Override
	public boolean isString() {
		return true;
	}

	@Override
	public @NotNull String tryAsString() {
		return this.value.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof StringData<?> that && this.value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}
}