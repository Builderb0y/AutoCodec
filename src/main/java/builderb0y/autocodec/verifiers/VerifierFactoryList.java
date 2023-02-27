package builderb0y.autocodec.verifiers;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.FactoryList;
import builderb0y.autocodec.common.LazyHandler;
import builderb0y.autocodec.verifiers.AutoVerifier.VerifierFactory;

public class VerifierFactoryList extends FactoryList<AutoVerifier<?>, VerifierFactory> implements VerifierFactory {

	public VerifierFactoryList(@NotNull AutoCodec autoCodec) {
		super(autoCodec);
	}

	@Override
	@OverrideOnly
	public void setup() {
		super.setup();
		this.addFactoryToStart(UseVerifierFactory.INSTANCE);
		this.addFactoriesToEnd(
			NotNullVerifier.Factory.INSTANCE,
			IntRangeVerifier.Factory.INSTANCE,
			FloatRangeVerifier.Factory.INSTANCE,
			SizeRangeVerifier.Factory.INSTANCE,
			SortedVerifier.Factory.INSTANCE
		);
	}

	@Override
	public @NotNull VerifierFactory createLookupFactory() {
		return new LookupVerifierFactory();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" }) //some generic wildcard capture BS going on here.
	public @NotNull LazyHandler<AutoVerifier<?>> createLazyHandler() {
		return new LazyVerifier();
	}

	@Override
	public @Nullable AutoVerifier<?> doCreate(@NotNull FactoryContext<?> context) throws FactoryException {
		List<AutoVerifier<?>> list = new ArrayList<>(4);
		for (VerifierFactory factory : this.factories) {
			AutoVerifier<?> verifier = context.tryCreateVerifier(factory);
			if (verifier != null && verifier != NoopVerifier.INSTANCE) list.add(verifier);
		}
		return switch (list.size()) {
			case 0 -> NoopVerifier.INSTANCE;
			case 1 -> list.get(0);
			default -> new MultiVerifier<>(AutoVerifier.ARRAY_FACTORY.collectionToArrayForcedGeneric(list));
		};
	}
}