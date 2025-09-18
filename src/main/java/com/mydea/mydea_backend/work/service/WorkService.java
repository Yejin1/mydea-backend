package com.mydea.mydea_backend.work.service;

import com.mydea.mydea_backend.storage.BlobSasService;
import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.dto.WorkUpdateRequest;
import com.mydea.mydea_backend.work.repo.WorkRepository;
import com.mydea.mydea_backend.work.dto.WorkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;
    private final BlobSasService blobSasService;

    @Transactional
    public Work create(WorkRequest req) {
        validate(req);
        Work entity = mapToEntity(req);
        return workRepository.save(entity);
    }

    private void validate(WorkRequest req) {
        // designType이 flower면 꽃 컬러 필수
        if (req.designType() == Work.DesignType.flower) {
            if (req.flowerPetal() == null || req.flowerCenter() == null) {
                throw new IllegalArgumentException("flower 디자인은 flowerPetal, flowerCenter가 필요합니다.");
            }
        }
        // ring인데 완전 수동(autoSize=0)이고 radius도 없고 sizeIndex도 없으면 경고
        if (req.workType() == Work.WorkType.ring
                && (req.autoSize() == 0)
                && req.radiusMm() == null
                && req.sizeIndex() == null) {
            throw new IllegalArgumentException("ring 수동 사이즈 저장 시 radiusMm 또는 sizeIndex 중 하나는 필요합니다.");
        }
    }

    @Transactional
    public Work update(Long id, WorkUpdateRequest r) {
        Work w = workRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("작업물 없음: id=" + id));

        // 검증: flower면 두 컬러 필요
        if (r.designType() == Work.DesignType.flower &&
                (r.flowerPetal() == null || r.flowerCenter() == null)) {
            throw new IllegalArgumentException("flower 디자인은 flowerPetal/flowerCenter가 필요합니다.");
        }
        // ring 수동 사이즈면 radius 또는 sizeIndex 중 하나 필요
        if (r.workType() == Work.WorkType.ring &&
                r.autoSize() != null && r.autoSize() == 0 &&
                r.radiusMm() == null && r.sizeIndex() == null) {
            throw new IllegalArgumentException("ring 수동 사이즈: radiusMm 또는 sizeIndex 필요");
        }

        // 전체 업데이트 (PUT)
        w.setName(r.name());
        w.setWorkType(r.workType());
        w.setDesignType(r.designType());
        w.setColors(r.colors());
        w.setFlowerPetal(r.flowerPetal());
        w.setFlowerCenter(r.flowerCenter());
        w.setAutoSize(r.autoSize());
        w.setRadiusMm(r.radiusMm());
        w.setSizeIndex(r.sizeIndex());

        return workRepository.save(w);
    }


    private Work mapToEntity(WorkRequest r) {
        return Work.builder()
                .userId(r.userId())
                .name(r.name())
                .workType(r.workType())
                .designType(r.designType())
                .colors(r.colors())
                .flowerPetal(r.flowerPetal())
                .flowerCenter(r.flowerCenter())
                .autoSize(r.autoSize())
                .radiusMm(r.radiusMm())
                .sizeIndex(r.sizeIndex())
                .build();
    }

    @Transactional
    public void deleteWorks(List<Long> ids) {
        // TODO: Blob Storage에서도 previewUrl 삭제해야함
        List<Work> works = workRepository.findAllById(ids);
        works.forEach(w -> {
            if (w.getPreviewUrl() != null) {
                //blobServiceClient.getBlobContainerClient(...).getBlobClient(...).deleteIfExists();
            }
        });
        workRepository.deleteAllByIdIn(ids);
    }

    @Transactional
    public void updatePreviewUrl(Long id, String previewUrl) {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("작업물 없음: id=" + id));
        work.setPreviewUrl(previewUrl);
        workRepository.save(work);
    }
}
