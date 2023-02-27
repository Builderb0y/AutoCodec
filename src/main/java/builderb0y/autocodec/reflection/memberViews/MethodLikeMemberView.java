package builderb0y.autocodec.reflection.memberViews;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.Hash.Strategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.HashStrategies;
import builderb0y.autocodec.util.HashStrategies.NamedHashStrategy;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.util.TypeFormatter;

/** common properties of {@link Method} and {@link Constructor}. */
public abstract class MethodLikeMemberView<T_Owner, T_Return> extends MemberView<T_Owner> {

	public static final @NotNull ObjectArrayFactory<MethodLikeMemberView<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(MethodLikeMemberView.class).generic();

	public static final Hash.@NotNull Strategy<MethodLikeMemberView<?, ?>>
		RAW_TYPE_STRATEGY = new HashStrategy("MethodLikeMemberView.RAW_TYPE_STRATEGY", ReifiedType.RAW_TYPE_STRATEGY),
		GENERIC_TYPE_STRATEGY = new HashStrategy("MethodLikeMemberView.GENERIC_TYPE_STRATEGY", ReifiedType.GENERIC_TYPE_STRATEGY),
		ORDERED_ANNOTATED_TYPE_STRATEGY = new HashStrategy("MethodLikeMemberView.ORDERED_ANNOTATED_TYPE_STRATEGY", ReifiedType.ORDERED_ANNOTATIONS_STRATEGY),
		UNORDERED_ANNOTATED_TYPE_STRATEGY = new HashStrategy("MethodLikeMemberView.UNORDERED_ANNOTATED_TYPE_STRATEGY", ReifiedType.UNORDERED_ANNOTATIONS_STRATEGY);

	public @NotNull ParameterView<T_Owner, ?> @Nullable [] parameters;
	public @NotNull ReifiedType<?> @Nullable [] parameterTypes;

	public MethodLikeMemberView(@NotNull ReifiedType<T_Owner> declaringType) {
		super(declaringType);
	}

	/**
	returns a {@link MethodHandle} which will invoke this method or constructor.

	if this MemberView represents a {@link MethodView method},
	the handle's {@link MethodHandle#type() type}'s
	{@link MethodType#returnType() return type}
	will match that of the underlying {@link Method}.

	if this MemberView represents a {@link ConstructorView constructor},
	the handle's {@link MethodHandle#type() type}'s
	{@link MethodType#returnType() return type} will be the
	class that the underlying {@link Constructor} is declared in.
	@see #getDeclaringType()
	*/
	public abstract @NotNull MethodHandle createMethodHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException;

	@Override
	public abstract @NotNull Executable getActualMember();

	/**
	returns true if this MethodLikeMemberView could
	behave like a constructor for the given target type.
	in other words, that it is static, and returns the target type.
	*/
	public abstract boolean isConstructorLike(@NotNull ReifiedType<?> target);

	/**
	returns the same type that would be present on a MethodHandle
	created {@link #createMethodHandle(ReflectContextProvider)} by this view.

	@see #createMethodHandle(ReflectContextProvider)
	*/
	public abstract @NotNull MethodType getMethodType();

	/** returns a ReifiedType which represents the type that this method will return. */
	public abstract @NotNull ReifiedType<T_Return> getReturnType();

	public int getParameterCount() {
		int count = this.getActualMember().getParameterCount();
		if (!this.isStatic()) count++;
		return count;
	}

	public @NotNull ParameterView<T_Owner, ?> @NotNull [] getParameters() {
		ParameterView<T_Owner, ?>[] parameters = this.parameters;
		if (parameters == null) {
			int parameterCount = this.getParameterCount();
			parameters = ParameterView.ARRAY_FACTORY.applyGeneric(parameterCount);
			int offset = 0;
			if (!this.isStatic()) {
				String name = (
					this instanceof ConstructorView<?>
					? this.getDeclaringType().requireRawClass().getSimpleName() + ".this"
					: "this"
				);
				parameters[offset] = new ParameterView<>(this, name, this.getDeclaringType(), 0);
				offset++;
			}
			Parameter[] actualParameters = this.getActualMember().getParameters();
			for (int index = 0, length = actualParameters.length; index < length; index++) {
				Parameter parameter = actualParameters[index];
				parameters[index + offset] = new ParameterView<>(
					this,
					parameter.isNamePresent() ? parameter.getName() : null,
					this.getDeclaringType().resolveDeclaration(parameter.getAnnotatedType()),
					index + offset
				);
			}
			this.parameters = parameters;
		}
		return parameters;
	}

	public @NotNull ReifiedType<?> @NotNull [] getParameterTypes() {
		ReifiedType<?>[] parameterTypes = this.parameterTypes;
		if (parameterTypes == null) {
			ParameterView<T_Owner, ?>[] parameters = this.getParameters();
			int length = parameters.length;
			parameterTypes = ReifiedType.ARRAY_FACTORY.applyGeneric(length);
			for (int index = 0; index < length; index++) {
				parameterTypes[index] = parameters[index].getType();
			}
			this.parameterTypes = parameterTypes;
		}
		return parameterTypes;
	}

	@Override
	public void appendTo(TypeFormatter formatter) {
		formatter
		.append(Modifier.toString(this.getModifiers()))
		.append(' ')
		.append(this.getReturnType())
		.append(' ')
		.append(this.getDeclaringType())
		.append('.')
		.append(this.getName())
		.append('(');
		ParameterView<T_Owner, ?>[] parameters = this.getParameters();
		int parameterCount = parameters.length;
		if (parameterCount != 0) {
			formatter.append(parameters[0]);
			for (int index = 1; index < parameterCount; index++) {
				formatter.append(", ").append(parameters[index]);
			}
		}
		formatter.append(')');
	}

	@Override
	public int hashCode() {
		return ORDERED_ANNOTATED_TYPE_STRATEGY.hashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return (
			obj instanceof MethodLikeMemberView<?, ?> that &&
			ORDERED_ANNOTATED_TYPE_STRATEGY.equals(this, that)
		);
	}

	public static class HashStrategy extends NamedHashStrategy<MethodLikeMemberView<?, ?>> {

		public final Hash.@NotNull Strategy<ReifiedType<?>> typeStrategy, declaringTypeStrategy;

		public HashStrategy(
			@NotNull String toString,
			@NotNull Strategy<ReifiedType<?>> typeStrategy,
			@NotNull Strategy<ReifiedType<?>> declaringTypeStrategy
		) {
			super(toString);
			this.typeStrategy = typeStrategy;
			this.declaringTypeStrategy = declaringTypeStrategy;
		}

		public HashStrategy(@NotNull String toString, @NotNull Strategy<ReifiedType<?>> typeStrategy) {
			this(toString, typeStrategy, typeStrategy);
		}

		@Override
		public int hashCode(MethodLikeMemberView<?, ?> o) {
			if (o == null) return 0;
			return (
				o.getName().hashCode() +
				this.typeStrategy.hashCode(o.getReturnType()) +
				HashStrategies.orderedArrayHashCode(this.typeStrategy, o.getParameterTypes()) +
				this.declaringTypeStrategy.hashCode(o.getDeclaringType())
			);
		}

		@Override
		public boolean equals(MethodLikeMemberView<?, ?> a, MethodLikeMemberView<?, ?> b) {
			if (a == b) return true;
			if (a == null || b == null) return false;
			return (
				a.getName().equals(b.getName()) &&
				this.typeStrategy.equals(a.getReturnType(), b.getReturnType()) &&
				HashStrategies.orderedArrayEquals(this.typeStrategy, a.getParameterTypes(), b.getParameterTypes()) &&
				this.declaringTypeStrategy.equals(a.getDeclaringType(), b.getDeclaringType())
			);
		}
	}
}