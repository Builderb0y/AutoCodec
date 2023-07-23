package builderb0y.autocodec.reflection;

import java.lang.reflect.AnnotatedElement;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class FieldPredicate extends MemberPredicate<FieldLikeMemberView<?, ?>> {

	public @NotNull FieldPredicate add(@NotNull Predicate<? super FieldLikeMemberView<?, ?>> predicate, @NotNull Consumer<StringBuilder> toString) {
		this.addV(predicate, toString);
		return this;
	}

	public @NotNull FieldPredicate name(@NotNull String name) {
		this.nameV(name);
		return this;
	}

	public @NotNull FieldPredicate name(@NotNull Predicate<? super String> namePredicate) {
		this.nameV(namePredicate);
		return this;
	}

	public @NotNull FieldPredicate apply(@NotNull UnaryOperator<@NotNull FieldPredicate> operator) {
		return operator.apply(this);
	}

	public @NotNull FieldPredicate applyConditional(boolean condition, @NotNull UnaryOperator<@NotNull FieldPredicate> ifTrue, @NotNull UnaryOperator<@NotNull FieldPredicate> ifFalse) {
		return (condition ? ifTrue : ifFalse).apply(this);
	}

	public @NotNull FieldPredicate serializedName(@NotNull String name) {
		return this.add(
			field -> field.getSerializedName().equals(name),
			builder -> builder.append("serializedName: ").append(name)
		);
	}

	public @NotNull FieldPredicate serializedName(@NotNull Predicate<? super String> namePredicate) {
		return this.add(
			field -> namePredicate.test(field.getSerializedName()),
			builder -> builder.append("serializedNamePredicate: ").append(namePredicate)
		);
	}

	public @NotNull FieldPredicate alias(@NotNull String @NotNull ... aliases) {
		if (aliases.length == 0) {
			throw new IllegalArgumentException("Must provide at least one alias.");
		}
		return this.add(
			field -> {
				String[] realAliases = field.getAliases();
				for (String requestedAlias : aliases) {
					for (String realAlias : realAliases) {
						if (realAlias.equals(requestedAlias)) return true;
					}
				}
				return false;
			},
			builder -> {
				builder.append("aliases: [").append(aliases[0]);
				for (int index = 1, length = aliases.length; index < length; index++) {
					builder.append(", ").append(aliases[index]);
				}
				builder.append(']');
			}
		);
	}

	public @NotNull FieldPredicate isStatic() {
		this.isStaticV();
		return this;
	}

	public @NotNull FieldPredicate notStatic() {
		this.notStaticV();
		return this;
	}

	public @NotNull FieldPredicate isFinal() {
		return this.add(
			FieldLikeMemberView::isFinal,
			builder -> builder.append("final: true")
		);
	}

	public @NotNull FieldPredicate notFinal() {
		return this.add(
			field -> !field.isFinal(),
			builder -> builder.append("final: false")
		);
	}

	public @NotNull FieldPredicate type(Hash.@NotNull Strategy<ReifiedType<?>> strategy, @NotNull ReifiedType<?> type) {
		return this.add(
			field -> strategy.equals(field.getType(), type),
			builder -> builder.append("type: ").append(type).append(" (strategy: ").append(strategy).append(')')
		);
	}

	public @NotNull FieldPredicate type(@NotNull Predicate<? super ReifiedType<?>> typePredicate) {
		return this.add(
			field -> typePredicate.test(field.getType()),
			builder -> builder.append("typePredicate: ").append(typePredicate)
		);
	}

	public @NotNull FieldPredicate actualMember(@NotNull Predicate<? super AnnotatedElement> actualMemberPredicate) {
		this.actualMemberV(actualMemberPredicate);
		return this;
	}

	public @NotNull FieldPredicate annotations(@NotNull Predicate<? super AnnotationContainer> annotationsPredicate) {
		this.annotationsV(annotationsPredicate);
		return this;
	}

	public @NotNull FieldPredicate declaringType(Hash.@NotNull Strategy<ReifiedType<?>> strategy, @NotNull ReifiedType<?> declaringType) {
		this.declaringTypeV(strategy, declaringType);
		return this;
	}

	public @NotNull FieldPredicate declaringType(@NotNull Predicate<? super ReifiedType<?>> declaringTypePredicate) {
		this.declaringTypeV(declaringTypePredicate);
		return this;
	}
}