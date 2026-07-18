package com.cuscatlan.coworking.repository;

import com.cuscatlan.coworking.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * JpaSpecificationExecutor permite construir filtros dinámicos (tipo, capacidad
 * mínima, ubicación) sin explotar en un método por combinación de parámetros.
 */
public interface SpaceRepository extends JpaRepository<Space, Long>, JpaSpecificationExecutor<Space> {
}
