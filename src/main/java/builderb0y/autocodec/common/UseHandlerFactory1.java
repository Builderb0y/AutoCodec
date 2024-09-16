package builderb0y.autocodec.common;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.AutoHandler.AutoFactory;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;

public abstract class UseHandlerFactory1<T_Handler extends AutoHandler> extends UseHandlerFactory0<T_Handler> {

	@SuppressWarnings("rawtypes") //B<X> extends A<X> -/> Class<B> is assignable to Class<? extends A<?>>.
	public final Class<? extends DynamicOpsContext> contextClass;
	public final Class<?> handlerResultClass;
	public final String implementedMethodName;

	public UseHandlerFactory1(
		Class<? super T_Handler> handlerClass,
		Class<? extends AutoFactory<T_Handler>> factoryClass,
		@SuppressWarnings("rawtypes")
		Class<? extends DynamicOpsContext> contextClass,
		Class<?> handlerResultClass,
		String implementedMethodName
	) {
		super(handlerClass, factoryClass);
		this.contextClass = contextClass;
		this.handlerResultClass = handlerResultClass;
		this.implementedMethodName = implementedMethodName;
	}

	public abstract @NotNull MethodLikeMemberView<?, ?> findMethodBeingHandler(@NotNull FactoryContext<?> context, @NotNull UseSpec spec);

	@Override
	@SuppressWarnings("unchecked")
	public @NotNull T_Handler createMethodBeingHandler(@NotNull FactoryContext<?> context, @NotNull UseSpec spec) throws Throwable {
		MethodLikeMemberView<?, ?> method = this.findMethodBeingHandler(context, spec);
		MethodHandle handle = method.createMethodHandle(context);
		return (T_Handler)(
			LambdaMetafactory.metafactory(
				context.reflect(method.getDeclaringType()).lookup(),
				this.implementedMethodName,
				MethodType.methodType(this.handlerClass),
				MethodType.methodType(this.handlerResultClass, this.contextClass),
				handle,
				handle.type()
			)
			.getTarget()
			.invoke()
		);
	}
}