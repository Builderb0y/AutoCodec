package builderb0y.autocodec.encoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.FactoryList;
import builderb0y.autocodec.common.LazyHandler;
import builderb0y.autocodec.encoders.AutoEncoder.EncoderFactory;

public class EncoderFactoryList extends FactoryList<AutoEncoder<?>, EncoderFactory> implements EncoderFactory {

	public EncoderFactoryList(@NotNull AutoCodec autoCodec) {
		super(autoCodec);
	}

	@Override
	@OverrideOnly
	public void setup() {
		super.setup();
		this.addFactoriesToStart(
			UseEncoderFactory.INSTANCE,
			DefaultEncoder.Factory.INSTANCE,
			DefaultEmptyEncoder.Factory.INSTANCE,
			MultiLineStringEncoder.Factory.INSTANCE,
			new WrapperEncoderFactory()
		);
		this.addFactoriesToEnd(
			CollectionEncoder.Factory.INSTANCE,
			MapEncoder.Factory.INSTANCE,
			PatternEncoder.Factory.INSTANCE,
			OptionalEncoderFactory.INSTANCE,
			MultiFieldEncoder.Factory.INSTANCE
		);
	}

	@Override
	@OverrideOnly
	public @NotNull EncoderFactory createLookupFactory() {
		return new LookupEncoderFactory();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" }) //some generic wildcard capture BS going on here.
	public @NotNull LazyHandler<AutoEncoder<?>> createLazyHandler() {
		return new LazyEncoder();
	}
}