package builderb0y.autocodec.data;

import java.util.stream.LongStream;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.util.ObjectArrayFactory;

public class LongListData extends Data {

	public static final @NotNull ObjectArrayFactory<LongListData> ARRAY_FACTORY = new ObjectArrayFactory<>(LongListData.class);

	public @NotNull LongList value;

	public LongListData() {
		this.value = new LongArrayList();
	}

	public LongListData(int capacity) {
		this.value = new LongArrayList(capacity);
	}

	public LongListData(@NotNull LongList value) {
		this.value = value;
	}

	public static @NotNull LongListData wrap(long @NotNull ... longs) {
		return new LongListData(LongArrayList.wrap(longs));
	}

	public static @NotNull LongListData collect(@NotNull LongStream stream) {
		return wrap(stream.toArray());
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createLongList(this.value.longStream());
	}

	public int size() {
		return this.value.size();
	}

	public long getLong(int index) {
		return this.value.getLong(index);
	}

	public long setLong(int index, long value) {
		return this.value.set(index, value);
	}

	public void append(long value) {
		this.value.add(value);
	}

	public long remove(int index) {
		return this.value.removeLong(index);
	}

	@Override
	public boolean equals(Object object) {
		LongListData longList;
		return object instanceof Data data && (longList = data.tryAsLongList()) != null && this.value.equals(longList.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	@Override
	public @NotNull Data deepCopy() {
		return new LongListData(new LongArrayList(this.value));
	}
}