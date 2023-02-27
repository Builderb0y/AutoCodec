package builderb0y.autocodec.constructors;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.common.LookupFactory;
import builderb0y.autocodec.constructors.AutoConstructor.ConstructorFactory;
import builderb0y.autocodec.reflection.ReflectionManager;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.TypeFormatter;

public class LookupConstructorFactory extends LookupFactory<AutoConstructor<?>> implements ConstructorFactory {

	@Override
	@OverrideOnly
	public void setup() {
		//java.util
		this.addSimple(       Deque.class, ArrayDeque.class);
		this.addSimple(        List.class,  ArrayList.class);
		this.addSimple(         Map.class,    HashMap.class);
		this.addSimple(NavigableMap.class,    TreeMap.class);
		this.addSimple(NavigableSet.class,    TreeSet.class);
		this.addSimple(       Queue.class, ArrayDeque.class);
		this.addSimple(         Set.class,    HashSet.class);
		this.addSimple(   SortedMap.class,    TreeMap.class);
		this.addSimple(   SortedSet.class,    TreeSet.class);
		//java.util.concurrent
		this.addSimple(         BlockingDeque.class,   LinkedBlockingDeque.class);
		this.addSimple(         BlockingQueue.class,   LinkedBlockingQueue.class);
		this.addSimple(         ConcurrentMap.class,     ConcurrentHashMap.class);
		this.addSimple(ConcurrentNavigableMap.class, ConcurrentSkipListMap.class);
		//no such thing as ConcurrentNavigableSet.
		this.addSimple(         TransferQueue.class,   LinkedTransferQueue.class);
	}

	public <T> void addGeneric(@NotNull ReifiedType<T> type, @NotNull AutoConstructor<T> constructor) {
		this.doAddGeneric(type, constructor);
	}

	public <T> void addRaw(@NotNull Class<T> type, @NotNull AutoConstructor<T> constructor) {
		this.doAddRaw(type, constructor);
	}

	/**
	adds a constructor for an implementation of an interface.
	for example, if the interfaceClass was List.class,
	then the implementationClass might be ArrayList.class or LinkedList.class.
	this method will find a simple no-arg constructor in the implementationClass,
	and use that for the AutoConstructor implementation.

	WARNING: this method bypasses visibility checks that would normally
	be performed by {@link ReflectionManager#canView(Constructor)}!
	if you are overriding that method to hide the constructors
	of any of the classes in {@link #setup()},
	then you should also override {@link #setup()}
	to not add them to this LookupConstructorFactory.
	*/
	public <T> void addSimple(@NotNull Class<T> interfaceClass, Class<? extends T> implementationClass) {
		try {
			this.addRaw(
				interfaceClass,
				new MethodHandleConstructor<>(
					TypeFormatter.appendSimpleClassUnchecked(
						new StringBuilder(32),
						implementationClass
					)
					.append("::new")
					.toString(),
					MethodHandles.dropArguments(
						MethodHandles.lookup().findConstructor(
							implementationClass,
							MethodType.methodType(void.class)
						),
						0,
						ConstructContext.class
					)
				)
			);
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}