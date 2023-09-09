package builderb0y.autocodec.annotations;

import java.lang.annotation.*;

import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
indicates that the provided field should be encoded and decoded "inline".
to see how that works and what that means, let's take an example: {@code
	public record Point2D(int x, int y) {}
	public record Point3D(Point2D xy, int z) {}
	...
	Point3D point = new Point3D(new Point2D(1, 2), 3);
}
normally, point would encode into the following structure: {@code
	{
		"xy": {
			"x": 1,
			"y": 2
		},
		"z": 3
	}
}
by contrast, if Point3D were instead defined as {@code
	public record Point3D(@EncodeInline Point2D xy, int z) {}
}
then the same point would now be encoded as {@code
	{
		"x": 1,
		"y": 2,
		"z": 3
	}
}
it should go without saying that this annotation is
only applicable to types which encode into a map.
it is an error to apply this annotation to type String,
for example, as it does not encode into a map.
HOWEVER, if you really know what you're doing,
it is possible to provide an alternate EncoderFactory/DecoderFactory
which converts String's to maps, and then the annotation would work,
but why in god's name you would ever do such a thing is beyond me.
I am *totally* not doing this myself in Big Globe.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EncodeInline {

	/**
	convenience instance of EncodeInline to be used
	whenever an instance of one is needed at runtime.
	for example, {@link ReifiedType#addAnnotations(Annotation...)}.
	*/
	public static final EncodeInline INSTANCE = new EncodeInline() {

		@Override
		public Class<? extends Annotation> annotationType() {
			return EncodeInline.class;
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public String toString() {
			return '@' + EncodeInline.class.getName() + "()";
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public int hashCode() {
			return 0;
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public boolean equals(Object obj) {
			return obj instanceof EncodeInline;
		}
	};
}