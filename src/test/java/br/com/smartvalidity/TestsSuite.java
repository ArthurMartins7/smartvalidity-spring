package br.com.smartvalidity;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.springframework.test.context.ActiveProfiles;

@Suite
@SuiteDisplayName("SmartValidity - Suite Completa de Testes")
@SelectPackages({
    "br.com.smartvalidity",           // Inclui SmartvalidityApplicationTests
    "br.com.smartvalidity.java.service"  // Inclui todos os *ServiceTest
})
@ActiveProfiles("test")
public class TestsSuite {

} 