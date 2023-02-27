package builderb0y.autocodec.common;

import java.lang.annotation.Annotation;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.Wrapper;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.AutoHandler.HandlerMapper;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.ParameterView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;
import builderb0y.autocodec.util.TypeFormatter;

/** intermediate parsing data from {@link Wrapper} */
public record WrapperSpec<T_Wrapper, T_Wrapped>(
	@NotNull FieldLikeMemberView<T_Wrapper, T_Wrapped> field,
	@NotNull MethodLikeMemberView<T_Wrapper, T_Wrapper> constructor,
	@NotNull ReifiedType<T_Wrapped> wrappedType,
	boolean wrapNull
) {

	public WrapperSpec(
		@NotNull FieldLikeMemberView<T_Wrapper, T_Wrapped> field,
		@NotNull MethodLikeMemberView<T_Wrapper, T_Wrapper> constructor,
		@NotNull List<@NotNull Annotation> annotations,
		boolean wrapNull
	) {
		this(field, constructor, field.getType().addAnnotations(annotations), wrapNull);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T_Owner> @Nullable WrapperSpec<T_Owner, ?> find(@NotNull FactoryContext<T_Owner> context, @NotNull List<@NotNull Annotation> annotations) {
		Wrapper annotation = context.type.getAnnotations().getFirst(Wrapper.class);
		if (annotation != null) {
			String fieldName = annotation.value();
			FieldLikeMemberView<T_Owner, ?> field;
			MethodLikeMemberView<T_Owner, ?> constructor;
			if (fieldName.isEmpty()) {
				constructor = context.reflect().searchMethods(
					false,
					new MethodPredicate()
					.isStatic()
					.constructorLike(context.type)
					.parameterCount(1)
					.parameter(0, new NamedPredicate<>(ParameterView::hasName, "Parameter has name")),
					MemberCollector.forceUnique()
				);
				fieldName = constructor.getParameters()[0].getName();
				assert fieldName != null : "0'th parameter had a name, but now it doesn't?";
				ReifiedType<?> type = constructor.getParameters()[0].getType();
				field = context.reflect().searchFields(
					true,
					new FieldPredicate()
					.notStatic()
					.name(fieldName)
					.type(ReifiedType.GENERIC_TYPE_STRATEGY, type),
					MemberCollector.forceUnique()
				);
			}
			else {
				field = context.reflect().searchFields(
					true,
					new FieldPredicate()
					.notStatic()
					.name(fieldName),
					MemberCollector.forceUnique()
				);
				constructor = context.reflect().searchMethods(
					false,
					new MethodPredicate()
					.isStatic()
					.constructorLike(context.type)
					.parameterCount(1)
					.parameterType(0, ReifiedType.GENERIC_TYPE_STRATEGY, field.getType()),
					MemberCollector.forceUnique()
				);
			}
			return new WrapperSpec(field, constructor, annotations, annotation.wrapNull());
		}
		return null;
	}

	public @NotNull String encodeMapperName() {
		return new TypeFormatter(128).annotations(true).simplify(true).append(this.field).toString();
	}

	public @NotNull HandlerMapper<@Nullable T_Wrapper, @Nullable T_Wrapped> encodeMapper(ReflectContextProvider provider) {
		try {
			return HandlerMapper.nullSafe(HandlerMapper.createLambda(this.field.createInstanceReaderHandle(provider)));
		}
		catch (IllegalAccessException exception) {
			throw new FactoryException(exception);
		}
	}

	public @NotNull String decodeMapperName() {
		return new TypeFormatter(128).annotations(true).simplify(true).append(this.constructor).toString();
	}

	public @NotNull HandlerMapper<@Nullable T_Wrapped, @Nullable T_Wrapper> decodeMapper(ReflectContextProvider provider) {
		try {
			HandlerMapper<T_Wrapped, T_Wrapper> mapper = HandlerMapper.createLambda(this.constructor.createMethodHandle(provider));
			if (!this.wrapNull) mapper = HandlerMapper.nullSafe(mapper);
			return mapper;
		}
		catch (IllegalAccessException exception) {
			throw new FactoryException(exception);
		}
	}

	public @NotNull AutoEncoder<T_Wrapper> createEncoder(@NotNull FactoryContext<?> context) {
		AutoEncoder<T_Wrapped> encoder = context.type(this.wrappedType).forceCreateEncoder();
		if (encoder instanceof AutoCoder<T_Wrapped> coder) {
			return coder.mapCoder(this.field.getDeclaringType(), this.encodeMapperName(), this.encodeMapper(context), this.decodeMapperName(), this.decodeMapper(context));
		}
		else {
			return encoder.mapEncoder(this.field.getDeclaringType(), this.encodeMapperName(), this.encodeMapper(context));
		}
	}

	public @NotNull AutoDecoder<T_Wrapper> createDecoder(@NotNull FactoryContext<?> context) {
		AutoDecoder<T_Wrapped> decoder = context.type(this.wrappedType).forceCreateDecoder();
		if (decoder instanceof AutoCoder<T_Wrapped> coder) {
			return coder.mapCoder(this.field.getDeclaringType(), this.encodeMapperName(), this.encodeMapper(context), this.decodeMapperName(), this.decodeMapper(context));
		}
		else {
			return decoder.mapDecoder(this.field.getDeclaringType(), this.decodeMapperName(), this.decodeMapper(context));
		}
	}
}