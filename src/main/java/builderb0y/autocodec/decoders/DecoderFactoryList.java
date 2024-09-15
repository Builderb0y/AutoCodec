package builderb0y.autocodec.decoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.FactoryList;
import builderb0y.autocodec.common.LazyHandler;
import builderb0y.autocodec.decoders.AutoDecoder.DecoderFactory;

public class DecoderFactoryList extends FactoryList<AutoDecoder<?>, DecoderFactory> implements DecoderFactory {

	public DecoderFactoryList(@NotNull AutoCodec autoCodec) {
		super(autoCodec);
	}

	@Override
	@OverrideOnly
	public void setup() {
		super.setup();
		this.addFactoriesToStart(
			UseDecoderFactory.INSTANCE,
			DefaultDecoder.Factory.INSTANCE,
			DefaultEmptyDecoder.Factory.INSTANCE,
			ConstructOnlyDecoder.Factory.INSTANCE,
			InternedStringDecoder.Factory.INSTANCE, //interned must come before multiline to stack with it.
			MultiLineStringDecoder.Factory.INSTANCE,
			new WrapperDecoderFactory()
		);
		this.addFactoriesToEnd(
			ArrayDecoder.Factory.INSTANCE,
			PatternDecoder.Factory.INSTANCE,
			OptionalDecoderFactory.INSTANCE,
			RecordDecoder.Factory.INSTANCE,
			ConstructImprintDecoder.Factory.INSTANCE
		);
	}

	@Override
	@OverrideOnly
	public @NotNull DecoderFactory createLookupFactory() {
		return new LookupDecoderFactory();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" }) //some generic wildcard capture BS going on here.
	public @NotNull LazyHandler<AutoDecoder<?>> createLazyHandler() {
		return new LazyDecoder();
	}
}