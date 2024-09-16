package builderb0y.autocodec.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.Wrapper;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.MemberCollector;
import builderb0y.autocodec.reflection.MethodPredicate;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.ParameterView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.NamedPredicate;

/** intermediate parsing data from {@link Wrapper} */
public record WrapperSpec<T_Wrapper, T_Wrapped>(
	@NotNull FieldLikeMemberView<T_Wrapper, T_Wrapped> field,
	@NotNull MethodLikeMemberView<T_Wrapper, T_Wrapper> constructor,
	boolean wrapNull
) {

	public ReifiedType<T_Wrapper> wrapperType() {
		return this.field.getDeclaringType();
	}

	public ReifiedType<T_Wrapped> wrappedType() {
		return this.field.getType();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T_Owner> @Nullable WrapperSpec<T_Owner, ?> find(@NotNull FactoryContext<T_Owner> context) {
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
			return new WrapperSpec(field, constructor, annotation.wrapNull());
		}
		return null;
	}
}