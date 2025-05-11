package builderb0y.autocodec.coders;

import java.util.function.Function;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.EmptyData;
import builderb0y.autocodec.data.MapData;
import builderb0y.autocodec.data.StringData;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
for anyone who's worked with key dispatching before {@link Codec#dispatch(Function, Function)},
congrats! I still don't know how that works. but I do know what it's capable of doing,
so this class is my best attempt to support similar functionality.

a key dispatch is when the encoded elements are "represented" by a key.
the elements know how to be converted into a key,
and the key knows how to supply an AutoCoder<T_Decoded>.
the last piece of the puzzle is that there also exists an AutoCoder<T_Key> which,
as stated in the generic type, is capable of encoding and decoding keys.
typically, the key coder would be a {@link LookupCoder} whose key type is
{@link String}, and whose value type is the key dispatch coder's key type.
if these 3 conditions hold, then the decoding process first looks up the key from the
encoded data, and then uses that key to decode the actual object from the same data.
encoding converts the object to its key, then converts the key to an AutoCoder.
that AutoCoder is used to encode the object, and then the key itself is encoded
and merged with the encoded object.
*/
public abstract class KeyDispatchCoder<T_Key, T_Decoded> extends NamedCoder<T_Decoded> {

	public final @NotNull AutoCoder<T_Key> keyCoder;
	public final @NotNull String keyName;

	public KeyDispatchCoder(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoCoder<T_Key> keyCoder, @NotNull String keyName) {
		super(type);
		this.toString = this.toString + " via " + keyName + ' ' + keyCoder;
		this.keyCoder = keyCoder;
		this.keyName  = keyName;
	}

	public KeyDispatchCoder(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoCoder<T_Key> keyCoder) {
		this(type, keyCoder, "type");
	}

	public KeyDispatchCoder(@NotNull String toString, @NotNull AutoCoder<T_Key> keyCoder, @NotNull String keyName) {
		super(toString);
		this.keyCoder = keyCoder;
		this.keyName = keyName;
	}

	public KeyDispatchCoder(@NotNull String toString, @NotNull AutoCoder<T_Key> keyCoder) {
		this(toString, keyCoder, "type");
	}

	public abstract @Nullable T_Key getKey(@NotNull T_Decoded object);

	public abstract @Nullable AutoCoder<? extends T_Decoded> getCoder(@NotNull T_Key key);

	@Override
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		MapData map = context.forceAsMap();
		Data type = map.get(this.keyName);
		if (type.isEmpty()) throw new DecodeException(() -> "Missing key " + this.keyName);
		T_Key key = context.fork(this.keyName, type).decodeWith(this.keyCoder);
		if (key == null) throw new DecodeException(() -> "Key " + this.keyName + ' ' + type + " decoded into null");
		AutoCoder<? extends T_Decoded> coder = this.getCoder(key);
		if (coder == null) throw new DecodeException(() -> "No such coder for key " + this.keyName + ": " + key);
		return context.withData(map.without(this.keyName)).decodeWith(coder);
	}

	@Override
	public <T_Encoded> @NotNull Data encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		T_Decoded object = context.object;
		if (object == null) return EmptyData.INSTANCE;
		T_Key key = this.getKey(object);
		if (key == null) throw new EncodeException(() -> "No such key for " + object);
		@SuppressWarnings("unchecked")
		AutoCoder<T_Decoded> coder = (AutoCoder<T_Decoded>)(this.getCoder(key));
		if (coder == null) throw new EncodeException(() -> "No such coder for key " + key);
		Data data = context.encodeWith(coder);
		MapData map = data.tryAsMap();
		if (map == null) throw new EncodeException(() -> object + " encoded into non-map " + data + " and " + this.keyName + " cannot be stored.");
		map.value.put(new StringData(this.keyName), context.object(key).encodeWith(this.keyCoder));
		return data;
	}

	/**
	the root of all objects which know how to code themselves.
	typically, there will be many abstract classes, D, which implement
	Dispatchable<D>, and then many more concrete classes which extend D
	and implement {@link #getCoder()}.
	*/
	public static interface Dispatchable<D extends Dispatchable<D>> {

		/**
		returns an AutoCoder capable of coding instances of the actual
		subclass of Dispatchable represented by {@link #getClass()}.
		*/
		public abstract AutoCoder<? extends D> getCoder();
	}

	/**
	a simplified flow of KeyDispatchCoder where the keys are themselves AutoCoders,
	and the objects are instances of {@link Dispatchable}.
	*/
	public static class DispatchCoder<D extends Dispatchable<D>> extends KeyDispatchCoder<AutoCoder<? extends D>, D> {

		public DispatchCoder(
			@NotNull ReifiedType<D> type,
			@NotNull AutoCoder<AutoCoder<? extends D>> keyCoder,
			@NotNull String keyName
		) {
			super(type, keyCoder, keyName);
		}

		public DispatchCoder(
			@NotNull ReifiedType<D> type,
			@NotNull AutoCoder<AutoCoder<? extends D>> keyCoder
		) {
			super(type, keyCoder);
		}

		public DispatchCoder(
			@NotNull String toString,
			@NotNull AutoCoder<AutoCoder<? extends D>> keyCoder,
			@NotNull String keyName
		) {
			super(toString, keyCoder, keyName);
		}

		public DispatchCoder(
			@NotNull String toString,
			@NotNull AutoCoder<AutoCoder<? extends D>> keyCoder
		) {
			super(toString, keyCoder);
		}

		@Override
		public @Nullable AutoCoder<? extends D> getKey(@NotNull D object) {
			return object.getCoder();
		}

		@Override
		public @Nullable AutoCoder<? extends D> getCoder(@NotNull AutoCoder<? extends D> coder) {
			return coder;
		}
	}
}