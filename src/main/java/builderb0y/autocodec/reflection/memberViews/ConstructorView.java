package builderb0y.autocodec.reflection.memberViews;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class ConstructorView<T_Owner> extends MethodLikeMemberView<T_Owner, T_Owner> {

	public static final @NotNull ObjectArrayFactory<ConstructorView<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(ConstructorView.class).generic();

	public final @NotNull Constructor<T_Owner> constructor;
	public MethodType cachedMethodType;

	public ConstructorView(@NotNull ReifiedType<T_Owner> declaringType, @NotNull Constructor<T_Owner> constructor) {
		super(declaringType);
		this.constructor = constructor;
	}

	@Override
	public @NotNull MethodHandle createMethodHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		return provider.reflect(this.getDeclaringType()).lookup().unreflectConstructor(this.constructor);
	}

	@Override
	public @NotNull Constructor<T_Owner> getActualMember() {
		return this.constructor;
	}

	@Override
	public boolean isConstructorLike(@NotNull ReifiedType<?> target) {
		return ReifiedType.GENERIC_TYPE_STRATEGY.equals(this.getReturnType(), target);
	}

	@Override
	public @NotNull String getName() {
		return "new";
	}

	@Override
	public int getModifiers() {
		int modifiers = this.constructor.getModifiers();
		if (!AutoCodecUtil.isNonStaticInnerClass(this.constructor.getDeclaringClass())) {
			modifiers |= Modifier.STATIC;
		}
		return modifiers;
	}

	@Override
	public @NotNull MethodType getMethodType() {
		MethodType cache = this.cachedMethodType;
		return cache != null ? cache : (this.cachedMethodType = MethodType.methodType(this.constructor.getDeclaringClass(), this.constructor.getParameterTypes()));
	}

	@Override
	public @NotNull ReifiedType<T_Owner> getReturnType() {
		return this.getDeclaringType();
	}
}