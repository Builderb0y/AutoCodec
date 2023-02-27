package builderb0y.autocodec.imprinters;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.EncodeInline;
import builderb0y.autocodec.common.AutoHandler;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.imprinters.AutoImprinter.NamedImprinter;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.manipulators.InstanceReader;
import builderb0y.autocodec.reflection.manipulators.InstanceWriter;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.verifiers.AutoVerifier;
import builderb0y.autocodec.verifiers.NoopVerifier;

public class MultiFieldImprinter<T_Decoded> extends NamedImprinter<T_Decoded> {

	public final @NotNull FieldStrategy<T_Decoded, ?> @NotNull [] fields;

	@SafeVarargs
	public MultiFieldImprinter(@NotNull ReifiedType<T_Decoded> type, @NotNull FieldStrategy<T_Decoded, ?> @NotNull ... fields) {
		super(type);
		this.fields = fields;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Decoded> context) throws ImprintException {
		for (AutoImprinter<T_Decoded> field : this.fields) {
			context.imprintWith(field);
		}
	}

	@Override
	public String toString() {
		return this.toString + ": { " + this.fields.length + " fields: " + Arrays.stream(this.fields).map((FieldStrategy<T_Decoded, ?> field) -> field.field.getSerializedName()).collect(Collectors.joining(", ")) + " }";
	}

	public static abstract class FieldStrategy<T_Owner, T_Member> extends NamedImprinter<T_Owner> {

		public static final @NotNull ObjectArrayFactory<FieldStrategy<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(FieldStrategy.class).generic();

		public final @NotNull FieldLikeMemberView<T_Owner, T_Member> field;

		public FieldStrategy(@NotNull FieldLikeMemberView<T_Owner, T_Member> field, AutoHandler delegate) {
			super(delegate + " [field: " + field.getSerializedName() + ']');
			this.field = field;
		}

		public static <T_Owner, T_Member> @Nullable FieldStrategy<T_Owner, T_Member> forField(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull FactoryContext<?> context
		)
		throws IllegalAccessException {
			if (field.isStatic()) {
				throw new IllegalArgumentException("Static field.");
			}
			if (field.isFinal()) {
				AutoImprinter<T_Member> imprinter = context.type(field.getType()).tryCreateImprinter();
				if (imprinter == null) return null;
				AutoVerifier<T_Member> verifier = context.type(field.getType()).forceCreateVerifier();
				if (verifier != NoopVerifier.INSTANCE) {
					imprinter = new VerifyingImprinter<>(field.getType(), imprinter, verifier);
				}
				InstanceReader<T_Owner, T_Member> reader = field.createInstanceReader(context);
				if (field.getType().getAnnotations().has(EncodeInline.class)) {
					return new InlineImprintingFieldStrategy<>(field, reader, imprinter);
				}
				else {
					return new NonInlineImprintingFieldStrategy<>(field, reader, imprinter);
				}
			}
			else {
				AutoDecoder<T_Member> decoder = context.type(field.getType()).tryCreateDecoder();
				if (decoder == null) return null;
				InstanceWriter<T_Owner, T_Member> writer = field.createInstanceWriter(context);
				if (field.getType().getAnnotations().has(EncodeInline.class)) {
					return new InlineDecodingFieldStrategy<>(field, writer, decoder);
				}
				else {
					return new NonInlineDecodingFieldStrategy<>(field, writer, decoder);
				}
			}
		}
	}

	public static abstract class DecodingFieldStrategy<T_Owner, T_Member> extends FieldStrategy<T_Owner, T_Member> {

		public final @NotNull InstanceWriter<T_Owner, T_Member> writer;
		public final @NotNull AutoDecoder<T_Member> decoder;

		public DecodingFieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceWriter<T_Owner, T_Member> writer,
			@NotNull AutoDecoder<T_Member> decoder
		) {
			super(field, decoder);
			this.writer  = writer;
			this.decoder = decoder;
		}
	}

	public static class NonInlineDecodingFieldStrategy<T_Owner, T_Member> extends DecodingFieldStrategy<T_Owner, T_Member> {

		public NonInlineDecodingFieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceWriter<T_Owner, T_Member> writer,
			@NotNull AutoDecoder<T_Member> decoder
		) {
			super(field, writer, decoder);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Owner> context) throws ImprintException {
			try {
				DecodeContext<T_Encoded> member = context.getMember(this.field.getSerializedName());
				T_Member object = member.decodeWith(this.decoder);
				if (object != null) this.writer.set(context.object, object);
			}
			catch (ImprintException exception) {
				throw exception;
			}
			catch (DecodeException exception) {
				throw new ImprintException(exception);
			}
		}
	}

	public static class InlineDecodingFieldStrategy<T_Owner, T_Member> extends DecodingFieldStrategy<T_Owner, T_Member> {

		public InlineDecodingFieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceWriter<T_Owner, T_Member> writer,
			@NotNull AutoDecoder<T_Member> decoder
		) {
			super(field, writer, decoder);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Owner> context) throws ImprintException {
			try {
				this.writer.set(context.object, context.decodeWith(this.decoder));
			}
			catch (DecodeException exception) {
				throw new ImprintException(exception);
			}
		}
	}

	public static abstract class ImprintingFieldStrategy<T_Owner, T_Member> extends FieldStrategy<T_Owner, T_Member> {

		public final @NotNull InstanceReader<T_Owner, T_Member> reader;
		public final @NotNull AutoImprinter<T_Member> imprinter;

		public ImprintingFieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceReader<T_Owner, T_Member> reader,
			@NotNull AutoImprinter<T_Member> imprinter
		) {
			super(field, imprinter);
			this.reader = reader;
			this.imprinter = imprinter;
		}
	}

	public static class NonInlineImprintingFieldStrategy<T_Owner, T_Member> extends ImprintingFieldStrategy<T_Owner, T_Member> {

		public NonInlineImprintingFieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceReader<T_Owner, T_Member> reader,
			@NotNull AutoImprinter<T_Member> imprinter
		) {
			super(field, reader, imprinter);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Owner> context) throws ImprintException {
			DecodeContext<T_Encoded> member = context.getMember(this.field.getSerializedName());
			T_Member object = this.reader.get(context.object);
			if (object != null) member.imprintWith(this.imprinter, object);
		}
	}

	public static class InlineImprintingFieldStrategy<T_Owner, T_Member> extends ImprintingFieldStrategy<T_Owner, T_Member> {

		public InlineImprintingFieldStrategy(
			@NotNull FieldLikeMemberView<T_Owner, T_Member> field,
			@NotNull InstanceReader<T_Owner, T_Member> reader,
			@NotNull AutoImprinter<T_Member> imprinter
		) {
			super(field, reader, imprinter);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, T_Owner> context) throws ImprintException {
			context.imprintWith(this.imprinter, this.reader.get(context.object));
		}
	}

	public static class Factory extends NamedImprinterFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public @Nullable <T_HandledType> AutoImprinter<T_HandledType> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			FieldLikeMemberView<T_HandledType, ?>[] fields = (
				context
				.reflect(context.type.getLowerBoundOrSelf())
				.searchFields(
					true,
					new FieldPredicate().notStatic(),
					MemberCollector.tryAll()
				)
				.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
			);
			int length = fields.length;
			if (length == 0) return null;
			if (length == 1) try {
				return FieldStrategy.forField(fields[0], context);
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException(exception);
			}
			FieldStrategy<T_HandledType, ?>[] strategies = FieldStrategy.ARRAY_FACTORY.applyGeneric(length);
			for (int index = 0; index < length; index++) try {
				FieldStrategy<T_HandledType, ?> strategy = FieldStrategy.forField(fields[index], context);
				if (strategy == null) return null;
				strategies[index] = strategy;
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException(exception);
			}
			return new MultiFieldImprinter(context.type, strategies);
		}
	}
}