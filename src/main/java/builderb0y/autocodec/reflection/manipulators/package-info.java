/**
{@link java.lang.reflect.Field fields} are annoying to work with.
many methods related to creating them, getting their value,
and setting their value, declare that they throw exceptions.

the field access framework is an abstraction layer around that.
not only is it generified to include type parameters for the owner type and the field type,
but it also throws no exceptions, making it much more usable elsewhere.

instances of InstanceReader's/Writer's can be obtained via
{@link builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView#createInstanceReader(ReflectContextProvider)}
and similar methods.
*/
package builderb0y.autocodec.reflection.manipulators;

import builderb0y.autocodec.common.ReflectContextProvider;