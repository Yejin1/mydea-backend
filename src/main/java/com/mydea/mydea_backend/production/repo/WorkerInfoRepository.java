package com.mydea.mydea_backend.production.repo;

import com.mydea.mydea_backend.production.domain.WorkerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkerInfoRepository extends JpaRepository<WorkerInfo, Long> {

    List<WorkerInfo> findByActiveTrue();

    /**
     * dow: 1=MON, 2=TUE, 3=WED, 4=THU, 5=FRI, 6=SAT, 7=SUN
     * required: 필요한 분(해당 요일의 가능 분이 이 값 이상인 작업자 조회)
     */
    @Query("select w from WorkerInfo w where w.active = true and " +
            " (case :dow when 1 then w.monMin when 2 then w.tueMin when 3 then w.wedMin when 4 then w.thuMin when 5 then w.friMin when 6 then w.satMin when 7 then w.sunMin end) >= :required " +
            " order by w.id asc")
    List<WorkerInfo> findAvailableByDow(@Param("dow") int dayOfWeek1to7,
                                        @Param("required") int requiredMinutes);
}
