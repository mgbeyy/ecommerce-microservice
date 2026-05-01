package com.zaid.authservice.service;

import com.zaid.authservice.dto.request.LoginRequest;
import com.zaid.authservice.dto.request.RegisterRequest;
import com.zaid.authservice.dto.response.AuthResponse;
import com.zaid.authservice.entity.User;
import com.zaid.authservice.entity.enums.ERole;
import com.zaid.authservice.repository.IUserRepository;
import com.zaid.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {
        // E-posta kontrolü (Burada ileride özel bir CustomException fırlatılabilir)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Bu e-posta adresi zaten kullanımda.");
        }

        // Yeni kullanıcı oluşturma (Şifreyi BCrypt ile hashleyerek kaydediyoruz)
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(ERole.USER) // Varsayılan rol
                .build();

        userRepository.save(user);

        // Kayıt sonrası otomatik login yapmak için token üretimi
        return generateAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Kullanıcıyı bul
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        // Şifre doğrulama (Düz metin şifre ile DB'deki hashli şifreyi karşılaştırır)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Hatalı şifre.");
        }

        // Başarılıysa token üret
        return generateAuthResponse(user);
    }

    // Token üretim mantığını tek bir yerde toplamak (DRY Prensibi)
    private AuthResponse generateAuthResponse(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name()); // Rolü String olarak JWT'ye ekliyoruz

        String token = jwtService.generateToken(extraClaims, user.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .build();
    }
}