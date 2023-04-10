package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
verifies that the annotated String, Map, Collection, or array must not be empty.

internally, this annotation is just a {@link Mirror}
for {@link VerifySizeRange}(min = 1).
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@VerifySizeRange(min = 1)
@Mirror(VerifySizeRange.class)
public @interface VerifyNotEmpty {

}