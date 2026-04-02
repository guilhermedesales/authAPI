package com.api.auth.API.Controller;

import com.api.auth.Application.DTOs.Sistema.CriarSistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaDTO;
import com.api.auth.Application.DTOs.Sistema.SistemaListDTO;
import com.api.auth.Application.Service.SistemaService;
import com.api.auth.Domain.Entities.Sistema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/sistema")
@Tag(name= "Sistemas")
public class SistemaController {

    private final SistemaService sistemaService;

    public SistemaController(SistemaService sistemaService) {
        this.sistemaService = sistemaService;
    }

    @PostMapping("/criar")
    public SistemaListDTO criar(CriarSistemaDTO dto) {
        log.info("[SISTEMA] Criar sistema - nome={}", dto.getNome());
        SistemaListDTO created = sistemaService.criar(dto);
        log.info("[SISTEMA] Sistema criado - sistemaId={}", created.getId());
        return created;
    }

    @GetMapping("/listar")
    public Page<SistemaListDTO> listar(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size
    ){
        log.debug("[SISTEMA] Listar sistemas - page={} size={}", page, size);
        return sistemaService.listar(page, size);
    }

    @GetMapping("/buscar/{id}")
    public SistemaDTO buscarPorId(@RequestParam UUID id) {
        log.debug("[SISTEMA] Buscar sistema - sistemaId={}", id);
        return sistemaService.buscarPorId(id);
    }

}
