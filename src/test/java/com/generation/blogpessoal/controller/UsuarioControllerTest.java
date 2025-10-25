package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.model.UsuarioLogin;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;
import com.generation.blogpessoal.util.JwtHelper;
import com.generation.blogpessoal.util.TestBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.DisplayName.class)
class UsuarioControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private UsuarioRepository usuarioRepository;

	private static final String BASE_URL = "/usuarios";
	private static final String ADMIN = "root@root.com";
	private static final String SENHA = "rootroot";

	@BeforeAll
	void start() {
		usuarioRepository.deleteAll();
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Root", ADMIN, SENHA));
	}

	@Test
	@DisplayName("✔ 01 - Deve cadastrar um novo usuário com sucesso")
	void deveCadastrarUsuario() {
		
		// Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Paulo Antunes", "paulo_antunes@email.com.br", "12345678");
		
		// When
		HttpEntity<Usuario> requisicao = new HttpEntity<>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}

	@Test
	@DisplayName("✔ 02 - Não deve permitir a duplicação do usuário")
	void naoDeveDuplicarUsuario() {
		
		// Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Maria da Silva", "maria_silva@email.com.br", "12345678");
		usuarioService.cadastrarUsuario(usuario);
		
		// When
		HttpEntity<Usuario> requisicao = new HttpEntity<>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL + "/cadastrar", HttpMethod.POST, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
	}

	@Test
	@DisplayName("✔ 03 - Deve atualizar os dados de um usuário com sucesso")
	void deveAtualizarUmUsuario() {
		
		// Given
		Usuario usuario = TestBuilder.criarUsuario(null, "Juliana Andrews", "ju_andrews@email.com.br", "12345678");
		Optional<Usuario> cadastrado = usuarioService.cadastrarUsuario(usuario);
		
		Usuario usuarioUpdate = TestBuilder.criarUsuario(cadastrado.get().getId(), "Juliana Ramos", 
				"ju_ramos@email.com.br", "12345678");
		
		// When
		String token = JwtHelper.obterToken(testRestTemplate, ADMIN, SENHA);
		HttpEntity<Usuario> requisicao = JwtHelper.criarRequisicaoComToken(usuarioUpdate, token);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL + "/atualizar", HttpMethod.PUT, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}

	@Test
	@DisplayName("✔ 04 - Deve listar todos os usuários com sucesso")
	void deveListarTodosUsuarios() {
		
		// Given
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Ana Marques", 
				"ana_marques@email.com.br", "12345678"));
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Carlos Moura", 
				"carlos_moura@email.com.br", "12345678"));
		
		// When
		String token = JwtHelper.obterToken(testRestTemplate, ADMIN, SENHA);
		HttpEntity<Void> requisicao = JwtHelper.criarRequisicaoComToken(token);
		ResponseEntity<Usuario[]> resposta = testRestTemplate.exchange(
				BASE_URL + "/all", HttpMethod.GET, requisicao, Usuario[].class);
		
		// Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}
	
	@Test
	@DisplayName("✔ 05 - Deve listar um usuário específico pelo id")
	void deveListarUsuarioPorId() {
		
		//Given
		Usuario usuario = TestBuilder.criarUsuario(null,  "Fernanda Lima", "fernandalima@email.com.br", "12345678");
		Optional<Usuario> cadastrado = usuarioService.cadastrarUsuario(usuario);
		
		// When
		String token = JwtHelper.obterToken(testRestTemplate, ADMIN, SENHA);
		HttpEntity<Void> requisicao = JwtHelper.criarRequisicaoComToken(token);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(
				BASE_URL + "/" + cadastrado.get().getId(), HttpMethod.GET, requisicao, Usuario.class);
		
		// Then
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
		assertEquals("Fernanda Lima", resposta.getBody().getNome());
		assertEquals("fernandalima@email.com.br", resposta.getBody().getUsuario());
		
	 }
	
	@Test
	@DisplayName("✔ 06 - Deve autenticar um usuário com sucesso")
	void deveAutenticarUsuario() {

	    // Given
	    Usuario usuario = TestBuilder.criarUsuario(null, "Ana Maria", "anamaria@email.com.br", "12345678");
	    usuarioService.cadastrarUsuario(usuario);

	    // When
	    UsuarioLogin login = new UsuarioLogin();
	    login.setUsuario("anamaria@email.com.br");
	    login.setSenha("12345678");

	    HttpEntity<UsuarioLogin> requisicao = new HttpEntity<>(login);
	    ResponseEntity<UsuarioLogin> resposta = testRestTemplate.exchange(
	            BASE_URL + "/logar", HttpMethod.POST, requisicao, UsuarioLogin.class);

	    // Then
	    assertEquals(HttpStatus.OK, resposta.getStatusCode());
	    assertNotNull(resposta.getBody());
	    assertNotNull(resposta.getBody().getToken());
	    assertEquals("anamaria@email.com.br", resposta.getBody().getUsuario());
	}


}

