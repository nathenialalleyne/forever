package dev.forever.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noCodeUnits;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.EvaluationResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	/*
	 * PROVEN TO BITE, not merely observed to pass.
	 *
	 * A rule that has only ever been seen green proves nothing: it may be matching no
	 * classes, or asserting something trivially true. This suite has twice shipped a
	 * check that could not fail, so each rule below was verified by writing a
	 * deliberately violating class, watching the named rule FAIL, then deleting it.
	 *
	 *   DOMAIN_MUST_NOT_DEPEND_ON_MINECRAFT        domain class importing ItemStack
	 *   DOMAIN_MUST_NOT_DEPEND_ON_OUTER_LAYERS     domain class importing an adapter type
	 *   APPLICATION_MUST_NOT_DEPEND_ON_ADAPTER     application class importing WarehouseController
	 *   MATCHA_INTERNALS_MUST_NOT_LEAK_OUTSIDE_ADAPTER  core class importing MatchaServerEvidence
	 *   PRODUCTION_CODE_MUST_AVOID_FORBIDDEN_JAVA_HYGIENE  production class writing to a
	 *                                                      standard stream directly
	 *
	 * Worth recording from that exercise: most Matcha internals are package-private, so
	 * the COMPILER rejects a leak before ArchUnit sees it. The rule still earns its place
	 * because it catches the types that must be public.
	 *
	 * Not yet proven by injection, and honest about it:
	 *   CORE_FEATURE_SLICES_MUST_BE_FREE_OF_CYCLES     needs a two-feature cycle to trigger
	 *   COMMON_CODE_MUST_NOT_DEPEND_ON_CLIENT_MINECRAFT  needs a common class importing a
	 *                                                    client-only Minecraft type
	 *   ADAPTER_CLASSES_SHOULD_ACTUALLY_TOUCH_MINECRAFT  advisory only, reports and passes
	 */

	private static final Logger LOGGER = LoggerFactory.getLogger(ArchitectureRulesTest.class);
	private static final JavaClasses CLASSES = new ClassFileImporter()
			.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
			.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
			.importPackages("dev.forever");

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

	/*
	 * The layering rules above only catch dependencies pointing the WRONG way. They
	 * cannot see a pure class parked in `adapter`, which is the more common migration
	 * mistake: calling something an adapter is always "safe", so defaulting there
	 * silently rebuilds the undifferentiated pile this layout exists to break up.
	 *
	 * This rule attacks that blind spot from the other side, but deliberately only for
	 * TOP-LEVEL classes. Nested types were originally included and that was a design
	 * error: a private `record Raw(...)` inside a SavedData class, or a nested result
	 * type, is an implementation detail of its enclosing adapter and cannot move
	 * independently. Flagging them produced pressure to allowlist correct code, which
	 * teaches contributors that the rule is noise.
	 *
	 * It is a WARNING-style advisory rather than a hard gate, evaluated as a reported
	 * rule below, because the honest answer for a registration entrypoint or a pure
	 * result type is a judgement call a human should make during review, not a build
	 * break during an unrelated change.
	 */
	private static final DescribedPredicate<JavaClass> TOP_LEVEL_ADAPTER_TYPES =
			new DescribedPredicate<>("top-level adapter classes") {
				@Override
				public boolean test(JavaClass input) {
					return input.getPackageName().contains(".adapter")
							&& !input.getName().contains("$");
				}
			};

	static final ArchRule ADAPTER_CLASSES_SHOULD_ACTUALLY_TOUCH_MINECRAFT = classes()
			.that(TOP_LEVEL_ADAPTER_TYPES)
			.should(new ArchCondition<JavaClass>("depend on a Minecraft or Fabric type") {
				@Override
				public void check(JavaClass item, ConditionEvents events) {
					boolean touchesGame = item.getDirectDependenciesFromSelf().stream()
							.map(dependency -> dependency.getTargetClass().getPackageName())
							.anyMatch(name -> name.startsWith("net.minecraft") || name.startsWith("net.fabricmc"));
					if (!touchesGame) {
						events.add(SimpleConditionEvent.violated(item, item.getFullName()
								+ " sits in an adapter package but depends on no Minecraft or Fabric type. "
								+ "Consider moving it to domain or application."));
					}
				}
			})
			.allowEmptyShould(true)
			.because("an adapter package should hold genuine Minecraft integration, not pure logic parked at the boundary");

	/*
	 * Reported, never enforced. Whether a Minecraft-free class belongs in `adapter` is a
	 * judgement call: a registration entrypoint, a pure result type, or a codec holder can
	 * each be legitimate at the boundary. Failing the build on that judgement produced
	 * pressure to allowlist correct code, and an allowlist full of correct code teaches
	 * the next contributor that the rule is noise. So this prints guidance for review and
	 * lets a human decide. The hard layering gates above remain strict.
	 */
	@Test
	void reportAdapterClassesThatTouchNoMinecraftType() {
		EvaluationResult result = ADAPTER_CLASSES_SHOULD_ACTUALLY_TOUCH_MINECRAFT.evaluate(CLASSES);
		List<String> messages = result.getFailureReport().getDetails();
		if (!messages.isEmpty()) {
			LOGGER.info("Adapter classes worth reviewing ({}). Each may be fine; see docs/code-guidelines.md:", messages.size());
			messages.stream().sorted().forEach(message -> LOGGER.info("  {}", message));
		}
	}

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
