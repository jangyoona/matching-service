package com.boyug.controller;


import com.boyug.security.jwt.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
@AllArgsConstructor
public class AuthenticationController {

//    private final JwtUtil jwtUtil;
//
//    @GetMapping("/api/auth")
//    public ResponseEntity<Boolean> authenticationToken(@RequestHeader("Authorization") String authorizationHeader) {
//
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            String token = authorizationHeader.substring(7); // "Bearer " 제거
//
//            if (jwtUtil.validateToken(token)) {
//                return ResponseEntity.ok(true);
//            }
//        }
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
//    }

}
