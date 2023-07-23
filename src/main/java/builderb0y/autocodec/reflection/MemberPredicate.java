package builderb0y.autocodec.reflection;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.reflection.memberViews.MemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.TypeFormatter;

public class MemberPredicate<M extends MemberView<?>> implements Predicate<M> {

	public final @NotNull List<@NotNull Predicate<? super M>> predicates = new ArrayList<>(4);
	public final @NotNull List<@NotNull Consumer<StringBuilder>> toStrings = new ArrayList<>(4);

	public void addV(@NotNull Predicate<? super M> predicate, @NotNull Consumer<StringBuilder> toString) {
		this.predicates.add(predicate);
		this.toStrings.add(toString);
	}

	public void nameV(@NotNull String name) {
		this.addV(
			member -> member.getName().equals(name),
			builder -> builder.append("name: ").append(name)
		);
	}

	public void nameV(@NotNull Predicate<? super String> namePredicate) {
		this.addV(
			member -> namePredicate.test(member.getName()),
			builder -> builder.append("namePredicate: ").append(namePredicate)
		);
	}

	public void isStaticV() {
		this.addV(
			MemberView::isStatic,
			builder -> builder.append("static: true")
		);
	}

	public void notStaticV() {
		this.addV(
			member -> !member.isStatic(),
			builder -> builder.append("static: false")
		);
	}

	public void actualMemberV(@NotNull Predicate<? super AnnotatedElement> actualMemberPredicate) {
		this.addV(
			member -> actualMemberPredicate.test(member.getActualMember()),
			builder -> builder.append("actualMemberPredicate: ").append(actualMemberPredicate)
		);
	}

	public void annotationsV(@NotNull Predicate<? super AnnotationContainer> annotationsPredicate) {
		this.addV(
			member -> annotationsPredicate.test(member.getAnnotations()),
			builder -> builder.append("annotationsPredicate: ").append(annotationsPredicate)
		);
	}

	public void declaringTypeV(Hash.@NotNull Strategy<ReifiedType<?>> strategy, @NotNull ReifiedType<?> declaringType) {
		this.addV(
			member -> strategy.equals(member.getDeclaringType(), declaringType),
			builder -> builder.append("declaringType: ").append(declaringType).append(" (strategy: ").append(strategy).append(')')
		);
	}

	public void declaringTypeV(@NotNull Predicate<? super ReifiedType<?>> declaringTypePredicate) {
		this.addV(
			member -> declaringTypePredicate.test(member.getDeclaringType()),
			builder -> builder.append("declaringTypePredicate: ").append(declaringTypePredicate)
		);
	}

	@Override
	public boolean test(M object) {
		List<Predicate<? super M>> predicates = this.predicates;
		int size = predicates.size();
		for (int index = 0; index < size; index++) {
			if (!predicates.get(index).test(object)) return false;
		}
		return true;
	}

	public static <M extends MemberView<?>> boolean testAndDescribe(Predicate<? super M> predicate, M member, TaskLogger logger) {
		if (predicate instanceof MemberPredicate<? super M> memberPredicate) {
			return memberPredicate.testAndDescribe(member, logger);
		}
		else {
			boolean passed = predicate.test(member);
			logger.logMessageLazy(() -> passed ? "PASSED: " + member : "FAILED: " + member + " because predicate " + predicate + " was false.");
			return passed;
		}
	}

	public boolean testAndDescribe(M member, TaskLogger logger) {
		List<Predicate<? super M>> predicates = this.predicates;
		int size = predicates.size();
		for (int index = 0; index < size; index++) {
			if (!predicates.get(index).test(member)) {
				final int index_ = index; //lambdas -_-
				logger.logMessageLazy(() -> {
					StringBuilder builder = new StringBuilder(128);
					builder.append("FAILED: ").append(member).append(" because condition '");
					this.toStrings.get(index_).accept(builder);
					return builder.append("' was false.").toString();
				});
				return false;
			}
		}
		logger.logMessageLazy(() -> "PASSED: " + member);
		return true;
	}

	@Override
	public String toString() {
		int size = this.toStrings.size();
		StringBuilder builder = new StringBuilder((size + 1) << 5);
		TypeFormatter.appendSimpleClassUnchecked(builder, this.getClass());
		builder.append(": {");
		if (size > 0) {
			this.toStrings.get(0).accept(builder.append(' '));
			for (int index = 1; index < size; index++) {
				this.toStrings.get(index).accept(builder.append(", "));
			}
			builder.append(' ');
		}
		return builder.append('}').toString();
	}
}