package br.com.leonardovinicius.todolist.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import at.favre.lib.crypto.bcrypt.BCrypt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository userRepository;
    
    @PostMapping("/")  
    public ResponseEntity create(@RequestBody UserModel userModel) {
        var user = this.userRepository.findByemail(userModel.getEmail());

        if (user != null) {
            System.out.println("Usuário já existente");
            //Mensagem de erro
            //Status code
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário já existe");
        }
        var passwordHashred = BCrypt.withDefaults().hashToString(12, userModel.getPassword().toCharArray());

        userModel.setPassword(passwordHashred);
        var userCreated = this.userRepository.save(userModel);

        return ResponseEntity.status(HttpStatus.OK).body(userCreated);

    }

    @GetMapping("/")
    public ResponseEntity<List<UserModel>> list() {
        List<UserModel> users = this.userRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody UserModel userModel) {
        var user = this.userRepository.findByemail(userModel.getEmail());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não encontrado");
        }

        var passwordVerify = BCrypt.verifyer().verify(
            userModel.getPassword().toCharArray(),
            user.getPassword()
        );

        if (!passwordVerify.verified) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha inválida");
        }

        // Gera o token JWT
        String token = Jwts.builder()
            .setSubject(user.getId().toString())  // ID do usuário como "sub"
            .claim("email", user.getEmail())      // Adiciona informações adicionais (claims)
            .claim("name", user.getName())
            .compact();

        // Retorna o token e os dados do usuário
        return ResponseEntity.ok(Map.of(
            "token", token,
            "user", user
        ));
    }

}
