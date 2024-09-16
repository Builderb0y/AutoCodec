package builderb0y.autocodec.coders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.coders.AutoCoder.CoderFactory;
import builderb0y.autocodec.common.*;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.NoopVerifier;

public class CoderFactoryList extends FactoryList<AutoCoder<?>, CoderFactory> implements CoderFactory {

	public CoderFactoryList(@NotNull AutoCodec autoCodec) {
		super(autoCodec);
	}

	@Override
	public void setup() {
		super.setup();
		this.addFactoryToStart(UseCoderFactory.INSTANCE);
		this.addFactoriesToEnd(
			ArrayCoder.Factory.INSTANCE,
			new EnumCoder.Factory(EnumName.DEFAULT),
			OptionalCoder.Factory.INSTANCE,
			PatternCoder.Factory.INSTANCE,
			EncoderDecoderCoder.Factory.INSTANCE
		);
	}

	@Override
	public @NotNull CoderFactory createLookupFactory() {
		return new LookupCoderFactory();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" }) //some generic wildcard capture BS going on here.
	public @NotNull LazyHandler<AutoCoder<?>> createLazyHandler() {
		return new LazyCoder();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public @Nullable AutoCoder<?> doCreate(@NotNull FactoryContext<?> context) throws FactoryException {
		AutoCoder coder = super.doCreate(context);
		if (coder != null) {
			AutoVerifier verifier = context.forceCreateVerifier();
			if (verifier != NoopVerifier.INSTANCE) {
				coder = new VerifyingCoder(context.type, coder, verifier);
			}
		}
		return coder;
	}
}