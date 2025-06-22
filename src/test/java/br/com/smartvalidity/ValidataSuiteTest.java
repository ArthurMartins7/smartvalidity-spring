package br.com.smartvalidity;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.springframework.test.context.ActiveProfiles;

import br.com.smartvalidity.java.service.CategoriaServiceTest;
import br.com.smartvalidity.java.service.CorredorServiceTest;
import br.com.smartvalidity.java.service.ItemProdutoServiceTest;
import br.com.smartvalidity.java.service.ProdutoServiceTest;
import br.com.smartvalidity.java.service.UsuarioServiceTest;

@Suite
@SelectClasses({
    SmartvalidityApplicationTests.class,
    CategoriaServiceTest.class,
    CorredorServiceTest.class,
    ItemProdutoServiceTest.class,
    ProdutoServiceTest.class,
    UsuarioServiceTest.class
})
@ActiveProfiles("test")
public class ValidataSuiteTest {
    
} 