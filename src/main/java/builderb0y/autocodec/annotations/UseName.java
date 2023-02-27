package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;

/**
changes the name this field or record component is serialized as.
this can be used for maintaining backwards compatibility with serialized data,
or naming encoded values things that java doesn't normally allow.
example usage: {@code
	public class Special {

		public @UseName("default") int defaultValue = 0;
	}
}
this would serialize as: {@code
	{ "default": 0 }
}
instead of: {@code
	{ "defaultValue": 0 }
}
see also: {@link FieldLikeMemberView#getSerializedName()}

this annotation can also be applied to enum constants to change the name they get serialized as.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseName {

	public abstract String value();
}