package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
applied to a type T, value() specifies a subclass of T to use for construction purposes.
example usage: {@code
	@UseImplementation(LinkedList.class) List<String> list;
}
when constructed, the list will be a LinkedList.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseImplementation {

	public abstract Class<?> value();
}