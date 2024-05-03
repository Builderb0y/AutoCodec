package builderb0y.autocodec.decoders;

import java.util.List;

import org.junit.Test;

import builderb0y.autocodec.annotations.Intern;
import builderb0y.autocodec.annotations.MultiLine;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectOps;

import static org.junit.Assert.*;

@SuppressWarnings("StringOperationCanBeSimplified")
public class InternedStringDecoderTest {

	@Test
	public void test() throws DecodeException {
		String expected = "test";
		String from = new String(expected);
		String to = TestCommon.DEFAULT_CODEC.decode(
			TestCommon.DEFAULT_CODEC.createDecoder(
				new ReifiedType<@Intern String>() {}
			),
			from,
			ObjectOps.INSTANCE
		);
		assertNotSame(expected, from);
		assertSame(expected, to);
	}

	@Test
	public void testMultiLine() throws DecodeException {
		String expected = ("hello" + System.lineSeparator() + "world").intern();
		List<String> from = List.of(new String("hello"), new String("world"));
		String to = TestCommon.DEFAULT_CODEC.decode(
			TestCommon.DEFAULT_CODEC.createDecoder(
				new ReifiedType<@Intern @MultiLine String>() {}
			),
			from,
			ObjectOps.INSTANCE
		);
		assertSame(expected, to);
	}
}