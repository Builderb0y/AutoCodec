package builderb0y.autocodec.decoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.*;
import builderb0y.autocodec.decoders.AutoDecoder.DecoderFactory;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.NoopVerifier;

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
			MultiLineStringDecoder.Factory.INSTANCE,
			new WrapperDecoderFactory()
		);
		this.addFactoriesToEnd(
			ArrayDecoder.Factory.INSTANCE,
			new EnumDecoder.Factory(EnumName.DEFAULT),
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

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public @Nullable AutoDecoder<?> doCreate(@NotNull FactoryContext<?> context) throws FactoryException {
		AutoDecoder decoder = super.doCreate(context);
		if (decoder != null) {
			AutoVerifier verifier = context.forceCreateVerifier();
			if (verifier != NoopVerifier.INSTANCE) {
				decoder = VerifyingDecoder.of(context.type, decoder, verifier);
			}
		}
		return decoder;
	}
}