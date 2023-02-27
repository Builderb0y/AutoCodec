package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.ReflectionManager;

/**
when applied to a member (record component, method, constructor, or field),
the annotated member will not (by default) be reported by {@link ReflectionManager#canView(Field)},
and therefore will not be reported by {@link ReflectionManager#getFields(ReflectContext, boolean)} either.
in other words, the ReflectionManager will pretend the annotated member does not exist.

when applied to a class, the class will not be flagged
as viewable by {@link ReflectionManager#canView(Class)},
and therefore none of its members will be reported.

note that when applied to a record component,
other logic elsewhere may use the list of record
components to search for a canonical record constructor,
and if some of those components are marked as @Hidden,
then the "canonical" constructor will not include them.
as such, it is important to provide a "canonical"
record constructor which includes parameters for all
non-@Hidden components, in the order they are declared.
*/
@Target({
	ElementType.RECORD_COMPONENT,
	ElementType.METHOD,
	ElementType.CONSTRUCTOR,
	ElementType.FIELD,
	ElementType.TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Hidden {}