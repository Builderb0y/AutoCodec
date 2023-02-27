package builderb0y.autocodec.coders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.regex.Pattern;

import org.junit.Test;

import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;

public class CodableAsStringTest {

	@Test
	public void test() throws DecodeException {
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, BigInteger.class).test(BigInteger.ONE.shiftLeft(100));
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, BigDecimal.class).test(BigDecimal.ONE.movePointRight(100));
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Instant.class).test(Instant.now());
		CoderUnitTester<Pattern> patternTester = new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Pattern.class);
		patternTester.equality = (p1, p2) -> p1.pattern().equals(p2.pattern()) && p1.flags() == p2.flags();
		patternTester.test(Pattern.compile("[A-Z][a-z]*"));
		patternTester.test(Pattern.compile(")hi(", Pattern.LITERAL));
		patternTester.test(Pattern.compile(")hi(", Pattern.LITERAL | Pattern.COMMENTS));
	}
}