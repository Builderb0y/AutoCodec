package builderb0y.autocodec.coders;

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
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
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

public class RecordCoder<T_DecodedRecord> extends MultiFieldEncoder<T_DecodedRecord> implements AutoCoder<T_DecodedRecord> {

	//constructor:
	//	(Foo, Bar) -> T_DecodedRecord
	//filter arguments: via context -> context.decodeWith(FieldStrategy<Foo | Bar>):
	//	(DecodeContext<T_DecodedRecord>, DecodeContext<T_DecodedRecord>) -> T_DecodedRecord
	//permute arguments:
	//	(DecodeContext<T_DecodedRecord>) -> T_DecodedRecord
	public final @NotNull MethodHandle decoder;

	public RecordCoder(@NotNull ReifiedType<T_DecodedRecord> handledType, @NotNull MethodHandle decoder, @NotNull FieldStrategy<T_DecodedRecord, ?> @NotNull [] fields) {
		super(handledType, fields);
		this.decoder = decoder.asType(MethodType.methodType(Object.class, DecodeContext.class));
	}

	@Override
	@OverrideOnly
	@SuppressWarnings("unchecked")
	public <T_Encoded> @Nullable T_DecodedRecord decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		try {
			return (T_DecodedRecord)(this.decoder.invokeExact(context));
		}
		catch (DecodeException | Error exception) {
			throw exception;
		}
		catch (Throwable throwable) {
			throw new DecodeException(throwable);
		}
	}

	public static class Factory extends NamedCoderFactory {

		public static final @NotNull MethodHandle DECODE_CONTEXT_DECODE_WITH;
		static {
			try {
				DECODE_CONTEXT_DECODE_WITH = MethodHandles.publicLookup().findVirtual(DecodeContext.class, "decodeWith", MethodType.methodType(Object.class, AutoDecoder.class));
			}
			catch (Throwable throwable) {
				throw AutoCodecUtil.rethrow(throwable);
			}
		}
		public static final Factory INSTANCE = new Factory();

		public <T_Owner> Map<@NotNull String, @NotNull FieldLikeMemberView<T_Owner, ?>> collectFields(@NotNull FactoryContext<T_Owner> context) {
			return (
				Arrays.stream(context.reflect().getFields(true))
				.filter(new FieldPredicate().notStatic())
				.collect(Collectors.toMap(FieldLikeMemberView::getName, Function.identity()))
			);
		}

		public <T_Owner> @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] findFieldsWithNames(
			@NotNull FactoryContext<T_Owner> context,
			@NotNull Stream<@NotNull String> toFind
		) {
			Map<String, FieldLikeMemberView<T_Owner, ?>> map = this.collectFields(context);
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

		public <T_Owner> @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] findFieldsWithTypes(@NotNull FactoryContext<T_Owner> context, @NotNull Stream<@NotNull ParameterView<T_Owner, ?>> toFind) {
			Map<String, FieldLikeMemberView<T_Owner, ?>> map = this.collectFields(context);
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

		@SuppressWarnings("unchecked")
		public <T_Owner> @NotNull MethodLikeMemberView<?, T_Owner> findConstructor(
			@NotNull FactoryContext<T_Owner> context,
			@NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] fields,
			@NotNull String name,
			@NotNull ReifiedType<?> in
		)
		throws FactoryException {
			return (MethodLikeMemberView<?, T_Owner>)(
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

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> RecordCoder<?> bind(
			@NotNull FactoryContext<T_HandledType> context,
			@NotNull MethodLikeMemberView<?, T_HandledType> constructor,
			@NotNull FieldLikeMemberView<T_HandledType, ?> @NotNull [] fields
		)
		throws FactoryException {
			try {
				//(fields[0].type, fields[1].type, fields[2].type, ...) -> T_RecordType
				MethodHandle constructorHandle = constructor.createMethodHandle(context);
				int length = fields.length;
				assert constructorHandle.type().parameterCount() == length;
				FieldStrategy<?, ?>[] strategies = new FieldStrategy<?, ?>[length];
				for (int index = 0; index < length; index++) {
					strategies[index] = new FieldStrategy(
						fields[index],
						fields[index].createInstanceReader(context),
						context.type(fields[index].getType()).forceCreateCoder(),
						fields[index].getType().getAnnotations().has(EncodeInline.class)
					);
				}
				MethodHandle[] filters = new MethodHandle[length];
				for (int index = 0; index < length; index++) {
					filters[index] = (
						MethodHandles
						.insertArguments(DECODE_CONTEXT_DECODE_WITH, 1, strategies[index])
						.asType(MethodType.methodType(constructorHandle.type().parameterType(index), DecodeContext.class))
					);
				}
				//(DecodeContext, DecodeContext, DecodeContext, ...) -> T_RecordType
				constructorHandle = MethodHandles.filterArguments(
					constructorHandle,
					0,
					filters
				);
				//(DecodeContext) -> T_RecordType
				constructorHandle = MethodHandles.permuteArguments(
					constructorHandle,
					MethodType.methodType(
						constructorHandle.type().returnType(),
						DecodeContext.class
					),
					new int[length]
				);
				return new RecordCoder(context.type, constructorHandle, strategies);
			}
			catch (FactoryException exception) {
				throw exception;
			}
			catch (Exception exception) {
				throw new FactoryException(exception);
			}
		}

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			//first priority: the @RecordLike annotation.
			RecordLike annotation = context.type.getAnnotations().getFirst(RecordLike.class);
			if (annotation != null) {
				context.logger().logMessageLazy(() -> "Found " + annotation);
				FieldLikeMemberView<T_HandledType, ?>[] fields = this.findFieldsWithNames(context, Arrays.stream(annotation.value()));
				MethodLikeMemberView<?, T_HandledType> constructor = this.findConstructor(
					context,
					fields,
					annotation.name(),
					annotation.in() == void.class
					? context.type
					: ReifiedType.parameterizeWithWildcards(annotation.in())
				);
				return this.bind(context, constructor, fields);
			}
			//second priority: actual records.
			Class<?> rawClass = context.type.getRawClass();
			if (rawClass != null && rawClass.isRecord()) {
				context.logger().logMessage("Class is an actual record.");
				FieldLikeMemberView<T_HandledType, ?>[] fields = (
					context
					.reflect()
					.searchFields(
						false,
						new FieldPredicate().notStatic(),
						MemberCollector.tryAll()
					)
					.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
				);
				MethodLikeMemberView<?, T_HandledType> constructor = this.findConstructor(context, fields, "new", context.type);
				return this.bind(context, constructor, fields);
			}
			//third priority: classes with only a single constructor.
			@SuppressWarnings("unchecked")
			MethodLikeMemberView<T_HandledType, T_HandledType> constructor = (MethodLikeMemberView<T_HandledType, T_HandledType>)(
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
				FieldLikeMemberView<T_HandledType, ?>[] fields = this.findFieldsWithTypes(context, Arrays.stream(constructor.getParameters()));
				return this.bind(context, constructor, fields);
			}
			//all of the above tactics failed.
			context.logger().logMessage("Not a record-like class.");
			return null;
		}
	}
}