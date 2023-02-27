package builderb0y.autocodec.encoders;

import java.lang.reflect.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.UseSpec;
import builderb0y.autocodec.common.UseHandlerFactory;
import builderb0y.autocodec.encoders.AutoEncoder.EncoderFactory;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;

public class UseEncoderFactory extends UseHandlerFactory<AutoEncoder<?>> implements EncoderFactory {

	public static final @NotNull Classes<AutoEncoder<?>> CLASSES = new Classes<>(AutoEncoder.class, EncoderFactory.class, EncodeContext.class, Object.class, "encode");
	public static final @NotNull UseEncoderFactory INSTANCE = new UseEncoderFactory();

	@Override
	public <T_HandledType> @Nullable UseSpec getSpec(@NotNull FactoryContext<T_HandledType> context) {
		return UseSpec.fromUseEncoder(context.type);
	}

	@Override
	public @NotNull Classes<AutoEncoder<?>> classes() {
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
							TypeVariable<?>[] typeParameters = executable.getTypeParameters();
							if (typeParameters.length == 1) {
								TypeVariable<?> t_encoded = typeParameters[0];
								if (executable.getAnnotatedReturnType().getType().equals(t_encoded)) {
									AnnotatedType[] parameterTypes = executable.getAnnotatedParameterTypes();
									if (parameterTypes.length == 1 && parameterTypes[0] instanceof AnnotatedParameterizedType encodeContext) {
										if (((ParameterizedType)(encodeContext.getType())).getRawType() == EncodeContext.class) {
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
						"Method signature matches that of AutoEncoder::encode"
					)
				),
				(MethodPredicate predicate) -> (
					predicate
					.returnsNotVoid()
					.parameterType(0, new NamedPredicate<>((ReifiedType<?> type) -> type.getRawClass() == EncodeContext.class, "EncodeContext"))
				)
			),
			MemberCollector.forceUnique()
		);
	}
}