package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {

    // id로 포인트 조회
    UserPoint selectById(Long id);

    // 포인트 충전 or 사용
    UserPoint insertOrUpdate(Long id, Long point, TransactionType transactionType);

    // 포인트 내역 조회
    public List<PointHistory> selectHistory(Long id);

}
