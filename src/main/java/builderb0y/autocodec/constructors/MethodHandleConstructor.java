package builderb0y.autocodec.constructors;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.constructors.AutoConstructor.NamedConstructor;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.TypeFormatter;

public class MethodHandleConstructor<T_Decoded> extends NamedConstructor<T_Decoded> {

	public static final MethodType HANDLE_TYPE = MethodType.methodType(Object.class, ConstructContext.class);

	public final @NotNull MethodHandle handle;

	public MethodHandleConstructor(@NotNull String name, @NotNull MethodHandle handle) {
		super(name);
		this.handle = handle.asType(HANDLE_TYPE);
	}

	public MethodHandleConstructor(@NotNull ReflectContextProvider provider, @NotNull MethodLikeMemberView<T_Decoded, T_Decoded> method) throws IllegalAccessException {
		super(
			new TypeFormatter(64)
			.annotations(false)
			.simplify(false)
			.append(method.getDeclaringType())
			.append("::")
			.append(method.getName())
			.toString()
		);
		MethodHandle handle = method.createMethodHandle(provider);
		if (handle.type().parameterCount() == 0) {
			handle = MethodHandles.dropArguments(handle, 0, ConstructContext.class);
		}
		this.handle = handle.asType(HANDLE_TYPE);
	}

	@Override
	@OverrideOnly
	@Contract("_ -> new")
	@SuppressWarnings("unchecked")
	public <T_Encoded> @NotNull T_Decoded construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
		try {
			return (T_Decoded)(this.handle.invokeExact(context));
		}
		catch (ConstructException | Error exception) {
			throw exception;
		}
		catch (Throwable throwable) {
			throw new ConstructException(throwable);
		}
	}

	public static class Factory extends NamedConstructorFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings("unchecked")
		public <T_HandledType> @Nullable AutoConstructor<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			//find constructor taking ConstructContext<T_HandledType>.
			MethodLikeMemberView<T_HandledType, T_HandledType> constructor = (
				(MethodLikeMemberView<T_HandledType, T_HandledType>)(
					context.reflect().searchMethods(
						false,
						new MethodPredicate()
						.name("new")
						.parameterTypes(
							ReifiedType.GENERIC_TYPE_STRATEGY,
							ReifiedType.parameterize(ConstructContext.class, context.type)
						)
						.constructorLike(context.type),
						MemberCollector.expectOne(false, true)
					)
				)
			);
			//if no constructor taking ConstructContext<T_HandledType>
			//is found, try a no-arg constructor next.
			if (constructor == null) {
				context.logger().logMessageLazy(() -> "Did not find a constructor taking a ConstructContext<" + context.type + '>');
				constructor = (
					(MethodLikeMemberView<T_HandledType, T_HandledType>)(
						context.reflect().searchMethods(
							false,
							new MethodPredicate()
							.name("new")
							.parameterCount(0)
							.constructorLike(context.type),
							MemberCollector.expectOne(false, true)
						)
					)
				);
				if (constructor == null) {
					context.logger().logMessage("Did not find a no-arg constructor either.");
					//if neither type of constructor is found, abort.
					return null;
				}
			}

			try {
				return new MethodHandleConstructor<>(context, constructor);
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException(exception);
			}
		}
	}
}