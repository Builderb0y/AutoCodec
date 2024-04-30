package builderb0y.autocodec.util;

import java.util.function.Supplier;

import com.mojang.serialization.DataResult;
import org.junit.Test;

import static org.junit.Assert.*;

public class CompatibilityTest {

	@Test
	public void testHandles() throws Throwable {
		assertEquals("old", ((DataResult<?>)(DFUVersions.createDataResultErrorHandle(CompatibilityTest.class, "oldMethod").invokeExact((Supplier<String>)() -> "shouldBeIgnored"))).error().orElseThrow().message());
		assertEquals("new", ((DataResult<?>)(DFUVersions.createDataResultErrorHandle(CompatibilityTest.class, "newMethod").invokeExact((Supplier<String>)() -> "shouldBeIgnored"))).error().orElseThrow().message());
	}

	public static <R> DataResult<R> oldMethod(String ignored) {
		return DataResult.error(() -> "old");
	}

	public static <R> DataResult<R> newMethod(Supplier<String> ignored) {
		return DataResult.error(() -> "new");
	}
}