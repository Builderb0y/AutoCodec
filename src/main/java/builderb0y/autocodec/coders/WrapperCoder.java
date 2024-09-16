package builderb0y.autocodec.coders;

import java.lang.invoke.MethodHandle;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.WrapperSpec;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.manipulators.InstanceReader;

public class WrapperCoder<T_Wrapper, T_Wrapped> extends NamedCoder<T_Wrapper> {

	public final @NotNull WrapperSpec<T_Wrapper, T_Wrapped> spec;
	public final @NotNull MethodHandle constructorHandle;
	public final @NotNull InstanceReader<T_Wrapper, T_Wrapped> getter;
	public final @NotNull AutoCoder<T_Wrapped> wrappedCoder;

	public WrapperCoder(
		@NotNull WrapperSpec<T_Wrapper, T_Wrapped> spec,
		@NotNull MethodHandle constructorHandle,
		@NotNull InstanceReader<T_Wrapper, T_Wrapped> getter,
		@NotNull AutoCoder<T_Wrapped> wrappedCoder
	) {
		super(spec.wrapperType());
		this.spec = spec;
		this.constructorHandle = constructorHandle;
		this.getter = getter;
		this.wrappedCoder = wrappedCoder;
	}

	@Override
	@OverrideOnly
	@SuppressWarnings("unchecked")
	public <T_Encoded> @Nullable T_Wrapper decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		try {
			if (context.isEmpty()) {
				if (this.spec.wrapNull()) {
					return (T_Wrapper)(this.constructorHandle.invokeExact((Object)(null)));
				}
				else {
					return null;
				}
			}
			else {
				return (T_Wrapper)(this.constructorHandle.invokeExact(context.decodeWith(this.wrappedCoder)));
			}
		}
		catch (DecodeException | Error exception) {
			throw exception;
		}
		catch (Throwable throwable) {
			throw new DecodeException(throwable);
		}
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Wrapper> context) throws EncodeException {
		T_Wrapper wrapper = context.object;
		if (wrapper == null) return context.empty();
		return context.object(this.getter.get(wrapper)).encodeWith(this.wrappedCoder);
	}

	@Override
	public String toString() {
		return super.toString() + ": { wrappedCodec: " + this.wrappedCoder + " }";
	}

	public static class Factory extends NamedCoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			WrapperSpec<T_HandledType, ?> spec = WrapperSpec.find(context);
			if (spec != null) try {
				AutoCoder<?> wrappedCoder = context.type(spec.wrappedType()).forceCreateCoder();
				MethodHandle handle = spec.constructor().createMethodHandle(context);
				InstanceReader<T_HandledType, ?> getter = spec.field().createInstanceReader(context);
				return new WrapperCoder(spec, handle, getter, wrappedCoder);
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException(exception);
			}
			else {
				return null;
			}
		}
	}
}