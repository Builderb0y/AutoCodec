package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class StringData extends Data {

	public @NotNull String value;

	public StringData() {
		this.value = "";
	}

	public StringData(@NotNull String value) {
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createString(this.value);
	}

	@Override
	public boolean equals(Object object) {
		StringData string;
		return object instanceof Data data && (string = data.tryAsString()) != null && this.value.equals(string.value);
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
	public @NotNull Data deepCopy() {
		return new StringData(this.value);
	}
}