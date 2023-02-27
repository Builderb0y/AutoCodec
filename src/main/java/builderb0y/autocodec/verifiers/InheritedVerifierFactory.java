package builderb0y.autocodec.verifiers;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.AutoVerifier.VerifierFactory;

/**
a VerifierFactory which applies to subtypes of the given type, not just the type itself.
this factory can be implemented in cases where class B extends class A,
and if A has a verifier that this factory can create, then B should too.
this factory should not be implemented when B should not have the same verifier, even if A does.
*/
public interface InheritedVerifierFactory extends VerifierFactory {

	@OverrideOnly
	public abstract <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context, @NotNull ReifiedType<? extends T_HandledType> originalType) throws FactoryException;

	@Override
	@OverrideOnly
	public default <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
		List<AutoVerifier<?>> list = new ArrayList<>(4);
		for (ReifiedType<? super T_HandledType> type : context.type.getInheritanceHierarchy()) {
			AutoVerifier<?> verifier = this.tryCreate(context.type(type), context.type);
			if (verifier != null && verifier != NoopVerifier.INSTANCE) list.add(verifier);
		}
		return switch (list.size()) {
			case 0 -> null;
			case 1 -> list.get(0);
			default -> new MultiVerifier<>(AutoVerifier.ARRAY_FACTORY.collectionToArrayForcedGeneric(list));
		};
	}
}