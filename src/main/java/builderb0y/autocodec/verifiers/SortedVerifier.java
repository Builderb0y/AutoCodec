package builderb0y.autocodec.verifiers;

import java.util.*;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.VerifySorted;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.manipulators.InstanceReader;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;

//todo: test this.
public class SortedVerifier<T_Owner, T_Member> implements AutoVerifier<T_Owner> {

	public final @NotNull InstanceReader<T_Owner, ? extends Comparable<T_Member>> mainField;
	public final @NotNull InstanceReader<T_Owner, ? extends T_Member> @NotNull []
		lessThan,
		greaterThan,
		lessThanOrEqual,
		greaterThanOrEqual;

	public SortedVerifier(
		@NotNull InstanceReader<T_Owner, ? extends Comparable<T_Member>> mainField,
		@NotNull InstanceReader<T_Owner, ? extends T_Member> @NotNull [] lessThan,
		@NotNull InstanceReader<T_Owner, ? extends T_Member> @NotNull [] greaterThan,
		@NotNull InstanceReader<T_Owner, ? extends T_Member> @NotNull [] lessThanOrEqual,
		@NotNull InstanceReader<T_Owner, ? extends T_Member> @NotNull [] greaterThanOrEqual
	) {
		this.mainField          = mainField;
		this.lessThan           = lessThan;
		this.greaterThan        = greaterThan;
		this.lessThanOrEqual    = lessThanOrEqual;
		this.greaterThanOrEqual = greaterThanOrEqual;
	}

	public void fail(@NotNull VerifyContext<?, T_Owner> context, @NotNull InstanceReader<T_Owner, ? extends T_Member> cause, @NotNull String why) throws VerifyException {
		throw new VerifyException(context.pathToStringBuilder().append('.').append(this.mainField.getMember().getSerializedName()).append(" must be ").append(why).append(' ').append(cause.getMember().getSerializedName()).toString());
	}

	@Override
	public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, T_Owner> context) throws VerifyException {
		T_Owner object = context.object;
		if (object == null) return;
		Comparable<T_Member> comparable = this.mainField.get(object);
		if (comparable == null) return;
		for (InstanceReader<T_Owner, ? extends T_Member> reader : this.lessThan) {
			T_Member compareTo = reader.get(object);
			if (compareTo != null && comparable.compareTo(compareTo) >= 0) {
				this.fail(context, reader, "less than");
			}
		}
		for (InstanceReader<T_Owner, ? extends T_Member> reader : this.greaterThan) {
			T_Member compareTo = reader.get(object);
			if (compareTo != null && comparable.compareTo(compareTo) <= 0) {
				this.fail(context, reader, "greater than");
			}
		}
		for (InstanceReader<T_Owner, ? extends T_Member> reader : this.lessThanOrEqual) {
			T_Member compareTo = reader.get(object);
			if (compareTo != null && comparable.compareTo(compareTo) > 0) {
				this.fail(context, reader, "less than or equal to");
			}
		}
		for (InstanceReader<T_Owner, ? extends T_Member> reader : this.greaterThanOrEqual) {
			T_Member compareTo = reader.get(object);
			if (compareTo != null && comparable.compareTo(compareTo) < 0) {
				this.fail(context, reader, "greater than or equal to");
			}
		}
	}

	public static class Factory extends NamedVerifierFactory {

		public static final Factory INSTANCE = new Factory();

		public static <T_Owner, T_Member> @NotNull InstanceReader<T_Owner, T_Member> reader(@NotNull FieldLikeMemberView<T_Owner, T_Member> field, @NotNull FactoryContext<?> context) {
			try {
				return field.createInstanceReader(context);
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException(exception);
			}
		}

		public <T_Owner> @NotNull InstanceReader<? super T_Owner, ?> @NotNull [] process(
			@NotNull FactoryContext<T_Owner> context,
			@NotNull FieldLikeMemberView<? super T_Owner, ?> field,
			@NotNull Function<@NotNull VerifySorted, @NotNull String @NotNull []> namesGetter
		) {
			ReifiedType<?> target = field.getType().boxed().resolveParameter(Comparable.class);
			if (target == null) throw new FactoryException("@VerifySorted applied to wrong type: Expected Comparable, got " + field.getType() + " on field " + field);
			VerifySorted annotation = field.getType().getAnnotations().getFirst(VerifySorted.class);
			assert annotation != null : field + " not annotated with @VerifySorted";
			String[] names = namesGetter.apply(annotation);
			int nameCount = names.length;
			InstanceReader<? super T_Owner, ?>[] readers = InstanceReader.ARRAY_FACTORY.applyGeneric(nameCount);
			for (int nameIndex = 0; nameIndex < nameCount; nameIndex++) {
				readers[nameIndex] = reader(
					context
					.reflect(field.getDeclaringType())
					.searchFields(
						true,
						new FieldPredicate()
						.name(names[nameIndex])
						.notStatic()
						.type(ReifiedType.BOXED_GENERIC_TYPE_STRATEGY, target),
						MemberCollector.forceFirst()
					),
					context
				);
			}
			return readers;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_Owner> @NotNull SortedVerifier<T_Owner, ?> makeVerifier(
			@NotNull FactoryContext<T_Owner> context,
			@NotNull FieldLikeMemberView<T_Owner, ?> field
		) {
			return new SortedVerifier(
				reader(field, context),
				this.process(context, field, VerifySorted::lessThan),
				this.process(context, field, VerifySorted::greaterThan),
				this.process(context, field, VerifySorted::lessThanOrEqual),
				this.process(context, field, VerifySorted::greaterThanOrEqual)
			);
		}

		@Override
		public <T_HandledType> @Nullable AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			List<FieldLikeMemberView<T_HandledType, ?>> fields = (
				context.reflect().searchFields(
					true,
					new FieldPredicate()
					.type(new NamedPredicate<>(
						(ReifiedType<?> type) -> type.getAnnotations().has(VerifySorted.class),
						"Annotated with @VerifySorted"
					)),
					MemberCollector.tryAll()
				)
			);
			return switch (fields.size()) {
				case 0 -> null;
				case 1 -> this.makeVerifier(context, fields.get(0));
				default -> new MultiVerifier<>(
					fields
					.stream()
					.map((FieldLikeMemberView<T_HandledType, ?> field) -> this.makeVerifier(context, field))
					.toArray(AutoVerifier.ARRAY_FACTORY.generic())
				);
			};
		}
	}
}