package builderb0y.autocodec.verifiers;

import java.lang.reflect.*;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.UseSpec;
import builderb0y.autocodec.common.UseHandlerFactory;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;
import builderb0y.autocodec.verifiers.AutoVerifier.VerifierFactory;

public class UseVerifierFactory extends UseHandlerFactory<AutoVerifier<?>> implements InheritedVerifierFactory {

	public static final @NotNull Classes<AutoVerifier<?>> CLASSES = new Classes<>(AutoVerifier.class, VerifierFactory.class, VerifyContext.class, void.class, "verify");
	public static final @NotNull UseVerifierFactory INSTANCE = new UseVerifierFactory();

	@Override
	@OverrideOnly
	public <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
		return InheritedVerifierFactory.super.tryCreate(context);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context, @NotNull ReifiedType<? extends T_HandledType> originalType) throws FactoryException {
		UseSpec[] specs = UseSpec.fromAllUseVerifiers(context.type);
		int length = specs.length;
		return switch (length) {
			case 0 -> null;
			case 1 -> this.doCreate(context, specs[0]);
			default -> {
				AutoVerifier<?>[] verifiers = AutoVerifier.ARRAY_FACTORY.apply(length);
				for (int index = 0; index < length; index++) {
					verifiers[index] = this.doCreate(context, specs[index]);
				}
				yield new MultiVerifier(verifiers);
			}
		};
	}

	@Override
	@Deprecated //only returns one annotation, not all of them.
	public <T_HandledType> @Nullable UseSpec getSpec(@NotNull FactoryContext<T_HandledType> context) {
		return UseSpec.fromUseVerifier(context.type);
	}

	@Override
	public @NotNull Classes<AutoVerifier<?>> classes() {
		return CLASSES;
	}

	@Override
	public @NotNull MethodLikeMemberView<?, ?> findMethodBeingHandler(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) {
		return context.reflect(spec.in()).searchMethods(
			false,
			new MethodPredicate()
			.name(spec.name())
			.isStatic()
			.parameterCount(1)
			.applyConditional(
				spec.strict(),
				(MethodPredicate predicate) -> predicate.actualMember(
					new NamedPredicate<>(
						(AnnotatedElement annotatedElement) -> {
							//ReifiedType does not reify type parameters declared on methods.
							//so, in order to check them properly, we have to use ordinary reflection.
							Executable executable = (Executable)(annotatedElement);
							if (executable.getAnnotatedReturnType().getType() == void.class) {
								TypeVariable<?>[] typeParameters = executable.getTypeParameters();
								if (typeParameters.length == 1) {
								TypeVariable<?> t_encoded = typeParameters[0];
									AnnotatedType[] parameterTypes = executable.getAnnotatedParameterTypes();
									if (parameterTypes.length == 1 && parameterTypes[0] instanceof AnnotatedParameterizedType encodeContext) {
										if (((ParameterizedType)(encodeContext.getType())).getRawType() == VerifyContext.class) {
											AnnotatedType[] encodedDecoded = encodeContext.getAnnotatedActualTypeArguments();
											if (encodedDecoded[0].getType().equals(t_encoded) && ReifiedType.BOXED_GENERIC_TYPE_STRATEGY.equals(spec.in().resolveDeclaration(encodedDecoded[1]), context.type)) {
												return true;
											}
										}
									}
								}
							}
							return false;
						},
						"Method signature matches that of AutoVerifier::verify"
					)
				),
				(MethodPredicate predicate) -> (
					predicate
					.returnsVoid()
					.parameterType(0, new NamedPredicate<>((ReifiedType<?> type) -> type.getRawClass() == VerifyContext.class, "VerifyContext"))
				)
			),
			MemberCollector.forceUnique()
		);
	}
}