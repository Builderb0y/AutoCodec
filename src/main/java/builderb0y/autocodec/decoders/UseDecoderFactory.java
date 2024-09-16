package builderb0y.autocodec.decoders;

import java.lang.reflect.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.UseHandlerFactory1;
import builderb0y.autocodec.common.UseSpec;
import builderb0y.autocodec.decoders.AutoDecoder.DecoderFactory;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;

public class UseDecoderFactory extends UseHandlerFactory1<AutoDecoder<?>> implements DecoderFactory {

	public static final @NotNull UseDecoderFactory INSTANCE = new UseDecoderFactory();

	public UseDecoderFactory() {
		super(AutoDecoder.class, DecoderFactory.class, DecodeContext.class, Object.class, "decode");
	}

	@Override
	public <T_HandledType> @Nullable UseSpec getSpec(@NotNull FactoryContext<T_HandledType> context) {
		return UseSpec.fromUseDecoder(context.type);
	}

	@Override
	public @NotNull MethodLikeMemberView<?, ?> findMethodBeingHandler(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) {
		return findMethodBeingDecoder(context, spec);
	}

	public static @NotNull MethodLikeMemberView<?, ?> findMethodBeingDecoder(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) {
		return context.reflect(spec.in()).searchMethods(
			false,
			new MethodPredicate()
			.name(spec.name())
			.isStatic()
			.parameterCount(1)
			.applyConditional(
				spec.strict(),
				(MethodPredicate predicate) -> (
					predicate
					.returnType(ReifiedType.BOXED_GENERIC_TYPE_STRATEGY, context.type)
					.actualMember(
						new NamedPredicate<>(
							(AnnotatedElement annotatedElement) -> {
								Executable executable = (Executable)(annotatedElement);
								TypeVariable<?>[] typeParameters = executable.getTypeParameters();
								if (typeParameters.length == 1) {
									TypeVariable<?> t_encoded = typeParameters[0];
									Type[] parameterTypes = executable.getGenericParameterTypes();
									if (parameterTypes.length == 1 && parameterTypes[0] instanceof ParameterizedType decodeContext) {
										if (decodeContext.getRawType() == DecodeContext.class) {
											if (decodeContext.getActualTypeArguments()[0].equals(t_encoded)) {
												return true;
											}
										}
									}
								}
								return false;
							},
							"Method signature matches that of AutoDecoder::decode"
						)
					)
				),
				(MethodPredicate predicate) -> (
					predicate
					.returnsNotVoid()
					.parameterType(0, new NamedPredicate<>((ReifiedType<?> type) -> type.getRawClass() == DecodeContext.class, "DecodeContext"))
				)
			),
			MemberCollector.forceUnique()
		);
	}
}