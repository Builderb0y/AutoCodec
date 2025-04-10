package builderb0y.autocodec.fixers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.FactoryList;
import builderb0y.autocodec.common.LazyHandler;
import builderb0y.autocodec.fixers.AutoFixer.FixerFactory;

public class FixerFactoryList extends FactoryList<AutoFixer<?>, FixerFactory> implements FixerFactory {

	public FixerFactoryList(@NotNull AutoCodec autoCodec) {
		super(autoCodec);
	}

	@Override
	@OverrideOnly
	public void setup() {
		super.setup();
		this.addFactoryToStart(UseFixerFactory.INSTANCE);
	}

	@Override
	@OverrideOnly
	public @NotNull FixerFactory createLookupFactory() {
		return new LookupFixerFactory();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" }) //some generic wildcard capture BS going on here.
	public @NotNull LazyHandler<AutoFixer<?>> createLazyHandler() {
		return new LazyFixer();
	}

	@Override
	public @Nullable AutoFixer<?> doCreate(@NotNull FactoryContext<?> context) throws FactoryException {
		FixerFactory previousFactory = null;
		AutoFixer<?> previousFixer = null;
		for (FixerFactory factory : this.factories) {
			AutoFixer<?> fixer = context.tryCreateFixer(factory);
			if (fixer != null && fixer != NoopFixer.instance()) {
				if (previousFixer == null) {
					previousFixer = fixer;
					previousFactory = factory;
				}
				else {
					throw new FactoryException("More than one factory supplied an AutoFixer for " + context + ": " + previousFactory + " supplied " + previousFixer + "; " + factory + " supplied " + fixer);
				}
			}
		}
		return previousFixer != null ? previousFixer : NoopFixer.instance();
	}
}