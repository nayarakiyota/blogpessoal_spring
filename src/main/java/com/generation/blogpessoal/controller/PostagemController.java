package com.generation.blogpessoal.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.generation.blogpessoal.model.Postagem;
import com.generation.blogpessoal.repository.PostagemRepository;
import com.generation.blogpessoal.repository.TemaRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/postagens")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PostagemController {
	
	// Injeção de dependência do repositório
	@Autowired
	private PostagemRepository postagemRepository;
	
	// Injeção de dependência do repositório Tema 
	@Autowired
	private TemaRepository temaRepository;
	
	// Método para listar todas as postagens
	@GetMapping
	public ResponseEntity<List<Postagem>> getAll() {
		return ResponseEntity.ok(postagemRepository.findAll());
	}
	
	// Método para buscar uma postagem por ID
	@GetMapping("/{id}")
	public ResponseEntity<Postagem> getById(@PathVariable Long id){
		return postagemRepository.findById(id)
				.map(resposta -> ResponseEntity.ok(resposta))
				.orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}
	
	// Método para buscar postagens por título
	@GetMapping("/titulo/{titulo}")
	public ResponseEntity<List<Postagem>> getAllByTitulo(@PathVariable String titulo){
		return ResponseEntity.ok(postagemRepository.findAllByTituloContainingIgnoreCase(titulo));
	}
	
	// Método para criar uma nova postagem
	@PostMapping
	public ResponseEntity<Postagem> post(@Valid @RequestBody Postagem postagem){
		
		// Verifica se o tema associado à postagem existe
		if (temaRepository.existsById(postagem.getTema().getId())) {
				
			postagem.setId(null);
		
			return ResponseEntity.status(HttpStatus.CREATED).body(postagemRepository.save(postagem));
	}
	
	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tema inexistente!", null);

}

	// Método para atualizar uma postagem existente
	@PutMapping
	public ResponseEntity<Postagem> put(@Valid @RequestBody Postagem postagem){
		
		if (postagemRepository.existsById(postagem.getId())) {

			if (temaRepository.existsById(postagem.getTema().getId())) {
							
				return ResponseEntity.status(HttpStatus.OK).body(postagemRepository.save(postagem));
			}

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tema inexistente!", null);
		}	
		
		return ResponseEntity.notFound().build();
		
	}
	
	// Método para deletar uma postagem por ID
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		
		Optional<Postagem> postagem = postagemRepository.findById(id);
		
		if (postagem.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		
		postagemRepository.deleteById(id);
	}
	
}
