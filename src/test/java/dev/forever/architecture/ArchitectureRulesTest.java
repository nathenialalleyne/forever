package dev.forever.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noCodeUnits;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

/**
 * Executable checks for the package boundaries described in the architecture records.
 *
 * <p>The selectors deliberately refer to package conventions rather than today's class
 * list. The new domain, application, and adapter packages are not populated in every
 * feature yet, so an empty selector passes now and starts enforcing the boundary as
 * soon as a class is moved into one of those packages. A frozen baseline is therefore
 * unnecessary for the layering rules while the tree is still flat.
 */
@AnalyzeClasses(
		packages = "dev.forever",
		importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeArchives.class})
class ArchitectureRulesTest {

	private static final String DOMAIN_PACKAGES = "..domain..";
	private static final String APPLICATION_PACKAGES = "..application..";
	private static final String ADAPTER_PACKAGES = "..adapter..";
	private static final String MATCHA_PACKAGE = "dev.forever.compat.matcha..";
	private static final String MINECRAFT_PACKAGE = "net.minecraft..";
	private static final String CLIENT_MINECRAFT_PACKAGE = "net.minecraft.client..";

	/*
	 * The asset validator is a separate developer-only source set. It deliberately uses
	 * System streams and a checked UsageException for its CLI contract, so including it
	 * here would turn a production-code hygiene rule into a false positive.
	 */
	private static final DescribedPredicate<JavaClass> PRODUCTION_TYPES =
			new DescribedPredicate<>("production classes") {
				@Override
				public boolean test(JavaClass input) {
					return isInPackage(input, "dev.forever")
							&& !isInPackage(input, "dev.forever.tools");
				}
			};

	private static final DescribedPredicate<JavaClass> COMMON_PRODUCTION_TYPES =
			new DescribedPredicate<>("common production classes") {
				@Override
				public boolean test(JavaClass input) {
					return PRODUCTION_TYPES.test(input) && !isInPackage(input, "dev.forever.client");
				}
			};

	/*
	 * ArchUnit exposes exact caught types through TryCatchBlock. Checking those types is
	 * more accurate than banning every dependency on java.lang.Exception, because a
	 * multi-catch variable may legitimately resolve exception.getMessage() to the common
	 * superclass while still catching only specific exception types.
	 */
	private static final ArchCondition<JavaClass> CATCH_GENERIC_EXCEPTION =
			new ArchCondition<>("catch java.lang.Exception or java.lang.Throwable") {
				@Override
				public void check(JavaClass input, ConditionEvents events) {
					input.getTryCatchBlocks().stream()
							.filter(block -> block.getCaughtThrowables().stream()
									.anyMatch(throwable -> throwable.getFullName().equals(Exception.class.getName())
											|| throwable.getFullName().equals(Throwable.class.getName())))
							.forEach(block -> events.add(SimpleConditionEvent.violated(
									block,
									"Class " + input.getFullName() + " catches a generic throwable at "
											+ block.getSourceCodeLocation())));
				}
			};

	/*
	 * ForeverMod is the current common lifecycle caller and must be able to install the
	 * no-op Matcha state. MatchaAdapter and MatchaAdapterStatus are the only stable
	 * adapter-facing types intended for future integration callers. Evidence, detector,
	 * mapping, observation, and profile classes remain implementation details, so naming
	 * this allowlist explicitly prevents those details leaking into core or client code.
	 */
	private static final Set<String> PUBLISHED_MATCHA_TYPES = Set.of(
			"dev.forever.compat.matcha.ForeverMatchaCompat",
			"dev.forever.compat.matcha.MatchaAdapter",
			"dev.forever.compat.matcha.MatchaAdapterStatus");

	private static final DescribedPredicate<JavaClass> MATCHA_INTERNAL_TYPES =
			new DescribedPredicate<>("Matcha implementation types") {
				@Override
				public boolean test(JavaClass input) {
					return isInPackage(input, "dev.forever.compat.matcha")
							&& !PUBLISHED_MATCHA_TYPES.contains(input.getFullName());
				}
			};

	@ArchTest
	static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_MINECRAFT = noClasses()
			.that().resideInAnyPackage(DOMAIN_PACKAGES)
			.should().dependOnClassesThat().resideInAnyPackage(MINECRAFT_PACKAGE)
			.because("domain classes must remain Minecraft-free so they can be unit-tested without a Minecraft runtime");

	@ArchTest
	static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_OUTER_LAYERS = noClasses()
			.that().resideInAnyPackage(DOMAIN_PACKAGES)
			.should().dependOnClassesThat().resideInAnyPackage(APPLICATION_PACKAGES, ADAPTER_PACKAGES)
			.because("domain dependencies must point inward and never reach application or Minecraft adapter code");

	@ArchTest
	static final ArchRule APPLICATION_MUST_NOT_DEPEND_ON_ADAPTER = noClasses()
			.that().resideInAnyPackage(APPLICATION_PACKAGES)
			.should().dependOnClassesThat().resideInAnyPackage(ADAPTER_PACKAGES)
			.because("application services must depend on stable inner contracts rather than Minecraft adapters");

	@ArchTest
	static final ArchRule MATCHA_INTERNALS_MUST_NOT_LEAK_OUTSIDE_ADAPTER = noClasses()
			.that().resideOutsideOfPackage(MATCHA_PACKAGE)
			.should().dependOnClassesThat(MATCHA_INTERNAL_TYPES)
			.because("Matcha implementation details must stay behind the isolated compatibility adapter");

	@ArchTest
	static final ArchRule CORE_FEATURE_SLICES_MUST_BE_FREE_OF_CYCLES = slices()
			.matching("dev.forever.core.(*)..")
			.should().beFreeOfCycles()
			.because("feature slices must remain independently changeable instead of forming dependency cycles");

	/*
	 * The current test classpath contains both Loom common and client outputs, so this
	 * package-based dependency check is meaningful today. ArchUnit does not know Gradle
	 * source-set labels directly, so Loom's split compilation remains the primary
	 * source-set guarantee and this rule checks the resulting class dependencies.
	 */
	@ArchTest
	static final ArchRule COMMON_CODE_MUST_NOT_DEPEND_ON_CLIENT_MINECRAFT = noClasses()
			.that(COMMON_PRODUCTION_TYPES)
			.should().dependOnClassesThat().resideInAnyPackage(CLIENT_MINECRAFT_PACKAGE)
			.because("common/server code must not load client-only Minecraft classes on a dedicated server");

	@ArchTest
	static final ArchRule PRODUCTION_CODE_MUST_AVOID_FORBIDDEN_JAVA_HYGIENE = CompositeArchRule.of(
				noClasses().that(PRODUCTION_TYPES).should().dependOnClassesThat().resideInAnyPackage("java.util.logging..")
						.because("production code must use the project's structured logger rather than java.util.logging"))
				.and(noClasses().that(PRODUCTION_TYPES).should().accessField(System.class, "out")
						.because("production code must not write to standard output because logging needs server-safe context"))
				.and(noClasses().that(PRODUCTION_TYPES).should().accessField(System.class, "err")
						.because("production code must not write to standard error because logging needs server-safe context"))
				.and(noCodeUnits().that().areDeclaredInClassesThat(PRODUCTION_TYPES)
						.should().declareThrowableOfType(Exception.class)
						.because("production code must not declare the generic Exception type in a throws clause"))
				.and(noClasses().that(PRODUCTION_TYPES).should(CATCH_GENERIC_EXCEPTION)
						.because("production code must catch a specific exception type rather than the generic Exception type"))
				.because("AGENTS.md forbids java.util.logging, direct standard streams, and broad generic Exception usage "
						+ "in main code");

	private static boolean isInPackage(JavaClass javaClass, String packageName) {
		return javaClass.getPackageName().equals(packageName)
				|| javaClass.getPackageName().startsWith(packageName + ".");
	}
}
