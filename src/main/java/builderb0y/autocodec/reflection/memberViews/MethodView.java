package builderb0y.autocodec.reflection.memberViews;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class MethodView<T_Owner, T_Return> extends MethodLikeMemberView<T_Owner, T_Return> {

	public static final @NotNull ObjectArrayFactory<MethodView<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(MethodView.class).generic();

	public final @NotNull Method method;
	public @Nullable ReifiedType<T_Return> returnType;
	public @Nullable MethodType cachedMethodType;

	public MethodView(@NotNull ReifiedType<T_Owner> declaringType, @NotNull Method method) {
		super(declaringType);
		this.method = method;
	}

	@Override
	public @NotNull MethodHandle createMethodHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		return provider.reflect(this.getDeclaringType()).lookup().unreflect(this.method);
	}

	@Override
	public @NotNull Method getActualMember() {
		return this.method;
	}

	@Override
	public boolean isConstructorLike(@NotNull ReifiedType<?> target) {
		return this.isStatic() && ReifiedType.GENERIC_TYPE_STRATEGY.equals(this.getReturnType(), target);
	}

	@Override
	public @NotNull String getName() {
		return this.method.getName();
	}

	@Override
	public int getModifiers() {
		return this.method.getModifiers();
	}

	@Override
	public @NotNull MethodType getMethodType() {
		MethodType cache = this.cachedMethodType;
		if (cache == null) {
			cache = this.cachedMethodType = toMethodType(this.method);
		}
		return cache;
	}

	@Override
	@SuppressWarnings("unchecked")
	public @NotNull ReifiedType<T_Return> getReturnType() {
		ReifiedType<T_Return> returnType = this.returnType;
		if (returnType == null) {
			returnType = this.returnType = (ReifiedType<T_Return>)(
				this.getDeclaringType().resolveDeclaration(this.method.getAnnotatedReturnType())
			);
		}
		return returnType;
	}

	public static @NotNull MethodType toMethodType(@NotNull Method method) {
		Class<?>[] parameters = method.getParameterTypes();
		if ((method.getModifiers() & Modifier.STATIC) == 0) {
			int parameterCount = parameters.length;
			Class<?>[] newParameters = new Class<?>[parameterCount + 1];
			System.arraycopy(parameters, 0, newParameters, 1, parameterCount);
			newParameters[0] = method.getDeclaringClass();
			parameters = newParameters;
		}
		return MethodType.methodType(method.getReturnType(), parameters);
	}
}