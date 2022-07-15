package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {

	// TEMPO DE VALIDADE DO TOKEN PARA 2 DIAS//

	private static final Integer EXPIRATION_TIME = 172800000;

	// UMA SENHA UNICA PARA COMPOR A AUTENTICAÇÃO E AJUDAR NA SEGURANÇA
	private static final String SECRET = "SenhaExtremamenteSecreta";

	// PREFIXO PADRÃO DO TOKEN
	private static final String TOKEN_PREFIX = "Bearer";

	private static final String HEADER_STRING = "Authorization";

	// GERANDO TOKEN DE AUTENTICAÇÃO E ADICIONANDO AO CABEÇALHO E RESPOSTA HTTP

	public void addAuthentication(HttpServletResponse response, String username) throws IOException {

		// MONTAGEM DO TOKEN

		String JWT = Jwts.builder() // CHAMA O GERADOR DE TOKEN
				.setSubject(username) // ADICIONA O USUÁRIO
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // TEMPO DE EXPIRAÇÃO DO TOKEN
				.signWith(SignatureAlgorithm.HS512, SECRET).compact(); // COMPACTAÇÃO E ALGORITMOS DE GERAÇÃO DE SENHAS

		// JUBTA O TOKEN COM O PREFIXO
		String token = TOKEN_PREFIX + " " + JWT;

		// ADICIONA NO CABEÇALHO HTTP
		response.addHeader(HEADER_STRING, token);

		// ESCREVE TOKEN COMO RESPOSTA NO CORPO HTTP
		response.getWriter().write("{\"Authorization\": \"" + token + "\"}");

	}

	// RETORNA O USUÁRIO VALIDADO COM TOKEN//
	// SE NÃO FOR VÁLIDO, RETORNA NULL//

	public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {

		// PEGA O TOKEN ENVIADO NO CABEÇALHO HTTP

		String token = request.getHeader(HEADER_STRING);

		if (token != null) {

			String tokenLimpo = token.replace(TOKEN_PREFIX, "").trim();

			// FAZ A VALIDAÇÃO DO TOKEN DO USUÁRIO NA REQUISIÇÃO

			String user = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(tokenLimpo).getBody().getSubject();

			if (user != null) {

				Usuario usuario = ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
						.findUserByLogin(user);

				if (usuario != null) {

					if (tokenLimpo.equalsIgnoreCase(usuario.getToken())) {

						return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(),
								usuario.getAuthorities());
					}
				}

			}
		}
		return null;
	}

}
