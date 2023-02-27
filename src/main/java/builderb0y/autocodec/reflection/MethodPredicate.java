package builderb0y.autocodec.reflection;

import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.ParameterView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.HashStrategies;

public class MethodPredicate extends MemberPredicate<MethodLikeMemberView<?, ?>> {

	public @NotNull MethodPredicate add(@NotNull Predicate<? super MethodLikeMemberView<?, ?>> predicate, @NotNull Consumer<StringBuilder> toString) {
		this.addV(predicate, toString);
		return this;
	}

	public @NotNull MethodPredicate name(@NotNull String name) {
		this.nameV(name);
		return this;
	}

	public @NotNull MethodPredicate name(@NotNull Predicate<? super String> namePredicate) {
		this.nameV(namePredicate);
		return this;
	}

	public @NotNull MethodPredicate apply(@NotNull UnaryOperator<@NotNull MethodPredicate> operator) {
		return operator.apply(this);
	}

	public @NotNull MethodPredicate applyConditional(boolean condition, @NotNull UnaryOperator<@NotNull MethodPredicate> ifTrue, @NotNull UnaryOperator<@NotNull MethodPredicate> ifFalse) {
		return (condition ? ifTrue : ifFalse).apply(this);
	}

	public @NotNull MethodPredicate isStatic() {
		this.isStaticV();
		return this;
	}

	public @NotNull MethodPredicate notStatic() {
		this.notStaticV();
		return this;
	}

	public @NotNull MethodPredicate annotations(@NotNull Predicate<? super AnnotationContainer> annotationsPredicate) {
		this.annotationsV(annotationsPredicate);
		return this;
	}

	public @NotNull MethodPredicate declaringType(Hash.@NotNull Strategy<ReifiedType<?>> strategy, @NotNull ReifiedType<?> declaringType) {
		this.declaringTypeV(strategy, declaringType);
		return this;
	}

	public @NotNull MethodPredicate actualMember(@NotNull Predicate<? super AnnotatedElement> actualMemberPredicate) {
		this.actualMemberV(actualMemberPredicate);
		return this;
	}

	public @NotNull MethodPredicate constructorLike(@NotNull ReifiedType<?> target) {
		return this.add(
			method -> method.isConstructorLike(target),
			builder -> builder.append("constructorLike: ").append(target)
		);
	}

	public @NotNull MethodPredicate notConstructorLike(@NotNull ReifiedType<?> target) {
		return this.add(
			method -> !method.isConstructorLike(target),
			builder -> builder.append("notConstructorLike: ").append(target)
		);
	}

	public @NotNull MethodPredicate methodType(@NotNull MethodType methodType) {
		return this.add(
			method -> method.getMethodType() == methodType,
			builder -> builder.append("methodType: ").append(methodType)
		);
	}

	public @NotNull MethodPredicate methodType(@NotNull Predicate<? super MethodType> methodTypePredicate) {
		return this.add(
			method -> methodTypePredicate.test(method.getMethodType()),
			builder -> builder.append("methodTypePredicate: ").append(methodTypePredicate)
		);
	}

	public @NotNull MethodPredicate returnType(Hash.@NotNull Strategy<ReifiedType<?>> strategy, @NotNull ReifiedType<?> type) {
		return this.add(
			method -> strategy.equals(method.getReturnType(), type),
			builder -> builder.append("returnType: ").append(type).append(" (strategy: ").append(strategy).append(')')
		);
	}

	public @NotNull MethodPredicate returnType(@NotNull Predicate<? super ReifiedType<?>> returnTypePredicate) {
		return this.add(
			method -> returnTypePredicate.test(method.getReturnType()),
			builder -> builder.append("returnTypePredicate: ").append(returnTypePredicate)
		);
	}

	public @NotNull MethodPredicate returnsVoid() {
		return this.add(
			method -> method.getReturnType().getRawClass() == void.class,
			builder -> builder.append("returnsVoid: true")
		);
	}

	public @NotNull MethodPredicate returnsNotVoid() {
		return this.add(
			method -> method.getReturnType().getRawClass() != void.class,
			builder -> builder.append("returnsVoid: false")
		);
	}

	public @NotNull MethodPredicate parameterCount(int count) {
		return this.add(
			method -> method.getParameterCount() == count,
			builder -> builder.append("parameterCount: ").append(count)
		);
	}

	public @NotNull MethodPredicate parameterCount(@NotNull IntPredicate countPredicate) {
		return this.add(
			method -> countPredicate.test(method.getParameterCount()),
			builder -> builder.append("parameterCountPredicate: ").append(countPredicate)
		);
	}

	public @NotNull MethodPredicate parameters(@NotNull Predicate<? super ParameterView<?, ?>[]> parametersPredicate) {
		return this.add(
			method -> parametersPredicate.test(method.getParameters()),
			builder -> builder.append("parametersPredicate: ").append(parametersPredicate)
		);
	}

	public @NotNull MethodPredicate parameter(int index, @NotNull Predicate<? super ParameterView<?, ?>> parameterPredicate) {
		return this.add(
			method -> parameterPredicate.test(method.getParameters()[index]),
			builder -> builder.append("parameterPredicate[").append(index).append("]: ").append(parameterPredicate)
		);
	}

	public @NotNull MethodPredicate parameterTypes(Hash.@NotNull Strategy<ReifiedType<?>> strategy, @NotNull ReifiedType<?> @NotNull ... parameterTypes) {
		return this.add(
			method -> HashStrategies.orderedArrayEquals(strategy, method.getParameterTypes(), parameterTypes),
			builder -> builder.append("parameterTypes: ").append(Arrays.toString(parameterTypes)).append(" (strategy: ").append(strategy).append(')')
		);
	}

	public @NotNull MethodPredicate parameterTypes(@NotNull Predicate<? super ReifiedType<?>[]> parameterTypesPredicate) {
		return this.add(
			method -> parameterTypesPredicate.test(method.getParameterTypes()),
			builder -> builder.append("parameterTypesPredicate: ").append(parameterTypesPredicate)
		);
	}

	public @NotNull MethodPredicate parameterType(int index, Hash.@NotNull Strategy<ReifiedType<?>> strategy, ReifiedType<?> parameterType) {
		return this.add(
			method -> strategy.equals(method.getParameterTypes()[index], parameterType),
			builder -> builder.append("parameterType[").append(index).append("]: ").append(parameterType).append(" (strategy: ").append(strategy).append(')')
		);
	}

	public @NotNull MethodPredicate parameterType(int index, @NotNull Predicate<? super ReifiedType<?>> parameterTypePredicate) {
		return this.add(
			method -> parameterTypePredicate.test(method.getParameterTypes()[index]),
			builder -> builder.append("parameterTypePredicate[").append(index).append("]: ").append(parameterTypePredicate)
		);
	}
}