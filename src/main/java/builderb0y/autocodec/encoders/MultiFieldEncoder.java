package builderb0y.autocodec.encoders;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.EncodeInline;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.manipulators.InstanceReader;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class MultiFieldEncoder<T_Decoded> extends NamedEncoder<T_Decoded> {

	public final @NotNull FieldStrategy<T_Decoded, ?> @NotNull [] fields;

	@SafeVarargs
	public MultiFieldEncoder(@NotNull ReifiedType<T_Decoded> type, @NotNull FieldStrategy<T_Decoded, ?> @NotNull ... fields) {
		super(type);
		this.fields = fields;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		if (context.input == null) return context.empty();
		Map<T_Encoded, T_Encoded> map = new LinkedHashMap<>(8);
		for (FieldStrategy<T_Decoded, ?> field : this.fields) {
			field.encodeOnto(map, context);
		}
		return context.createGenericMap(map);
	}

	@Override
	public String toString() {
		return this.toString + ": { " + this.fields.length + " fields: " + Arrays.stream(this.fields).map((FieldStrategy<T_Decoded, ?> field) -> field.field.getSerializedName()).collect(Collectors.joining(", ")) + " }";
	}

	public static abstract class FieldStrategy<T_Owner, T_Member> {

		public static final @NotNull ObjectArrayFactory<FieldStrategy<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(FieldStrategy.class).generic();

		public final @NotNull FieldLikeMemberView<T_Owner, T_Member> field;
		public final @NotNull InstanceReader<T_Owner, T_Member> reader;
		public final @NotNull AutoEncoder<T_Member> encoder;

		public FieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceReader<T_Owner, T_Member> reader,
			@NotNull AutoEncoder<T_Member> encoder
		) {
			this.field   = field;
			this.reader  = reader;
			this.encoder = encoder;
		}

		public static <T_Owner, T_Member> @Nullable FieldStrategy<T_Owner, T_Member> of(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull FactoryContext<?> context
		)
		throws IllegalAccessException {
			AutoEncoder<T_Member> encoder = context.type(field.getType()).tryCreateEncoder();
			if (encoder == null) return null;
			return of(field, encoder, context);
		}

		public static <T_Owner, T_Member> @NotNull FieldStrategy<T_Owner, T_Member> of(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull AutoEncoder<T_Member> encoder,
			@NotNull FactoryContext<?> context
		)
		throws IllegalAccessException {
			InstanceReader<T_Owner, T_Member> reader = field.createInstanceReader(context);
			if (field.getType().getAnnotations().has(EncodeInline.class)) {
				return new InlineFieldStrategy<>(field, reader, encoder);
			}
			else {
				return new NonInlineFieldStrategy<>(field, reader, encoder);
			}
		}

		public abstract <T_Encoded> void encodeOnto(@NotNull Map<T_Encoded, T_Encoded> map, @NotNull EncodeContext<T_Encoded, T_Owner> context) throws EncodeException;
	}

	public static class InlineFieldStrategy<T_Owner, T_Member> extends FieldStrategy<T_Owner, T_Member> {

		public InlineFieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceReader<T_Owner, T_Member> reader,
			@NotNull AutoEncoder<T_Member> encoder
		) {
			super(field, reader, encoder);
		}

		@Override
		public <T_Encoded> void encodeOnto(@NotNull Map<T_Encoded, T_Encoded> map, @NotNull EncodeContext<T_Encoded, T_Owner> context) throws EncodeException {
			if (context.input == null) return;
			T_Member member = this.reader.get(context.input);
			T_Encoded newMap = context.input(member).encodeWith(this.encoder);
			context.logger().unwrap(
				context.ops.getMapValues(newMap),
				true,
				EncodeException::new
			)
			.filter((Pair<T_Encoded, T_Encoded> pair) -> !Objects.equals(context.ops.empty(), pair.getSecond()))
			.forEach((Pair<T_Encoded, T_Encoded> pair) -> map.put(pair.getFirst(), pair.getSecond()));
		}
	}

	public static class NonInlineFieldStrategy<T_Owner, T_Member> extends FieldStrategy<T_Owner, T_Member> {

		public NonInlineFieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceReader<T_Owner, T_Member> reader,
			@NotNull AutoEncoder<T_Member> encoder
		) {
			super(field, reader, encoder);
		}

		@Override
		public <T_Encoded> void encodeOnto(@NotNull Map<T_Encoded, T_Encoded> map, @NotNull EncodeContext<T_Encoded, T_Owner> context) throws EncodeException{
			if (context.input == null) return;
			T_Member decodedMember = this.reader.get(context.input);
			T_Encoded encodedMember = context.input(decodedMember).encodeWith(this.encoder);
			if (!Objects.equals(context.ops.empty(), encodedMember)) {
				map.put(context.createString(this.field.getSerializedName()), encodedMember);
			}
		}
	}

	public static class Factory extends NamedEncoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public @Nullable <T_HandledType> AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			FieldLikeMemberView<?, ?>[] fields = (
				Arrays.stream(context.reflect().getFields(true))
				.filter(new FieldPredicate().notStatic())
				.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
			);
			int length = fields.length;
			context.logger().logMessageLazy(() -> "Found " + length + " field(s) that are applicable for imprinting.");
			//if (length == 0) return null;
			FieldStrategy<?, ?>[] strategies = FieldStrategy.ARRAY_FACTORY.applyGeneric(length);
			for (int index = 0; index < length; index++) try {
				FieldStrategy<?, ?> strategy = FieldStrategy.of(fields[index], context);
				if (strategy == null) return null;
				strategies[index] = strategy;
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException(exception);
			}
			return new MultiFieldEncoder(context.type, strategies);
		}
	}
}