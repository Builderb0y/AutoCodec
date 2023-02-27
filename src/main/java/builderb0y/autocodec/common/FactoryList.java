package builderb0y.autocodec.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.AutoHandler.AutoFactory;
import builderb0y.autocodec.common.AutoHandler.NamedFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public abstract class FactoryList<
	T_Handler extends AutoHandler,
	T_Factory extends AutoFactory<T_Handler>
>
extends NamedFactory<T_Handler> {

	public final @NotNull AutoCodec autoCodec;
	public final @NotNull List<T_Factory> factories = new ArrayList<>(16);

	public FactoryList(@NotNull AutoCodec autoCodec) {
		this.autoCodec = autoCodec;
		this.setup();
	}

	public AutoCodec autoCodec() {
		return this.autoCodec;
	}

	//////////////////////////////// setup ////////////////////////////////

	/**
	sets up this FactoryList, and adds all the
	relevant factories to its {@link #factories} list.
	many additional methods are provided below
	this to make setup easier for subclasses,
	particularly anonymous subclasses.
	the concrete subclasses provided by
	default will add a few built-in factories,
	and anonymous subclasses of these concrete
	subclasses can modify the factories list after that.
	*/
	@OverrideOnly
	public void setup() {
		this.addFactoryToEnd(this.createLookupFactory());
	}

	@OverrideOnly
	public abstract @NotNull T_Factory createLookupFactory();

	public int indexOf(@NotNull T_Factory factory) {
		int index = this.factories.indexOf(factory);
		if (index >= 0) return index;
		else throw new IllegalStateException("Factory not present: " + factory);
	}

	public int indexOf(@NotNull Class<? extends T_Factory> factoryClass) {
		List<T_Factory> factories = this.factories;
		for (int index = 0, size = factories.size(); index < size; index++) {
			if (factoryClass.isInstance(factories.get(index))) {
				return index;
			}
		}
		throw new IllegalStateException("Factory not present: " + factoryClass);
	}

	public void addFactoryToStart(@NotNull T_Factory start) {
		this.factories.add(0, start);
	}

	public void addFactoryToEnd(@NotNull T_Factory end) {
		this.factories.add(end);
	}

	public void addFactoryAfter(@NotNull T_Factory before, @NotNull T_Factory after) {
		this.factories.add(this.indexOf(before) + 1, after);
	}

	public void addFactoryBefore(@NotNull T_Factory after, @NotNull T_Factory before) {
		this.factories.add(this.indexOf(after), before);
	}

	public void addFactoryAfter(@NotNull Class<? extends T_Factory> beforeClass, @NotNull T_Factory after) {
		this.factories.add(this.indexOf(beforeClass) + 1, after);
	}

	public void addFactoryBefore(@NotNull Class<? extends T_Factory> afterClass, @NotNull T_Factory before) {
		this.factories.add(this.indexOf(afterClass), before);
	}

	@SafeVarargs
	public final void addFactoriesToStart(@NotNull T_Factory @NotNull ... start) {
		this.factories.addAll(0, Arrays.asList(start));
	}

	@SafeVarargs
	public final void addFactoriesToEnd(@NotNull T_Factory @NotNull ... end) {
		this.factories.addAll(Arrays.asList(end));
	}

	@SafeVarargs
	public final void addFactoriesAfter(@NotNull T_Factory before, @NotNull T_Factory @NotNull ... after) {
		this.factories.addAll(this.indexOf(before) + 1, Arrays.asList(after));
	}

	@SafeVarargs
	public final void addFactoriesBefore(@NotNull T_Factory after, @NotNull T_Factory @NotNull ... before) {
		this.factories.addAll(this.indexOf(after), Arrays.asList(before));
	}

	@SafeVarargs
	public final void addFactoriesAfter(@NotNull Class<? extends T_Factory> beforeClass, @NotNull T_Factory @NotNull ... after) {
		this.factories.addAll(this.indexOf(beforeClass) + 1, Arrays.asList(after));
	}

	@SafeVarargs
	public final void addFactoriesBefore(@NotNull Class<? extends T_Factory> afterClass, @NotNull T_Factory @NotNull ... before) {
		this.factories.addAll(this.indexOf(afterClass), Arrays.asList(before));
	}

	public void removeFactory(@NotNull T_Factory factory) {
		this.factories.remove(this.indexOf(factory));
	}

	public void removeFactory(@NotNull Class<? extends T_Factory> factoryClass) {
		this.factories.remove(this.indexOf(factoryClass));
	}

	public void replaceFactory(@NotNull T_Factory oldFactory, @NotNull T_Factory newFactory) {
		this.factories.set(this.indexOf(oldFactory), newFactory);
	}

	public void replaceFactory(@NotNull Class<? extends T_Factory> oldFactoryClass, @NotNull T_Factory newFactory) {
		this.factories.set(this.indexOf(oldFactoryClass), newFactory);
	}

	@SuppressWarnings("unchecked")
	public <T_ExtendedFactory extends T_Factory> @NotNull T_ExtendedFactory getFactory(@NotNull Class<T_ExtendedFactory> factoryClass) {
		return (T_ExtendedFactory)(this.factories.get(this.indexOf(factoryClass)));
	}

	//////////////////////////////// request handling ////////////////////////////////

	public final @NotNull Map<@NotNull ReifiedType<?>, @NotNull T_Handler> cache = new Object2ObjectOpenCustomHashMap<>(256, ReifiedType.ORDERED_ANNOTATIONS_STRATEGY);

	/**
	used to create handlers which self-reference. for example: {@code
		public class Foo {
			public Foo parent;
		}
	}

	under normal circumstances, the request for Foo would itself make another request for Foo,
	and *that* request would make another request for Foo,
	and this repeats until something stack overflows.

	to solve this issue, the first time a request for Foo is made,
	we create a "lazy" handler for Foo at the same time,
	and both are put in the requestStack.
	this lazy handler is initially incapable of doing anything,
	but when that first request has been satisfied,
	the lazy handler obtains a reference to the actual handler,
	and then it will be able to delegate handle tasks to the actual handler.
	the fun part though is in the second, recursive request.
	that request will simply receive the lazy handler.
	that way, as soon as the first request has been fulfilled,
	both requests will automatically work.
	*/
	public final @NotNull Map<@NotNull ReifiedType<?>, @NotNull LazyHandler<T_Handler>> requestStack = new Object2ObjectOpenCustomHashMap<>(16, ReifiedType.ORDERED_ANNOTATIONS_STRATEGY);

	public abstract @NotNull LazyHandler<T_Handler> createLazyHandler();

	/**
	attempts to create a handler from our list of {@link #factories}.
	the exact way in which this works depends on {@link #doCreate(FactoryContext)},
	but in most cases, the first factory which returns a non-null handler is used.
	if all factories return null, then this method returns null.
	*/
	@Override
	@OverrideOnly
	public <T_HandledType> @Nullable T_Handler tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
		ReifiedType<?> type = context.type;
		T_Handler handler = this.cache.get(type);
		if (handler != null) {
			context.logger().logMessage("Found cached handler.");
			return handler;
		}
		LazyHandler<T_Handler> lazy = this.requestStack.get(type);
		if (lazy != null) {
			context.logger().logMessage("Recursive request. Using lazy handler.");
			return lazy.getThisHandler();
		}
		lazy = this.createLazyHandler();
		this.requestStack.put(type, lazy);
		try {
			context.logger().logMessage("No cached or lazy handler found. Creating a new handler...");
			handler = this.doCreate(context);
			if (handler != null) {
				lazy.setDelegateHandler(handler);
				this.cache.put(type, handler);
			}
			return handler;
		}
		finally {
			this.requestStack.remove(type);
		}
	}

	public @Nullable T_Handler doCreate(@NotNull FactoryContext<?> context) throws FactoryException {
		for (T_Factory factory : this.factories) {
			T_Handler handler = context.logger().tryCreateHandler(factory, context);
			if (handler != null) return handler;
		}
		return null;
	}

	public @Nullable T_Handler tryCreateFallback(@NotNull FactoryContext<?> context, @NotNull T_Factory caller) throws FactoryException {
		List<T_Factory> factories = this.factories;
		for (int index = this.indexOf(caller), length = factories.size(); ++index < length;) {
			T_Handler handler = context.logger().tryCreateHandler(factories.get(index), context);
			if (handler != null) return handler;
		}
		return null;
	}

	public @NotNull T_Handler forceCreateFallback(@NotNull FactoryContext<?> context, @NotNull T_Factory caller) throws FactoryException {
		T_Handler handler = this.tryCreateFallback(context, caller);
		if (handler != null) return handler;
		throw new FactoryException(this + " cannot create fallback handler for " + context + "; called by: " + caller);
	}
}