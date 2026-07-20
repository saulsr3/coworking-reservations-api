package com.cuscatlan.coworking.service;

import com.cuscatlan.coworking.dto.request.SpaceRequest;
import com.cuscatlan.coworking.dto.response.SpaceResponse;
import com.cuscatlan.coworking.entity.Space;
import com.cuscatlan.coworking.entity.SpaceType;
import com.cuscatlan.coworking.exception.ResourceNotFoundException;
import com.cuscatlan.coworking.mapper.SpaceMapper;
import com.cuscatlan.coworking.repository.SpaceRepository;
import com.cuscatlan.coworking.specification.SpaceSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;

    public List<SpaceResponse> findAll(SpaceType type, Integer minCapacity, String location) {
        return spaceRepository.findAll(SpaceSpecifications.withFilters(type, minCapacity, location))
                .stream()
                .map(spaceMapper::toResponse)
                .toList();
    }

    public SpaceResponse findById(Long id) {
        return spaceMapper.toResponse(getSpaceOrThrow(id));
    }

    @Transactional
    public SpaceResponse create(SpaceRequest request) {
        Space space = spaceMapper.toEntity(request);
        return spaceMapper.toResponse(spaceRepository.save(space));
    }

    @Transactional
    public SpaceResponse update(Long id, SpaceRequest request) {
        Space space = getSpaceOrThrow(id);
        spaceMapper.updateEntityFromRequest(request, space);
        return spaceMapper.toResponse(spaceRepository.save(space));
    }

    @Transactional
    public void delete(Long id) {
        if (!spaceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Space", id);
        }
        spaceRepository.deleteById(id);
    }

    Space getSpaceOrThrow(Long id) {
        return spaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space", id));
    }
}