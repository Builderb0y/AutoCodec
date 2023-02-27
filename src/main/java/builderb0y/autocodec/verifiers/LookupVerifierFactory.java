package builderb0y.autocodec.verifiers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.LookupFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.AutoVerifier.VerifierFactory;

public class LookupVerifierFactory extends LookupFactory<AutoVerifier<?>> implements VerifierFactory {

	@Override
	@OverrideOnly
	public void setup() {}

	public <T> void addGeneric(@NotNull ReifiedType<T> type, @NotNull AutoVerifier<T> constructor) {
		this.doAddGeneric(type, constructor);
	}

	public <T> void addRaw(@NotNull Class<T> type, @NotNull AutoVerifier<T> constructor) {
		this.doAddRaw(type, constructor);
	}
}