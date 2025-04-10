package builderb0y.autocodec.fixers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.LookupFactory;
import builderb0y.autocodec.fixers.AutoFixer.FixerFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class LookupFixerFactory extends LookupFactory<AutoFixer<?>> implements FixerFactory {

	@Override
	@OverrideOnly
	public void setup() {}

	public <T> void addGeneric(@NotNull ReifiedType<T> type, @NotNull AutoFixer<T> constructor) {
		this.doAddGeneric(type, constructor);
	}

	public <T> void addRaw(@NotNull Class<T> type, @NotNull AutoFixer<T> constructor) {
		this.doAddRaw(type, constructor);
	}
}