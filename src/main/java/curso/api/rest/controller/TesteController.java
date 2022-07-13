package curso.api.rest.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;

@RestController
@RequestMapping(value = "/usuario")
public class TesteController {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@GetMapping(value = "v1/{id}", produces = "application/json")
	@CacheEvict(value = "cacheusers", allEntries = true)
	@CachePut("cacheusers")

	public ResponseEntity<Usuario> buscaPorId(@PathVariable(value = "id") Integer id) {

		Optional<Usuario> usuario = usuarioRepository.findById(id);

		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}

	@GetMapping(value = "v2/{id}", produces = "application/json")
	@CacheEvict(value = "cacheusers", allEntries = true)
	@CachePut("cacheusers")

	public ResponseEntity<Usuario> buscaPorId2(@PathVariable(value = "id") Integer id) {

		Optional<Usuario> usuario = usuarioRepository.findById(id);

		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}

	@GetMapping(value = "/", produces = "application/json")
	@CacheEvict(value = "cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<List<Usuario>> buscaTodos() throws InterruptedException {

		List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();

		return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
	}

	@PostMapping(value = "/salvar")
	@ResponseBody

	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) {

		for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}

		// AQUI ESTÁ CONFIGURANDO PARA UTILIZAR A SENHA CADASTRADA NO DB
		String senhaCriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhaCriptografada);
		Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);

	}

	@DeleteMapping(value = "/deletar")
	@ResponseBody
	public ResponseEntity<String> deletar(@RequestParam Integer id) {
		usuarioRepository.deleteById(id);

		return new ResponseEntity<>("Usuário deletado com suceso", HttpStatus.OK);

	}

	@PutMapping(value = "/editar")
	@ResponseBody
	public ResponseEntity<?> editar(@RequestBody Usuario usuario) {

		for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}

		Usuario usertemp = usuarioRepository.findUserByLogin(usuario.getLogin());

		if (usuario.getId() == null) {
			return new ResponseEntity<String>("ID do usuário não Foi encontrado para atualização", HttpStatus.OK);

		}
		// CONDIÇÃO PARA VALIDAR SENHA DO USUÁRIO
		if (!usertemp.getSenha().equals(usuario.getSenha())) {
			String senhaCriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
			usuario.setSenha(senhaCriptografada);
		}

		Usuario usuarioEdita = usuarioRepository.save(usuario);

		return new ResponseEntity<Usuario>(usuarioEdita, HttpStatus.OK);
	}

}
