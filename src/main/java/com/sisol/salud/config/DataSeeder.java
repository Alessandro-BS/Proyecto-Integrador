package com.sisol.salud.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.model.enums.Rol;
import com.sisol.salud.repository.MedicoRepository;
import com.sisol.salud.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String emailMedico = "doctor@sisol.com";
        
        if (usuarioRepository.findByEmail(emailMedico).isEmpty()) {
            log.info("Creando cuenta de Médico de prueba...");
            
            // Crear Usuario Médico
            Usuario usuarioMedico = Usuario.builder()
                    .dni("12345678")
                    .nombre("Ana")
                    .apellido("Vazquez")
                    .email(emailMedico)
                    .password(passwordEncoder.encode("123456"))
                    .telefono("987654321")
                    .rol(Rol.MEDICO)
                    .activo(true)
                    .build();
            
            usuarioMedico = usuarioRepository.save(usuarioMedico);
            
            // Crear entidad Medico
            Medico medico = Medico.builder()
                    .usuario(usuarioMedico)
                    .numeroColegiatura("CMP-99999")
                    .build();
                    
            medicoRepository.save(medico);
            log.info("¡Cuenta de Médico creada! Email: {} | Contraseña: {}", emailMedico, "123456");
        } else {
            log.info("La cuenta de Médico {} ya existe. Contraseña: {}", emailMedico, "123456");
        }
    }
}
