package org.hightea;

import org.drools.compiler.CommonTestMethodBase;
import org.drools.compiler.SecondClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;


/**
 * This test demonstrates an issue whith path memory and container upgrade (NPE  accessiong segment memories in RuleNetworkEvaluator)
 * Note : if the packages are the same, there is no issue
 */
public class IncrementalRemoveRuleTest extends CommonTestMethodBase {


	private static final String DRL_PACKAGE_A = "package org.hightea.a\n" +
			"\n" +
			"import org.drools.compiler.Message\n" +
			"import org.drools.compiler.FirstClass\n" +
			"\n" +
			"rule \"RG_1\"\n" +
			"    when\n" +
			"        $event : Message()\n" +
			"        FirstClass(item1 == $event.message1)\n" +
			"    then\n" +
			"        System.out.println(\"RG_1\");" +
			"end\n";

	private static final String DRL_PACKAGE_B = "package org.hightea.b\n" +
			"\n" +
			"import org.drools.compiler.Message\n" +
			"import org.drools.compiler.SecondClass\n" +
			"\n" +
			"rule \"RG_2\"\n" +
			"    when\n" +
			"        $event: Message()\n" +
			"        SecondClass(item1 == $event.message1)\n" +
			"    then\n" +
			"        System.out.println(\"RG_2\");" +
			"end" +
			"\n" ;




	@Test
	public void testNpeInRuleNetworkEvaluator() throws Exception {
		KieServices ks = KieServices.Factory.get();
		ReleaseId releaseId = ks.newReleaseId( "org.hightea", "test-rule", "1" );

		createAndDeployJar(ks, releaseId, DRL_PACKAGE_B, DRL_PACKAGE_A);  // ORDER of declared DRL with different package names
		KieContainer kieContainer = ks.newKieContainer(releaseId);
		KieSession kieSession = kieContainer.newKieSession();

		kieSession.insert(new SecondClass());
		kieSession.fireAllRules();

		ReleaseId releaseId2 = ks.newReleaseId( "org.hightea", "test-rule", "2" );
		createAndDeployJar( ks, releaseId2, DRL_PACKAGE_A);
		kieContainer.updateToVersion(releaseId2);


		kieSession.fireAllRules(); // NPE in RuleNetworkEvaluator.evaluateNetwork(RuleNetworkEvaluator.java:114

	}

	@Test
	public void testOK() throws Exception {

		KieServices ks = KieServices.Factory.get();
		ReleaseId releaseId = ks.newReleaseId( "org.hightea", "test-rule", "1" );

		createAndDeployJar(ks, releaseId, DRL_PACKAGE_A, DRL_PACKAGE_B);  // ORDER of declared DRL with different package names
		KieContainer kieContainer = ks.newKieContainer(releaseId);
		KieSession kieSession = kieContainer.newKieSession();

		kieSession.insert(new SecondClass());
		kieSession.fireAllRules();

		ReleaseId releaseId2 = ks.newReleaseId( "org.hightea", "test-rule", "2" );
		createAndDeployJar( ks, releaseId2, DRL_PACKAGE_A);
		kieContainer.updateToVersion(releaseId2);


		kieSession.fireAllRules();//OK

	}

}