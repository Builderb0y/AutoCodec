package builderb0y.autocodec.fixers;

import java.lang.reflect.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.UseHandlerFactory1;
import builderb0y.autocodec.common.UseSpec;
import builderb0y.autocodec.fixers.AutoFixer.FixerFactory;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;

public class UseFixerFactory extends UseHandlerFactory1<AutoFixer<?>> implements FixerFactory {

	public static final UseFixerFactory INSTANCE = new UseFixerFactory();

	public UseFixerFactory() {
		super(AutoFixer.class, FixerFactory.class, DataFixContext.class, DataFixContext.class, "fix");
	}

	@Override
	public @Nullable <T_HandledType> UseSpec getSpec(@NotNull FactoryContext<T_HandledType> context) {
		return UseSpec.fromUseFixer(context.type);
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
							Executable executable = (Executable)(annotatedElement);
							TypeVariable<?>[] variables = executable.getTypeParameters();
							Type[] actual;
							return (
								variables.length == 1 &&

								//check return type.
								executable.getAnnotatedReturnType().getType() instanceof ParameterizedType parameterizedReturn &&
								parameterizedReturn.getRawType() == DataFixContext.class &&
								(actual = parameterizedReturn.getActualTypeArguments()).length == 1 &&
								actual[0].equals(variables[0]) &&

								//check parameter type.
								executable.getGenericParameterTypes()[0] /* length already checked previously */ instanceof ParameterizedType parameterizedArg &&
								parameterizedArg.getRawType() == DataFixContext.class &&
								(actual = parameterizedArg.getActualTypeArguments()).length == 1 &&
								actual[0].equals(variables[0])
							);
						},
						"Method signature matches that of AutoFixer::fix"
					)
				),
				(MethodPredicate predicate) -> (
					predicate
					.returnType(ReifiedType.RAW_TYPE_STRATEGY, ReifiedType.from(DataFixContext.class))
					.parameterType(0, ReifiedType.RAW_TYPE_STRATEGY, ReifiedType.from(DataFixContext.class))
				)
			),
			MemberCollector.forceUnique()
		);
	}
}