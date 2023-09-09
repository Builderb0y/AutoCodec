package builderb0y.autocodec.annotations;

import java.lang.annotation.*;

import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
when applied to type {@link String}, the String will
be {@link String#intern()}ed when it is decoded.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Intern {

	/**
	convenience instance of Intern to be used
	whenever an instance of one is needed at runtime.
	for example, {@link ReifiedType#addAnnotations(Annotation...)}.
	*/
	public static final Intern INSTANCE = new Intern() {

		@Override
		public Class<? extends Annotation> annotationType() {
			return Intern.class;
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public String toString() {
			return '@' + Intern.class.getName() + "()";
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public int hashCode() {
			return 0;
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Intern;
		}
	};
}