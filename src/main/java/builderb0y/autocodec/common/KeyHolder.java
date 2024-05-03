package builderb0y.autocodec.common;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.verifiers.AutoVerifier;

/**
implemented by {@link AutoEncoder}, {@link AutoDecoder}, and {@link AutoImprinter},
but not by {@link AutoConstructor} or {@link AutoVerifier}.

if this handler deals with encoded values as objects with known keys,
then {@link #getKeys()} returns those keys.
if this handler deals with encoded values as any other type
(like numbers, arrays, strings, or booleans),
or if the encoded values are objects, but the keys are not known in advance,
then {@link #getKeys()} returns null.

this interface assists with the creation of {@link MapEncoder}'s, {@link MapDecoder}'s,
and {@link MapCodec}'s, by providing the keys used for their compression logic.
*/
public interface KeyHolder {

	public default @Nullable Stream<String> getKeys() {
		return null;
	}

	public default boolean hasKeys() {
		Stream<String> keys = this.getKeys();
		if (keys != null) {
			keys.close();
			return true;
		}
		else {
			return false;
		}
	}
}