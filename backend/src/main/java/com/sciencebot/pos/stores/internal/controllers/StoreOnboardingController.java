package com.sciencebot.pos.stores.internal.controllers;

import com.sciencebot.pos.stores.CreateStoreCommand;
import com.sciencebot.pos.stores.StoreDocumentDto;
import com.sciencebot.pos.stores.StoreDto;
import com.sciencebot.pos.stores.StoreFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@Tag(name = "Onboarding de Locales", description = "Auto-registro del local por su propio ADMINISTRATOR")
public class StoreOnboardingController {

    private final StoreFacade storeFacade;

    public StoreOnboardingController(StoreFacade storeFacade) {
        this.storeFacade = storeFacade;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Registrar mi local", description = "Crea el local del ADMINISTRATOR autenticado (que aun no tiene uno). Queda en PENDING_VERIFICATION.")
    public ResponseEntity<StoreDto> registerOwnStore(@RequestBody CreateStoreCommand command, Authentication authentication) {
        StoreDto created = storeFacade.registerOwnStore(authentication.getName(), command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Obtener mi local", description = "Retorna el local del ADMINISTRATOR autenticado.")
    public ResponseEntity<StoreDto> getOwnStore(Authentication authentication) {
        return ResponseEntity.ok(storeFacade.getOwnStore(authentication.getName()));
    }

    @GetMapping("/me/documents")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Listar mis documentos legales", description = "Documentos KYC subidos por el local del ADMINISTRATOR autenticado.")
    public ResponseEntity<List<StoreDocumentDto>> getOwnDocuments(Authentication authentication) {
        return ResponseEntity.ok(storeFacade.getOwnDocuments(authentication.getName()));
    }

    @PostMapping(value = "/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Subir un documento legal", description = "Sube un nuevo documento KYC (RUT, Cámara de Comercio, cédula, certificación bancaria u otro) para el local del ADMINISTRATOR autenticado. Queda en PENDING hasta que el backoffice lo revise.")
    public ResponseEntity<StoreDocumentDto> uploadOwnDocument(
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        StoreDocumentDto uploaded = storeFacade.uploadOwnDocument(authentication.getName(), documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }
}
