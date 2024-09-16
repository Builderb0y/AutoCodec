package builderb0y.autocodec.imprinters;

import java.lang.reflect.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.UseHandlerFactory1;
import builderb0y.autocodec.common.UseSpec;
import builderb0y.autocodec.imprinters.AutoImprinter.ImprinterFactory;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;

public class UseImprinterFactory extends UseHandlerFactory1<AutoImprinter<?>> implements ImprinterFactory {

	public static final @NotNull UseImprinterFactory INSTANCE = new UseImprinterFactory();

	public UseImprinterFactory() {
		super(AutoImprinter.class, ImprinterFactory.class, ImprintContext.class, void.class, "imprint");
	}

	@Override
	public <T_HandledType> @Nullable UseSpec getSpec(@NotNull FactoryContext<T_HandledType> context) {
		return UseSpec.fromUseImprinter(context.type);
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
										if (((ParameterizedType)(encodeContext.getType())).getRawType() == ImprintContext.class) {
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
						"Method signature matches that of AutoImprinter::imprint"
					)
				),
				(MethodPredicate predicate) -> (
					predicate
					.returnsVoid()
					.parameterType(0, new NamedPredicate<>((ReifiedType<?> type) -> type.getRawClass() == ImprintContext.class, "ImprintContext"))
				)
			),
			MemberCollector.forceUnique()
		);
	}
}