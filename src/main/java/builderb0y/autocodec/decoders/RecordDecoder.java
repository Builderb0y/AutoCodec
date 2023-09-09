package builderb0y.autocodec.decoders;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.EncodeInline;
import builderb0y.autocodec.annotations.RecordLike;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.encoders.MultiFieldEncoder;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.ParameterView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.autocodec.util.NamedPredicate;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class RecordDecoder<T_Decoded> extends NamedDecoder<T_Decoded> {

	public static final MethodType HANDLE_TYPE = MethodType.methodType(Object.class, DecodeContext.class);

	/**
	a single handle controls ALL the underlying fields.
	the implementation of this handle is as follows: {@code
		public <T_Encoded> T_Decoded invoke(DecodeContext<T_Encoded> context) {
			return new T_Decoded(field0.decode(context), field1.decode(context), field2.decode(context), ...);
		}
	}
	*/
	public final @NotNull MethodHandle constructor;
	/**
	not used outside of toString(), and that doesn't
	need much more info than just the parameter *names*,
	but we will retain a reference to the full
	decoder array anyway for aid in debugging.
	*/
	public final @NotNull FieldStrategy<?> @NotNull [] components;

	public RecordDecoder(@NotNull ReifiedType<T_Decoded> type, @NotNull MethodHandle constructor, @NotNull FieldStrategy<?> @NotNull [] components) {
		super(type);
		this.constructor = constructor.asType(HANDLE_TYPE);
		this.components = components;
	}

	@Override
	@OverrideOnly
	@SuppressWarnings("unchecked")
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		try {
			return (T_Decoded)(this.constructor.invokeExact(context));
		}
		catch (DecodeException | Error exception) {
			throw exception;
		}
		catch (Throwable throwable) {
			throw new DecodeException(throwable);
		}
	}

	@Override
	public String toString() {
		return this.toString + ": { " + this.components.length + " components: " + Arrays.stream(this.components).map((FieldStrategy<?> parameter) -> parameter.field.getSerializedName()).collect(Collectors.joining(", ")) + " }";
	}

	public static abstract class FieldStrategy<T_Member> extends NamedDecoder<T_Member> {

		public static final @NotNull ObjectArrayFactory<FieldStrategy<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(FieldStrategy.class).generic();

		public final @NotNull FieldLikeMemberView<?, T_Member> field;
		public final @NotNull AutoDecoder<T_Member> decoder;

		public FieldStrategy(@NotNull FieldLikeMemberView<?, T_Member> field, @NotNull AutoDecoder<T_Member> decoder) {
			super(field.getType());
			this.field = field;
			this.decoder = decoder;
		}

		public static <T_Member> FieldStrategy<T_Member> of(@NotNull FieldLikeMemberView<?, T_Member> field, @NotNull AutoDecoder<T_Member> decoder) {
			return (
				field.getType().getAnnotations().has(EncodeInline.class)
				? new InlineFieldStrategy<>(field, decoder)
				: new NonInlineFieldStrategy<>(field, decoder)
			);
		}

		@Override
		public String toString() {
			return this.toString + ": [field: " + this.field.getSerializedName() + ']';
		}
	}

	public static class InlineFieldStrategy<T_Member> extends FieldStrategy<T_Member> {

		public static final @NotNull ObjectArrayFactory<InlineFieldStrategy<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(InlineFieldStrategy.class).generic();

		public InlineFieldStrategy(@NotNull FieldLikeMemberView<?, T_Member> field, @NotNull AutoDecoder<T_Member> decoder) {
			super(field, decoder);
		}

		@Override
		public <T_Encoded> @Nullable T_Member decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			return context.decodeWith(this.decoder);
		}
	}

	public static class NonInlineFieldStrategy<T_Member> extends FieldStrategy<T_Member> {

		public static final @NotNull ObjectArrayFactory<NonInlineFieldStrategy<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(NonInlineFieldStrategy.class).generic();

		public NonInlineFieldStrategy(@NotNull FieldLikeMemberView<?, T_Member> field, @NotNull AutoDecoder<T_Member> decoder) {
			super(field, decoder);
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable T_Member decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			return context.getFirstMember(this.field.getAliases()).decodeWith(this.decoder);
		}
	}

	public static class Factory extends NamedDecoderFactory {

		public static final @NotNull MethodHandle DECODE_CONTEXT_DECODE_WITH;
		static {
			try {
				DECODE_CONTEXT_DECODE_WITH = MethodHandles.publicLookup().findVirtual(DecodeContext.class, "decodeWith", MethodType.methodType(Object.class, AutoDecoder.class));
			}
			catch (Throwable throwable) {
				throw AutoCodecUtil.rethrow(throwable);
			}
		}
		public static final @NotNull Factory INSTANCE = new Factory();

		public static record ConstructorFields<T_Owner>(
			@NotNull MethodLikeMemberView<?, ?> constructor,
			@NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] fields
		) {}

		public <T_Owner> @NotNull MethodLikeMemberView<?, ?> findConstructor(@NotNull FactoryContext<T_Owner> context, @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] fields, @NotNull String name, @NotNull ReifiedType<?> in) throws FactoryException {
			return (
				context.reflect(in).searchMethods(
					false,
					new MethodPredicate()
					.name(name)
					.constructorLike(context.type)
					.parameterTypes(
						ReifiedType.GENERIC_TYPE_STRATEGY,
						Arrays
						.stream(fields)
						.map(FieldLikeMemberView::getType)
						.toArray(ReifiedType.ARRAY_FACTORY.generic())
					),
					MemberCollector.forceUnique()
				)
			);
		}

		public <T_Owner> @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] findFields(@NotNull FactoryContext<?> context, @NotNull Stream<@NotNull String> toFind) {
			Map<String, FieldLikeMemberView<?, ?>> map = (
				Arrays.stream(context.reflect().getFields(true))
				.filter(new FieldPredicate().notStatic())
				.collect(Collectors.toMap(FieldLikeMemberView::getName, Function.identity()))
			);
			return (
				toFind
				.map((String name) -> {
					FieldLikeMemberView<?, ?> field = map.get(name);
					if (field != null) return field;
					else throw new FactoryException("Cannot find field " + name + " in " + context.type + " or its super classes or super interfaces.");
				})
				.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
			);
		}

		public <T_Owner> @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] findFieldsWithTypes(@NotNull FactoryContext<?> context, @NotNull Stream<@NotNull ParameterView<T_Owner, ?>> toFind) {
			Map<String, FieldLikeMemberView<?, ?>> map = (
				Arrays.stream(context.reflect().getFields(true))
				.filter(new FieldPredicate().notStatic())
				.collect(Collectors.toMap(FieldLikeMemberView::getName, Function.identity()))
			);
			return (
				toFind
				.map((ParameterView<T_Owner, ?> parameter) -> {
					FieldLikeMemberView<?, ?> field = map.get(parameter.getName());
					if (field != null && ReifiedType.GENERIC_TYPE_STRATEGY.equals(field.getType(), parameter.getType())) return field;
					else throw new FactoryException("Cannot find field " + parameter.getName() + " of type " + parameter.getType() + " in " + context.type + " or its super classes or super interfaces.");
				})
				.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
			);
		}

		public <T_Owner> @Nullable ConstructorFields<T_Owner> getApplicableConstructor(@NotNull FactoryContext<T_Owner> context) {
			//first priority: the @RecordLike annotation.
			RecordLike annotation = context.type.getAnnotations().getFirst(RecordLike.class);
			if (annotation != null) {
				context.logger().logMessageLazy(() -> "Found " + annotation);
				FieldLikeMemberView<T_Owner, ?>[] fields = this.findFields(context, Arrays.stream(annotation.value()));
				MethodLikeMemberView<?, ?> constructor = this.findConstructor(context, fields, annotation.name(), annotation.in() == void.class ? context.type : ReifiedType.parameterizeWithWildcards(annotation.in()));
				return new ConstructorFields<>(constructor, fields);
			}
			//second priority: actual records.
			Class<?> rawClass = context.type.getRawClass();
			if (rawClass != null && rawClass.isRecord()) {
				context.logger().logMessage("Class is an actual record.");
				FieldLikeMemberView<T_Owner, ?>[] fields = (
					context
					.reflect()
					.searchFields(
						false,
						new FieldPredicate().notStatic(),
						MemberCollector.tryAll()
					)
					.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
				);
				MethodLikeMemberView<?, ?> constructor = this.findConstructor(context, fields, "new", context.type);
				return new ConstructorFields<>(constructor, fields);
			}
			//third priority: classes with only a single constructor.
			MethodLikeMemberView<T_Owner, ?> constructor = (
				context.reflect().searchMethods(
					false,
					new MethodPredicate()
					.name("new")
					.constructorLike(context.type)
					.parameters(new NamedPredicate<>(
						(ParameterView<?, ?>[] params) -> Arrays.stream(params).allMatch(ParameterView::hasName),
						"All parameters have names"
					)),
					MemberCollector.tryUnique()
				)
			);
			if (constructor != null && constructor.getParameterCount() != 0) {
				context.logger().logMessageLazy(() -> "Class has exactly one constructor: " + constructor);
				FieldLikeMemberView<T_Owner, ?>[] fields = this.findFieldsWithTypes(context, Arrays.stream(constructor.getParameters()));
				return new ConstructorFields<>(constructor, fields);
			}
			//all of the above tactics failed.
			context.logger().logMessage("Not a record-like class.");
			return null;
		}

		public <T_Owner, T_HandledType> @NotNull MethodHandle handleFor(@NotNull FactoryContext<?> context, @NotNull MethodLikeMemberView<T_Owner, T_HandledType> method) {
			try {
				return method.createMethodHandle(context);
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException(exception);
			}
		}

		public @NotNull MethodHandle addParametersToHandle(@NotNull MethodHandle constructorHandle, @NotNull FieldStrategy<?> @NotNull [] fieldStrategies) {
			int length = fieldStrategies.length;
			MethodHandle[] handles = new MethodHandle[length];
			for (int index = 0; index < length; index++) {
				MethodHandle handle = DECODE_CONTEXT_DECODE_WITH;
				//handle == (DecodeContext, AutoDecoder) -> Object
				handle = MethodHandles.insertArguments(handle, 1, fieldStrategies[index]);
				//handle == (DecodeContext) -> Object
				handle = MethodHandles.explicitCastArguments(handle, MethodType.methodType(constructorHandle.type().parameterType(index), DecodeContext.class));
				//handle == (DecodeContext) -> fields[index].type
				handles[index] = handle;
			}
			//constructorHandle == (fields[0].type, fields[1].type, fields[2].type, ...) -> T_HandledType
			constructorHandle = MethodHandles.filterArguments(constructorHandle, 0, handles);
			//constructorHandle == (DecodeContext, DecodeContext, DecodeContext, ...) -> T_HandledType
			constructorHandle = MethodHandles.permuteArguments(constructorHandle, MethodType.methodType(constructorHandle.type().returnType(), DecodeContext.class), new int[length]);
			//constructorHandle == (DecodeContext) -> T_HandledType
			return constructorHandle;
		}

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			ConstructorFields<T_HandledType> constructorFields = this.getApplicableConstructor(context);
			if (constructorFields != null) {
				//create coders.
				MethodHandle constructorHandle = this.handleFor(context, constructorFields.constructor);
				FieldLikeMemberView[] fields = constructorFields.fields;
				int fieldCount = fields.length;
				AutoCoder[] coders = AutoCoder.ARRAY_FACTORY.apply(fieldCount);
				for (int index = 0; index < fieldCount; index++) {
					coders[index] = context.type(fields[index].getType()).forceCreateCoder();
				}

				//create decoder fields.
				RecordDecoder.FieldStrategy<?>[] recordFields = RecordDecoder.FieldStrategy.ARRAY_FACTORY.apply(fieldCount);
				for (int index = 0; index < fieldCount; index++) {
					recordFields[index] = RecordDecoder.FieldStrategy.of(fields[index], coders[index]);
				}
				constructorHandle = this.addParametersToHandle(constructorHandle, recordFields);
				RecordDecoder<T_HandledType> decoder = new RecordDecoder<>(context.type, constructorHandle, recordFields);

				//create encoder fields.
				MultiFieldEncoder.FieldStrategy<T_HandledType, ?>[] encoderFields = MultiFieldEncoder.FieldStrategy.ARRAY_FACTORY.applyGeneric(fieldCount);
				for (int index = 0; index < fieldCount; index++) try {
					encoderFields[index] = MultiFieldEncoder.FieldStrategy.of(fields[index], coders[index], context);
				}
				catch (IllegalAccessException exception) {
					throw new FactoryException(exception);
				}
				MultiFieldEncoder<T_HandledType> encoder = new MultiFieldEncoder<>(context.type, encoderFields);

				//bring them all together.
				return AutoCoder.of(encoder, decoder);
			}
			return null; //not a record or record-like.
		}
	}
}