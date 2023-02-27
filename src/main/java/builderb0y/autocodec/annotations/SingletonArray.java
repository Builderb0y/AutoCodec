package builderb0y.autocodec.annotations;

import java.lang.annotation.*;

import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
applicable to arrays, indicates that the array can be
serialized and deserialized from its singleton element.
for example, the array [ "foo" ] will be serialized as "foo",
and the string "foo" can be deserialized into [ "foo" ].

annotations on arrays are not all that common,
so it is worth repeating the fact that the correct way to annotate the
type String[] is String @SingletonArray [], not @SingletonArray String[].
the former will annotate the array type String[],
where as the latter will annotate its component type String.

also whenever you have a multi-dimensional array,
the order of annotations is backwards.
given the type @A String @B [] @C [],
A annotates the inner-most component type String,
B annotates the outer-most array type String[][],
and C annotates the intermediate type String[].
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SingletonArray {

	/**
	convenience instance of SingletonArray to be used
	whenever an instance of one is needed at runtime.
	for example, {@link ReifiedType#addAnnotations(Annotation...)}.
	*/
	public static final SingletonArray INSTANCE = new SingletonArray() {

		@Override
		public Class<? extends Annotation> annotationType() {
			return SingletonArray.class;
		}
	};
}