package com.sciencebot.pos.stores;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StoreFacade {
    StoreMetricsDto getMetrics();
    Page<StoreDto> searchStores(String status, String q, Pageable pageable);
    StoreDto createStore(CreateStoreCommand command);
    StoreDto updateStore(Long id, UpdateStoreCommand command);
    StoreDto changeStatus(Long id, String newStatus);
    StoreDto verifyEmail(Long id);
    StoreDto getById(Long id);
    List<StoreDocumentDto> getDocuments(Long storeId);
    StoreDocumentDto reviewDocument(Long storeId, Long docId, ReviewDocumentCommand command);
    List<StoreCategoryDto> listCategories();
    StoreCategoryDto createCategory(CreateStoreCategoryCommand command);
    StoreCategoryDto updateCategory(Long id, CreateStoreCategoryCommand command);
    void deleteCategory(Long id);
}