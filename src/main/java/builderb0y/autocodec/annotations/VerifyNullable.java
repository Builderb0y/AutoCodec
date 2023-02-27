package builderb0y.autocodec.annotations;

import java.lang.annotation.*;

import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.NotNullVerifier;

/**
indicates that the annotated element is allowed to be null during verification.
null most often appears when the serialized
representation of an object lacks a requested member,
so this annotation states that the member is allowed to be absent.
this annotation can be applied to any type, including primitives.
when applied to a primitive type, this annotation
will still allow the member to be absent,
and the field's default value will be used.

see also: {@link NotNullVerifier}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyNullable {

	/**
	convenience instance of VerifyNullable to be used
	whenever an instance of one is needed at runtime.
	for example, {@link ReifiedType#addAnnotations(Annotation...)}.
	*/
	public static final VerifyNullable INSTANCE = new VerifyNullable() {

		@Override
		public Class<? extends Annotation> annotationType() {
			return VerifyNullable.class;
		}
	};
}