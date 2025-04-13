package builderb0y.autocodec.data;

import java.util.WeakHashMap;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

public class EmptyData<T_Encoded> extends Data<T_Encoded> {

	@Internal
	public static final WeakHashMap<DynamicOps<?>, EmptyData<?>> OPS_CACHE = new WeakHashMap<>();

	public EmptyData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
	}

	@SuppressWarnings("unchecked")
	public static <T_Encoded> @NotNull EmptyData<T_Encoded> forOps(@NotNull DynamicOps<T_Encoded> ops) {
		synchronized (OPS_CACHE) {
			return (EmptyData<T_Encoded>)(OPS_CACHE.computeIfAbsent(ops, EmptyData::new));
		}
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.empty();
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof Data<?> data && data.isEmpty();
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