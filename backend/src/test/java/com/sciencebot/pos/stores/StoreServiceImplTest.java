package com.sciencebot.pos.stores;

import com.sciencebot.pos.stores.internal.entities.StoreDocumentEntity;
import com.sciencebot.pos.stores.internal.entities.StoreEntity;
import com.sciencebot.pos.stores.internal.mappers.StoreMapper;
import com.sciencebot.pos.stores.internal.repositories.StoreCategoryRepository;
import com.sciencebot.pos.stores.internal.repositories.StoreDocumentRepository;
import com.sciencebot.pos.stores.internal.repositories.StoreRepository;
import com.sciencebot.pos.stores.internal.services.StoreServiceImpl;
import com.sciencebot.pos.users.UserDto;
import com.sciencebot.pos.users.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StoreServiceImplTest {

    @Mock private StoreRepository storeRepository;
    @Mock private StoreCategoryRepository categoryRepository;
    @Mock private StoreDocumentRepository documentRepository;
    @Mock private StoreMapper storeMapper;
    @Mock private UserFacade userFacade;
    @InjectMocks private StoreServiceImpl storeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMetrics_ReturnsAggregatedMetrics() {
        when(storeRepository.count()).thenReturn(10L);
        when(storeRepository.countByStatus("ACTIVE")).thenReturn(7L);
        when(storeRepository.countByStatus("INACTIVE")).thenReturn(1L);
        when(storeRepository.countByStatus("PENDING_VERIFICATION")).thenReturn(1L);
        when(storeRepository.countByStatus("SUSPENDED")).thenReturn(1L);
        when(storeRepository.countByEmailVerifiedTrue()).thenReturn(6L);

        StoreMetricsDto metrics = storeService.getMetrics();

        assertNotNull(metrics);
        assertEquals(10L, metrics.totalStores());
        assertEquals(7L, metrics.activeStores());
        assertEquals(1L, metrics.inactiveStores());
        assertEquals(1L, metrics.pendingVerification());
        assertEquals(1L, metrics.suspended());
        assertEquals(6L, metrics.emailVerified());
    }

    @Test
    void createStore_Success() {
        CreateStoreCommand command = new CreateStoreCommand("Tienda 1", 1L, "3001234567", "tienda1@test.com", "https://tienda1.com", "Calle 1", "900123");
        when(storeRepository.existsByEmail("tienda1@test.com")).thenReturn(false);

        StoreEntity saved = new StoreEntity();
        saved.setId(1L);
        saved.setName("Tienda 1");
        saved.setEmail("tienda1@test.com");
        when(storeRepository.save(any(StoreEntity.class))).thenReturn(saved);

        StoreDto dto = new StoreDto(1L, "Tienda 1", 1L, "Retail", "3001234567", "tienda1@test.com", "https://tienda1.com", "Calle 1", "900123", "PENDING_VERIFICATION", false, null, null);
        when(storeMapper.toDto(any(), any())).thenReturn(dto);

        StoreDto result = storeService.createStore(command);
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Tienda 1", result.name());
        verify(storeRepository, times(1)).save(any(StoreEntity.class));
    }

    @Test
    void changeStatus_ValidStatus_Success() {
        StoreEntity entity = new StoreEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_VERIFICATION");

        when(storeRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(storeRepository.save(any())).thenReturn(entity);
        when(storeMapper.toDto(any(), any())).thenReturn(new StoreDto(1L, "Tienda", null, null, null, "t@test.com", null, null, null, "ACTIVE", false, null, null));

        StoreDto result = storeService.changeStatus(1L, "ACTIVE");
        assertNotNull(result);
        assertEquals("ACTIVE", result.status());
    }

    @Test
    void changeStatus_InvalidStatus_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storeService.changeStatus(1L, "INVALID"));
        assertTrue(ex.getMessage().contains("Estado invalido"));
    }

    @Test
    void registerOwnStore_Success_AssignsStoreToOwner() {
        CreateStoreCommand command = new CreateStoreCommand("Tienda 1", 1L, "3001234567", "tienda1@test.com", "https://tienda1.com", "Calle 1", "900123");
        UserDto owner = new UserDto(5L, "Carlos", "carlos", "carlos@test.com", "ADMINISTRATOR", true, null, true);
        when(userFacade.findByUsername("carlos")).thenReturn(Optional.of(owner));
        when(storeRepository.existsByEmail("tienda1@test.com")).thenReturn(false);

        StoreEntity saved = new StoreEntity();
        saved.setId(9L);
        saved.setName("Tienda 1");
        when(storeRepository.save(any(StoreEntity.class))).thenReturn(saved);
        when(storeMapper.toDto(any(), any())).thenReturn(
                new StoreDto(9L, "Tienda 1", 1L, "Retail", "3001234567", "tienda1@test.com", "https://tienda1.com", "Calle 1", "900123", "PENDING_VERIFICATION", false, null, null));

        StoreDto result = storeService.registerOwnStore("carlos", command);

        assertNotNull(result);
        assertEquals(9L, result.id());
        verify(userFacade, times(1)).assignStore(5L, 9L);
    }

    @Test
    void registerOwnStore_OwnerAlreadyHasStore_ThrowsException() {
        CreateStoreCommand command = new CreateStoreCommand("Tienda 1", 1L, "3001234567", "tienda1@test.com", "https://tienda1.com", "Calle 1", "900123");
        UserDto owner = new UserDto(5L, "Carlos", "carlos", "carlos@test.com", "ADMINISTRATOR", true, 2L, true);
        when(userFacade.findByUsername("carlos")).thenReturn(Optional.of(owner));

        assertThrows(IllegalArgumentException.class, () -> storeService.registerOwnStore("carlos", command));
        verify(storeRepository, never()).save(any());
    }

    @Test
    void registerOwnStore_NotAdministrator_ThrowsAccessDenied() {
        CreateStoreCommand command = new CreateStoreCommand("Tienda 1", 1L, "3001234567", "tienda1@test.com", "https://tienda1.com", "Calle 1", "900123");
        UserDto owner = new UserDto(5L, "Sofia", "sofia", "sofia@test.com", "SELLER", true, 1L, true);
        when(userFacade.findByUsername("sofia")).thenReturn(Optional.of(owner));

        assertThrows(AccessDeniedException.class, () -> storeService.registerOwnStore("sofia", command));
    }

    @Test
    void rejectStore_WithReason_Success() {
        StoreEntity entity = new StoreEntity();
        entity.setId(1L);
        entity.setStatus("PENDING_VERIFICATION");

        when(storeRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(storeRepository.save(any())).thenReturn(entity);
        when(storeMapper.toDto(any(), any())).thenReturn(
                new StoreDto(1L, "Tienda", null, null, null, "t@test.com", null, null, null, "REJECTED", false, "Documentos incompletos", null));

        StoreDto result = storeService.rejectStore(1L, "Documentos incompletos");

        assertNotNull(result);
        assertEquals("REJECTED", result.status());
        assertEquals("Documentos incompletos", result.rejectionReason());
    }

    @Test
    void rejectStore_WithoutReason_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> storeService.rejectStore(1L, " "));
        assertTrue(ex.getMessage().contains("motivo"));
    }

    @Test
    void reviewDocument_Approved_Success() {
        StoreDocumentEntity doc = new StoreDocumentEntity();
        doc.setId(10L);
        doc.setStoreId(1L);
        doc.setStatus("PENDING");

        when(documentRepository.findById(10L)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any())).thenReturn(doc);
        when(storeMapper.toDocumentDto(any())).thenReturn(new StoreDocumentDto(10L, 1L, "RUT", "http://doc.pdf", "APPROVED", null, null, null));

        StoreDocumentDto result = storeService.reviewDocument(1L, 10L, new ReviewDocumentCommand("APPROVED", null));
        assertNotNull(result);
        assertEquals("APPROVED", result.status());
    }
}