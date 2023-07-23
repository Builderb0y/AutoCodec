package builderb0y.autocodec.coders;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
an AutoCoder which maps the input and output of another AutoCoder, similar to how
{@link AutoCoder#mapCoder(ReifiedType, HandlerMapper, HandlerMapper)} works.
this class offers a more transparent view of the backing map than
mapCoder(type, encode::get, decode::get) would.
*/
public class LookupCoder<T_Key, T_Value> extends NamedCoder<T_Value> {

	public final @NotNull AutoCoder<T_Key> keyCoder;
	public final @NotNull Map<@NotNull T_Key, @NotNull T_Value> decode;
	public final @NotNull Map<@NotNull T_Value, @NotNull T_Key> encode;

	public LookupCoder(
		@NotNull ReifiedType<T_Value> handledType,
		@NotNull AutoCoder<T_Key> keyCoder,
		@NotNull Map<@NotNull T_Key, @NotNull T_Value> decode,
		@NotNull Map<@NotNull T_Value, @NotNull T_Key> encode
	) {
		super(handledType);
		if (!decode.isEmpty()) throw new IllegalArgumentException("decode map already contains entries: " + decode);
		if (!encode.isEmpty()) throw new IllegalArgumentException("encode map already contains entries: " + encode);
		this.toString = this.toString + ": " + keyCoder;
		this.keyCoder = keyCoder;
		this.decode = decode;
		this.encode = encode;
	}

	public LookupCoder(
		@NotNull ReifiedType<T_Value> handledType,
		@NotNull AutoCoder<T_Key> keyCoder
	) {
		this(handledType, keyCoder, new HashMap<>(), new HashMap<>());
	}

	public LookupCoder(
		@NotNull String toString,
		@NotNull AutoCoder<T_Key> keyCoder,
		@NotNull Map<@NotNull T_Key, @NotNull T_Value> decode,
		@NotNull Map<@NotNull T_Value, @NotNull T_Key> encode
	) {
		super(toString);
		if (!decode.isEmpty()) throw new IllegalArgumentException("decode map already contains entries: " + decode);
		if (!encode.isEmpty()) throw new IllegalArgumentException("encode map already contains entries: " + encode);
		this.keyCoder = keyCoder;
		this.decode = decode;
		this.encode = encode;
	}

	public LookupCoder(@NotNull String toString, @NotNull AutoCoder<T_Key> keyCoder) {
		this(toString, keyCoder, new HashMap<>(), new HashMap<>());
	}

	public void add(T_Key key, T_Value value) {
		if (this.decode.containsKey(key)) throw new IllegalArgumentException("Key already present: " + key);
		if (this.encode.containsKey(value)) throw new IllegalArgumentException("Value already present: " + value);
		this.decode.put(key, value);
		this.encode.put(value, key);
	}

	@Override
	public <T_Encoded> @Nullable T_Value decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) return null;
		T_Key key = context.decodeWith(this.keyCoder);
		if (key == null) throw new DecodeException(() -> "Unknown key: " + context.input);
		T_Value value = this.decode.get(key);
		if (value == null) throw new DecodeException(() -> "Unknown key: " + key);
		return value;
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Value> context) throws EncodeException {
		T_Value input = context.input;
		if (input == null) return context.empty();
		T_Key key = this.encode.get(input);
		if (key == null) throw new EncodeException(() -> "Unknown value: " + input);
		return context.input(key).encodeWith(this.keyCoder);
	}

	@Override
	public String toString() {
		return this.toString + " -> " + this.decode;
	}
}