package com.sciencebot.pos.categories;

import java.util.List;
import java.util.Optional;

public interface CategoryFacade {
    Optional<CategoryDto> getById(Long id);
    CategoryDto createCategory(CreateCategoryCommand command);
    CategoryDto updateCategory(Long id, UpdateCategoryCommand command);
    void deleteCategory(Long id);
    List<CategoryDto> listAll();
}
