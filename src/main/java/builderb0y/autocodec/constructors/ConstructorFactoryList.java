package builderb0y.autocodec.constructors;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.FactoryList;
import builderb0y.autocodec.common.LazyHandler;
import builderb0y.autocodec.constructors.AutoConstructor.ConstructorFactory;

public class ConstructorFactoryList extends FactoryList<AutoConstructor<?>, ConstructorFactory> implements ConstructorFactory {

	public ConstructorFactoryList(@NotNull AutoCodec autoCodec) {
		super(autoCodec);
	}

	@Override
	@OverrideOnly
	public void setup() {
		super.setup();
		this.addFactoriesToStart(
			UseConstructorFactory.INSTANCE,
			UseImplementationConstructorFactory.INSTANCE
		);
		this.addFactoriesToEnd(
			EnumMapConstructor.Factory.INSTANCE,
			EnumSetConstructor.Factory.INSTANCE,
			MethodHandleConstructor.Factory.INSTANCE
		);
	}

	@Override
	@OverrideOnly
	public @NotNull ConstructorFactory createLookupFactory() {
		return new LookupConstructorFactory();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" }) //some generic wildcard capture BS going on here.
	public @NotNull LazyHandler<AutoConstructor<?>> createLazyHandler() {
		return new LazyConstructor();
	}
}