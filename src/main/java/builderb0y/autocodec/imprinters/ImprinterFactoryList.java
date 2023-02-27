package builderb0y.autocodec.imprinters;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.FactoryList;
import builderb0y.autocodec.common.LazyHandler;
import builderb0y.autocodec.imprinters.AutoImprinter.ImprinterFactory;

public class ImprinterFactoryList extends FactoryList<AutoImprinter<?>, ImprinterFactory> implements ImprinterFactory {

	public ImprinterFactoryList(@NotNull AutoCodec autoCodec) {
		super(autoCodec);
	}

	@Override
	@OverrideOnly
	public void setup() {
		super.setup();
		this.addFactoryToStart(UseImprinterFactory.INSTANCE);
		this.addFactoriesToEnd(
			ArrayImprinter.Factory.INSTANCE,
			CollectionImprinter.Factory.INSTANCE,
			MapImprinter.Factory.INSTANCE,
			MultiFieldImprinter.Factory.INSTANCE
		);
	}

	@Override
	@OverrideOnly
	public @NotNull ImprinterFactory createLookupFactory() {
		return new LookupImprinterFactory();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" }) //some generic wildcard capture BS going on here.
	public @NotNull LazyHandler<AutoImprinter<?>> createLazyHandler() {
		return new LazyImprinter();
	}
}