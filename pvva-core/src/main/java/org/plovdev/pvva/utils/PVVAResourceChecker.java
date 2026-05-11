package org.plovdev.pvva.utils;

import org.plovdev.pvva.models.configs.resourceconfig.CategoriesResources;
import org.plovdev.pvva.models.configs.resourceconfig.MainResources;
import org.plovdev.pvva.models.configs.resourceconfig.ModelsResources;
import org.plovdev.pvva.models.configs.resourceconfig.ResourceConfig;

import java.util.Optional;

public record PVVAResourceChecker(ResourceConfig resourceConfig) {
    public boolean supportMain() {
        MainResources resources = resourceConfig.mainResources();
        return resources.supports() && resources.endpoint().isPresent();
    }

    public boolean supportMainSearch() {
        MainResources resources = resourceConfig.mainResources();
        if (resources.supports() && resources.endpoint().isPresent()) {
            return resources.supportSearch() && resources.searchUrl().isPresent();
        } else {
            return false;
        }
    }

    public boolean supportModels() {
        Optional<ModelsResources> resourcesOpt = resourceConfig.modelsResources();
        if (resourcesOpt.isPresent()) {
            ModelsResources resources = resourcesOpt.get();
            return resources.supports() && resources.endpoint().isPresent();
        } else {
            return false;
        }
    }

    public boolean supportModel() {
        Optional<ModelsResources> resourcesOpt = resourceConfig.modelsResources();
        if (resourcesOpt.isPresent()) {
            ModelsResources resources = resourcesOpt.get();
            if (resources.supports() && resources.endpoint().isPresent()) {
                return resources.supportModel() && resources.modelEndpoint().isPresent();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean supportModelsSearch() {
        Optional<ModelsResources> resourcesOpt = resourceConfig.modelsResources();
        if (resourcesOpt.isEmpty()) {
            return false;
        }
        ModelsResources resources = resourcesOpt.get();
        if (resources.supports() && resources.endpoint().isPresent()) {
            return resources.supportModelSearch() && resources.modelSearchEndpoint().isPresent();
        } else {
            return false;
        }
    }

    public boolean supportCategories() {
        Optional<CategoriesResources> resourcesOpt = resourceConfig.categoriesResources();
        if (resourcesOpt.isPresent()) {
            CategoriesResources resources = resourcesOpt.get();
            return resources.supports() && resources.endpoint().isPresent();
        } else {
            return false;
        }
    }

    public boolean supportCategory() {
        Optional<CategoriesResources> resourcesOpt = resourceConfig.categoriesResources();
        if (resourcesOpt.isPresent()) {
            CategoriesResources resources = resourcesOpt.get();
            if (resources.supports() && resources.endpoint().isPresent()) {
                return resources.supportCategory() && resources.categoryEndpoint().isPresent();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean supportVideo() {
        return resourceConfig.supportVideo();
    }

    public boolean supportMirrors() {
        return resourceConfig.supportMirrors() && resourceConfig.mirrors().isPresent();
    }
}