package builderb0y.autocodec.imprinters;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.LookupFactory;
import builderb0y.autocodec.imprinters.AutoImprinter.ImprinterFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class LookupImprinterFactory extends LookupFactory<AutoImprinter<?>> implements ImprinterFactory {

	@Override
	@OverrideOnly
	public void setup() {}

	public <T> void addGeneric(@NotNull ReifiedType<T> type, @NotNull AutoImprinter<T> constructor) {
		this.doAddGeneric(type, constructor);
	}

	public <T> void addRaw(@NotNull Class<T> type, @NotNull AutoImprinter<T> constructor) {
		this.doAddRaw(type, constructor);
	}
}