package builderb0y.autocodec.common;

import java.lang.invoke.MethodHandles;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
{@link MethodLikeMemberView#createMethodHandle(ReflectContextProvider)}
and similar methods on {@link FieldLikeMemberView} are... a bit weird.
problem 1:
	the method one only uses a ReflectContext to obtain a {@link MethodHandles.Lookup},
	and it does not need the rest of the context.
	but I want the field one to have the same parameters as the method one,
	but the field one needs the rest of the context.
problem 2:
	ideally both should take a ReflectContext<T_Owner>,
	but it is impossible to provide one when the owner type is a wildcard.
	ugly workarounds are often used instead, and I don't like that.
problem 3:
	more often than not, these calls take the form
	method.createMethodHandle(factoryContext.reflect(...)).
	it would be more convenient to make createMethodHandle() take a FactoryContext
	instead of a ReflectContext, but that means giving it even more stuff it doesn't need,
	and it also means forcing you to have a FactoryContext available, when you may not.
solution:
	I make the above methods take a ReflectContextProvider
	instead of an actual ReflectContext.
	then, I make {@link FactoryContext}, {@link ReflectContext},
	and {@link AutoCodec} all implement ReflectContextProvider.
	as long as I have one of these 3 objects at my disposal,
	I can provide it to the method or field, and let it figure out
	how to get the Lookup or whatever else it needs for its own owner.
	sound good? I think so.
*/
public interface ReflectContextProvider {

	/**
	provides reflective access to the given type.
	the returned {@link ReflectContext} can be used
	to query fields and methods in the given type,
	as well as obtain a {@link MethodHandles.Lookup}
	for the given type.
	*/
	public abstract <T_Owner> @NotNull ReflectContext<T_Owner> reflect(@NotNull ReifiedType<T_Owner> type);
}