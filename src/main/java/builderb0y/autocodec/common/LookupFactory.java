package builderb0y.autocodec.common;

import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.TypeFormatter;

public abstract class LookupFactory<T_Handler extends AutoHandler> implements AutoHandler.AutoFactory<T_Handler> {

	public final @NotNull Map<@NotNull ReifiedType<?>, @NotNull AutoHandler>
		genericLookup = new Object2ObjectOpenCustomHashMap<>(32, ReifiedType.GENERIC_TYPE_STRATEGY);
	public final @NotNull Map<@NotNull Class<?>, @NotNull AutoHandler>
		rawLookup = new HashMap<>(64);

	public LookupFactory() {
		this.setup();
	}

	@OverrideOnly
	public abstract void setup();

	/** sub-classes will provide delegates to this method with stricter type checks. */
	public void doAddGeneric(@NotNull ReifiedType<?> type, @NotNull AutoHandler handler) {
		this.genericLookup.put(type, handler);
	}

	/** sub-classes will provide delegates to this method with stricter type checks. */
	public void doAddRaw(@NotNull Class<?> type, @NotNull AutoHandler handler) {
		this.rawLookup.put(type, handler);
	}

	public void removeGeneric(@NotNull ReifiedType<?> type) {
		this.genericLookup.remove(type);
	}

	public void removeRaw(@NotNull Class<?> type) {
		this.rawLookup.remove(type);
	}

	@Override
	@OverrideOnly
	@SuppressWarnings("unchecked")
	public <T_HandledType> @Nullable T_Handler tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
		AutoHandler result = this.genericLookup.get(context.type);
		if (result != null) return (T_Handler)(result);
		Class<?> raw = context.type.getRawClass();
		if (raw == null) return null;
		return (T_Handler)(this.rawLookup.get(raw));
	}

	@Override
	public String toString() {
		return TypeFormatter.getSimpleClassName(this.getClass());
	}
}