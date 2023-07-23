package builderb0y.autocodec.common;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.AutoHandler.AutoFactory;
import builderb0y.autocodec.common.AutoHandler.NamedFactory;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;
import builderb0y.autocodec.util.TypeFormatter;

public abstract class UseHandlerFactory<T_Handler extends AutoHandler> extends NamedFactory<T_Handler> {

	@Override
	@OverrideOnly
	public <T_HandledType> @Nullable T_Handler tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
		UseSpec spec = this.getSpec(context);
		if (spec != null) {
			return this.doCreate(context, spec);
		}
		return null;
	}

	public <T_HandledType> @NotNull T_Handler doCreate(@NotNull FactoryContext<T_HandledType> context, @NotNull UseSpec spec) {
		try {
			context.logger().logMessage(spec);
			return switch (spec.usage()) {
				case FIELD_CONTAINS_HANDLER -> this.createFieldContainingHandler(context, this.findFieldContainingHandler(context, spec));
				case FIELD_CONTAINS_FACTORY -> this.createFieldContainingFactory(context, this.findFieldContainingFactory(context, spec));
				case METHOD_RETURNS_HANDLER -> this.createMethodReturningHandler(context, this.findMethodReturningHandler(context, spec));
				case METHOD_RETURNS_FACTORY -> this.createMethodReturningFactory(context, this.findMethodReturningFactory(context, spec));
				case METHOD_IS_HANDLER      -> this.createMethodBeingHandler    (context, this.findMethodBeingHandler    (context, spec));
				case METHOD_IS_FACTORY      -> this.createMethodBeingFactory    (context, this.findMethodBeingFactory    (context, spec));
			};
		}
		catch (FactoryException | Error exception) {
			throw exception;
		}
		catch (Throwable throwable) {
			throw new FactoryException(throwable);
		}
	}

	public abstract <T_HandledType> @Nullable UseSpec getSpec(@NotNull FactoryContext<T_HandledType> context);

	public static record Classes<T_Handler extends AutoHandler>(
		Class<? super T_Handler> handler,
		Class<? extends AutoFactory<T_Handler>> factory,
		@SuppressWarnings("rawtypes") //B<X> extends A<X> -/> Class<B> is assignable to Class<? extends A<?>>.
		Class<? extends DynamicOpsContext> context,
		Class<?> handlerResult,
		String implementedMethod
	) {}

	public abstract @NotNull Classes<T_Handler> classes();

	public @NotNull Predicate<ReifiedType<?>> matchHandler(@NotNull ReifiedType<?> handledType, boolean strict) {
		Class<? super T_Handler> handlerClass = this.classes().handler;
		return (
			strict
			? new NamedPredicate<>(
				(ReifiedType<?> type) -> ReifiedType.GENERIC_TYPE_STRATEGY.equals(type.resolveParameter(handlerClass), handledType),
				() -> new TypeFormatter(64).simplify(true).annotations(false).append("? extends ").append(handlerClass).append('<').append(handledType).append('>').toString()
			)
			: new NamedPredicate<>(
				(ReifiedType<?> type) -> type.getRawClass() != null && handlerClass.isAssignableFrom(type.getRawClass()),
				() -> "? extends " + TypeFormatter.getSimpleClassName(handlerClass)
			)
		);
	}

	public @NotNull Predicate<ReifiedType<?>> matchFactory() {
		Class<? super T_Handler> handlerClass = this.classes().handler;
		return new NamedPredicate<>(
			(ReifiedType<?> type) -> {
				ReifiedType<?> resolution = type.resolveParameter(AutoFactory.class);
				if (resolution == null) return false;
				Class<?> actualHandlerClass = resolution.getRawClass();
				if (actualHandlerClass == null) return false;
				return handlerClass.isAssignableFrom(actualHandlerClass);
			},
			() -> "? extends AutoFactory<? extends " + TypeFormatter.getSimpleClassName(handlerClass) + "<?>>"
		);
	}

	public @NotNull Predicate<ReifiedType<?>> matchFactoryContext() {
		return new NamedPredicate<>(
			(ReifiedType<?> type) -> type.getRawClass() == FactoryContext.class,
			"FactoryContext"
		);
	}

	public @NotNull FieldLikeMemberView<?, ?> findFieldContainingHandler(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) {
		return context.reflect(spec.in()).searchFields(
			false,
			new FieldPredicate()
			.name(spec.name())
			.isStatic()
			.type(this.matchHandler(context.type, spec.strict())),
			MemberCollector.forceUnique()
		);
	}

	public @NotNull FieldLikeMemberView<?, ?> findFieldContainingFactory(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) {
		return context.reflect(spec.in()).searchFields(
			false,
			new FieldPredicate()
			.name(spec.name())
			.isStatic()
			.type(this.matchFactory()),
			MemberCollector.forceUnique()
		);
	}

	public @NotNull MethodLikeMemberView<?, ?> findMethodReturningHandler(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) {
		return context.reflect(spec.in()).searchMethods(
			false,
			new MethodPredicate()
			.name(spec.name())
			.isStatic()
			.returnType(this.matchHandler(context.type, spec.strict()))
			.parameterCount(0),
			MemberCollector.forceUnique()
		);
	}

	public @NotNull MethodLikeMemberView<?, ?> findMethodReturningFactory(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) {
		return context.reflect(spec.in()).searchMethods(
			false,
			new MethodPredicate()
			.name(spec.name())
			.isStatic()
			.returnType(this.matchFactory())
			.parameterCount(0),
			MemberCollector.forceUnique()
		);
	}

	public abstract @NotNull MethodLikeMemberView<?, ?> findMethodBeingHandler(@NotNull FactoryContext<?> context, @NotNull UseSpec spec);

	public @NotNull MethodLikeMemberView<?, ?> findMethodBeingFactory(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) {
		return context.reflect(spec.in()).searchMethods(
			false,
			new MethodPredicate()
			.name(spec.name())
			.isStatic()
			.returnType(this.matchHandler(context.type, false))
			.parameterCount(1)
			.parameterType(0, this.matchFactoryContext()),
			MemberCollector.forceUnique()
		);
	}

	public @NotNull T_Handler createFieldContainingHandler(@NotNull FactoryContext<?> context, @NotNull FieldLikeMemberView<?, ?> field) throws Throwable {
		@SuppressWarnings("unchecked")
		T_Handler handler = (T_Handler)(field.createStaticReader(context).get());
		if (handler == null) throw new FactoryException(field + " was null.");
		return handler;
	}

	public @NotNull T_Handler createFieldContainingFactory(@NotNull FactoryContext<?> context, @NotNull FieldLikeMemberView<?, ?> field) throws Throwable {
		@SuppressWarnings("unchecked")
		AutoFactory<T_Handler> factory = (AutoFactory<T_Handler>)(field.createStaticReader(context).get());
		if (factory == null) throw new FactoryException(field + " was null.");
		return factory.forceCreate(context);
	}

	public @NotNull T_Handler createMethodReturningHandler(@NotNull FactoryContext<?> context, @NotNull MethodLikeMemberView<?, ?> method) throws Throwable {
		@SuppressWarnings("unchecked")
		T_Handler handler = (T_Handler)(method.createMethodHandle(context).invoke());
		if (handler == null) throw new FactoryException(method + " returned null.");
		return handler;
	}

	public @NotNull T_Handler createMethodReturningFactory(@NotNull FactoryContext<?> context, @NotNull MethodLikeMemberView<?, ?> method) throws Throwable {
		@SuppressWarnings("unchecked")
		AutoFactory<T_Handler> factory = (AutoFactory<T_Handler>)(method.createMethodHandle(context).invoke());
		if (factory == null) throw new FactoryException(method + " returned null.");
		return factory.forceCreate(context);
	}

	@SuppressWarnings("unchecked")
	public @NotNull T_Handler createMethodBeingHandler(@NotNull FactoryContext<?> context, @NotNull MethodLikeMemberView<?, ?> method) throws Throwable {
		MethodHandle handle = method.createMethodHandle(context);
		return (T_Handler)(
			LambdaMetafactory.metafactory(
				context.reflect(method.getDeclaringType()).lookup(),
				this.classes().implementedMethod,
				MethodType.methodType(this.classes().handler),
				MethodType.methodType(this.classes().handlerResult, this.classes().context),
				handle,
				handle.type()
			)
			.getTarget()
			.invoke()
		);
	}

	@SuppressWarnings("unchecked")
	public @NotNull T_Handler createMethodBeingFactory(@NotNull FactoryContext<?> context, @NotNull MethodLikeMemberView<?, ?> method) throws Throwable {
		T_Handler handler = (T_Handler)(method.createMethodHandle(context).invoke(context));
		if (handler == null) throw new FactoryException(method + " returned null.");
		return handler;
	}
}