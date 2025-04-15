package builderb0y.autocodec.data;

import java.nio.ByteBuffer;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import org.jetbrains.annotations.NotNull;

public class ByteListData extends Data {

	public @NotNull ByteList value;

	public ByteListData() {
		this.value = new ByteArrayList();
	}

	public ByteListData(int capacity) {
		this.value = new ByteArrayList(capacity);
	}

	public ByteListData(@NotNull ByteList value) {
		this.value = value;
	}

	public static @NotNull ByteListData wrap(byte @NotNull ... bytes) {
		return new ByteListData(ByteArrayList.wrap(bytes));
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded convert(@NotNull DynamicOps<T_Encoded> ops) {
		if (this.value instanceof ByteArrayList arrayList) {
			return ops.createByteList(ByteBuffer.wrap(arrayList.elements(), 0, arrayList.size()));
		}
		else {
			return ops.createByteList(ByteBuffer.wrap(this.value.toByteArray()));
		}
	}

	public int size() {
		return this.value.size();
	}

	public byte getByte(int index) {
		return this.value.getByte(index);
	}

	public byte setByte(int index, byte value) {
		return this.value.set(index, value);
	}

	public void append(byte value) {
		this.value.add(value);
	}

	public byte remove(int index) {
		return this.value.removeByte(index);
	}

	@Override
	public boolean equals(Object object) {
		ByteListData byteList;
		return object instanceof Data data && (byteList = data.tryAsByteList()) != null && this.value.equals(byteList.value);
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
		return new ByteListData(new ByteArrayList(this.value));
	}
}