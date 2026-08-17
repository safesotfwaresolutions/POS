package com.sciencebot.pos.products.internal.services;

import com.sciencebot.pos.categories.CategoryDeleteValidator;
import com.sciencebot.pos.categories.CategoryFacade;
import com.sciencebot.pos.categories.CategoryProductCountProvider;
import com.sciencebot.pos.products.*;
import com.sciencebot.pos.products.internal.entities.Product;
import com.sciencebot.pos.products.internal.repositories.ProductRepository;
import com.sciencebot.pos.products.internal.repositories.ProductSpecifications;
import com.sciencebot.pos.products.internal.mappers.ProductMapper;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductFacade, CategoryDeleteValidator, CategoryProductCountProvider {

    private final ProductRepository productRepository;
    private final CategoryFacade categoryFacade;
    private final ProductMapper productMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            @org.springframework.context.annotation.Lazy CategoryFacade categoryFacade,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryFacade = categoryFacade;
        this.productMapper = productMapper;
    }

    @Override
    public Optional<ProductDto> getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        return Optional.of(productMapper.toDto(product));
    }


    @Override
    @Transactional
    public ProductDto createProduct(CreateProductCommand command) {
        validateCommand(command.internalCode(), command.purchasePrice(), command.salePrice(), command.categoryId());

        if (productRepository.existsByInternalCode(command.internalCode().trim())) {
            throw new IllegalArgumentException("Ya existe un producto con el código interno: " + command.internalCode().trim());
        }

        if (command.barcode() != null && !command.barcode().isBlank()) {
            String cleanBarcode = command.barcode().trim();
            if (productRepository.existsByBarcode(cleanBarcode)) {
                throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + cleanBarcode);
            }
        }

        categoryFacade.getById(command.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("La categoría especificada no existe"));

        if (command.quantityAvailable() < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }

        Product product = new Product();
        product.setInternalCode(command.internalCode().trim());
        product.setBarcode(command.barcode() != null ? command.barcode().trim() : null);
        product.setName(command.name().trim());
        product.setCategoryId(command.categoryId());
        product.setPurchasePrice(command.purchasePrice());
        product.setSalePrice(command.salePrice());
        product.setMinStock(command.minStock());
        product.setDescription(command.description() != null ? command.description().trim() : null);
        product.setImageUrl(command.imageUrl() != null ? command.imageUrl().trim() : null);
        product.setQuantityAvailable(command.quantityAvailable());
        product.setActive(true);

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductCommand command) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        validatePrices(command.purchasePrice(), command.salePrice());

        categoryFacade.getById(command.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("La categoría especificada no existe"));

        if (command.barcode() != null && !command.barcode().isBlank()) {
            String cleanBarcode = command.barcode().trim();
            if (!cleanBarcode.equals(product.getBarcode()) && productRepository.existsByBarcode(cleanBarcode)) {
                throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + cleanBarcode);
            }
        }

        product.setName(command.name().trim());
        product.setCategoryId(command.categoryId());
        product.setPurchasePrice(command.purchasePrice());
        product.setSalePrice(command.salePrice());
        product.setMinStock(command.minStock());
        product.setBarcode(command.barcode() != null ? command.barcode().trim() : null);
        product.setDescription(command.description() != null ? command.description().trim() : null);
        product.setImageUrl(command.imageUrl() != null ? command.imageUrl().trim() : null);

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void changeStatus(Long id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        product.setActive(active);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public Page<ProductDto> searchProducts(String search, Long categoryId, Boolean active, Pageable pageable) {
        Specification<Product> spec = Specification.unrestricted();
        if (search != null) {
            spec = spec.and(ProductSpecifications.hasName(search));
            spec = spec.or(ProductSpecifications.hasInternalCode(search));
            spec = spec.or(ProductSpecifications.hasBarcode(search));
        } 
        if (categoryId != null) {
            spec = spec.and(ProductSpecifications.hasCategoryId(categoryId));
        }
        if (active != null) {
            spec = spec.and(ProductSpecifications.hasActive(active));
        }

        Page<Product> page = productRepository.findAll(spec, pageable);
        return page.map(productMapper::toDto);
        
    }

    @Override
    public void validateBeforeDelete(Long categoryId) {
        if (productRepository.existsByCategoryIdAndActiveTrue(categoryId)) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene productos asociados activos");
        }
    }

    @Override
    public int getProductCount(Long categoryId) {
        return productRepository.countByCategoryIdAndActiveTrue(categoryId);
    }


    private void validateCommand(String internalCode, BigDecimal purchasePrice, BigDecimal salePrice, Long categoryId) {
        if (internalCode == null || internalCode.isBlank()) {
            throw new IllegalArgumentException("El código interno es obligatorio");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        validatePrices(purchasePrice, salePrice);
    }

    private void validatePrices(BigDecimal purchasePrice, BigDecimal salePrice) {
        if (purchasePrice == null || purchasePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de compra no puede ser negativo");
        }
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser negativo");
        }
        if (salePrice.compareTo(purchasePrice) < 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor o igual al precio de compra (RN-PROD-001)");
        }
    }

    @Override
    @Transactional
    public void updateStock(Long id, int quantityDelta) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Producto no encontrado con ID: " + id));
        int newStock = product.getQuantityAvailable() + quantityDelta;
        if (newStock < 0) {
            throw new IllegalArgumentException("No hay suficiente stock para el producto: " + product.getName());
        }
        product.setQuantityAvailable(newStock);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updatePurchasePrice(Long id, BigDecimal newPrice) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Producto no encontrado con ID: " + id));
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de compra no puede ser negativo");
        }
        if (product.getSalePrice().compareTo(newPrice) < 0) {
            throw new IllegalArgumentException("El nuevo precio de compra no puede ser mayor que el precio de venta actual: " + product.getSalePrice());
        }
        product.setPurchasePrice(newPrice);
        productRepository.save(product);
    }
}

